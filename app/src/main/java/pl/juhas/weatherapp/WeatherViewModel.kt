package pl.juhas.weatherapp

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.gson.Gson
import pl.juhas.weatherapp.api.Constant
import pl.juhas.weatherapp.api.NetworkResponse
import pl.juhas.weatherapp.api.RetrofitInstance
import pl.juhas.weatherapp.api.model.ForecastModel
import pl.juhas.weatherapp.api.model.GeoLocationModel
import pl.juhas.weatherapp.api.model.WeatherModel

class WeatherViewModel(context: Context) : ViewModel() {
    private val appContext = context.applicationContext

    fun checkConnection(): Boolean {
        return NetworkUtils.isInternetAvailable(appContext)
    }

    private val gson = Gson()
    private val weatherApi = RetrofitInstance.weatherAPI
    private val geoApi = RetrofitInstance.geoAPI
    private val preferencesManager = PreferencesManager(context)

    var lastLon by mutableStateOf(
        value = "0.0",
        policy = structuralEqualityPolicy()
    )

    var lastLat by mutableStateOf(
        value = "0.0",
        policy = structuralEqualityPolicy()
    )

    var unit by mutableStateOf(
        value = "Metric",
        policy = structuralEqualityPolicy()
    )

    var refreshInterval by mutableStateOf(
        value = preferencesManager.getRefreshInterval(),
        policy = structuralEqualityPolicy()
    )
    private var refreshJob: kotlinx.coroutines.Job? = null

    private val _currentWeatherResult = MutableLiveData<NetworkResponse<WeatherModel>>()
    val currentWeatherResult: LiveData<NetworkResponse<WeatherModel>> = _currentWeatherResult

    private val _forecastResult = MutableLiveData<NetworkResponse<ForecastModel>>()
    val forecastResult: LiveData<NetworkResponse<ForecastModel>> = _forecastResult

    private val _geoLocationResult = MutableLiveData<NetworkResponse<GeoLocationModel>>()
    val geoLocationResult: LiveData<NetworkResponse<GeoLocationModel>> = _geoLocationResult

    private val _favoritePlaces = MutableStateFlow<List<FavoritePlace>>(emptyList())
    val favoritePlaces: StateFlow<List<FavoritePlace>> = _favoritePlaces

    private val _favoriteWeatherData = MutableStateFlow<Map<String, WeatherModel>>(emptyMap())
    val favoriteWeatherData: StateFlow<Map<String, WeatherModel>> = _favoriteWeatherData

    init {
        // Wczytaj preferowaną jednostkę z SharedPreferences
        unit = preferencesManager.getUnitPreference()
        refreshInterval = preferencesManager.getRefreshInterval()
        startAutoRefresh()
        viewModelScope.launch {
            preferencesManager.favoritePlacesFlow
                .collect { places ->
                    _favoritePlaces.value = places
                    fetchWeatherForFavorites() // Pobierz dane pogodowe dla ulubionych miejsc
                }
        }
    }

    private fun refreshFavoritePlaces() {
        _favoritePlaces.value = preferencesManager.getFavoritePlaces()
    }

    fun addFavoritePlace(name: String, country: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            Log.i("addFavoritePlace", "$name $country $lat $lon")
            val place = FavoritePlace(name, country, lat, lon)
            preferencesManager.addFavoritePlace(place)
            refreshFavoritePlaces()
        }
    }

    fun removeFavoritePlace(name: String, country: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            val place = FavoritePlace(name, country, lat, lon)
            preferencesManager.removeFavoritePlace(place)
            refreshFavoritePlaces()
        }
    }

    fun updateUnit(newUnit: String) {
        unit = newUnit
        preferencesManager.saveUnitPreference(newUnit)
        refreshWeatherData()
    }

    fun updateRefreshInterval(newInterval: Int) {
        refreshInterval = newInterval
        preferencesManager.saveRefreshInterval(newInterval)
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        if (refreshInterval <= 0) return // Wyłącz odświeżanie, jeśli interwał to 0
        refreshJob = viewModelScope.launch {
            while (true) {
                refreshWeatherData()
                kotlinx.coroutines.delay(refreshInterval * 1000L)
            }
        }
    }

    private fun isValidLocation(lat: String, lon: String): Boolean {
        return lat != "0.0" && lon != "0.0" && lat.isNotBlank() && lon.isNotBlank()
    }

    private fun refreshWeatherData() {
        if (checkConnection()) {
            if (isValidLocation(lastLat, lastLon)) {
                getCurrentWeather(lastLat, lastLon)
                getForecast(lastLat, lastLon)
            }
            fetchWeatherForFavorites()
            val date = java.util.Date(System.currentTimeMillis())
            Log.i("refreshWeatherData", "Refreshed weather data at $date")
        }
    }

    fun getCurrentWeather(lat: String, lon: String) {
        lastLat = lat
        lastLon = lon
        if (!isValidLocation(lat, lon)) return

        viewModelScope.launch {
            _currentWeatherResult.value = NetworkResponse.Loading

            val cachedData = preferencesManager.getWeatherData(lat, lon)

            if (cachedData != null) {
                try {
                    val weather = gson.fromJson(cachedData, WeatherModel::class.java)
                    _currentWeatherResult.value = NetworkResponse.Success(weather)
                } catch (e: Exception) {
                    Log.e("WeatherViewModel", "Error loading cached weather", e)
                }
            }

            if (checkConnection()) {
                try {
                    val response = weatherApi.getCurrentWeather(lat, lon, Constant.apiKey, unit)
                    if (response.isSuccessful) {
                        response.body()?.let { weather ->
                            _currentWeatherResult.value = NetworkResponse.Success(weather)
                            // Save with location-specific key
                            Log.i("saveWeatherDataCurrentGETWEATHER", "$lat, $lon")
                            preferencesManager.saveWeatherData(
                                lat, lon,
                                gson.toJson(weather)
                            )

                            // Also save as last location for fallback
                            Log.i("saveWeatherDataCurrentGETWEATHER", "$lat, $lon")
                            preferencesManager.saveLastLocation(lat, lon)
                        } ?: run {
                            _currentWeatherResult.value = NetworkResponse.Error("No data found")
                        }
                    } else if (cachedData == null) {
                        _currentWeatherResult.value = NetworkResponse.Error("API error: ${response.code()}")
                    }
                } catch (e: Exception) {
                    if (cachedData == null) {
                        _currentWeatherResult.value = NetworkResponse.Error("Network error: ${e.message}")
                    }
                }
            } else if (cachedData == null) {
                // Fallback to last location data if available
                loadLastLocationWeather()
            }
        }
    }

    private fun loadLastLocationWeather() {
        val (lat, lon) = preferencesManager.getLastLocation() ?: Pair("0.0", "0.0")

        if (lat != "0.0" && lon != "0.0") {
            val cachedData = preferencesManager.getWeatherData(lat, lon)

            if (cachedData != null) {
                try {
                    val weather = gson.fromJson(cachedData, WeatherModel::class.java)
                    _currentWeatherResult.value = NetworkResponse.Success(weather)
                    lastLat = lat.toString()
                    lastLon = lon.toString()
                } catch (e: Exception) {
                    _currentWeatherResult.value = NetworkResponse.Error("No cached data available")
                }
            } else {
                _currentWeatherResult.value = NetworkResponse.Error("No internet and no cached data")
            }
        } else {
            _currentWeatherResult.value = NetworkResponse.Error("No internet and no location history")
        }
    }


    fun getForecast(lat: String, lon: String) {
        viewModelScope.launch {
            _forecastResult.value = NetworkResponse.Loading

            val cachedData = preferencesManager.getForecastData(lat, lon)

            if (cachedData != null) {
                try {
                    val forecast = gson.fromJson(cachedData, ForecastModel::class.java)
                    _forecastResult.value = NetworkResponse.Success(forecast)
                } catch (e: Exception) {
                    Log.e("WeatherViewModel", "Error loading cached forecast", e)
                }
            }

            if (checkConnection()) {
                try {
                    val response = weatherApi.getForecast(lat, lon, Constant.apiKey, unit)
                    if (response.isSuccessful) {
                        response.body()?.let { forecast ->
                            _forecastResult.value = NetworkResponse.Success(forecast)
                            preferencesManager.saveForecastData(lat, lon, gson.toJson(forecast))
                        } ?: run {
                            _forecastResult.value = NetworkResponse.Error("No data found")
                        }
                    } else if (cachedData == null) {
                        _forecastResult.value = NetworkResponse.Error("API error: ${response.code()}")
                    }
                } catch (e: Exception) {
                    if (cachedData == null) {
                        _forecastResult.value = NetworkResponse.Error("Network error: ${e.message}")
                    }
                }
            } else if (cachedData == null) {
                _forecastResult.value = NetworkResponse.Error("No internet and no cached forecast")
            }
        }
    }


    fun getGeoLocation(city: String) {
        viewModelScope.launch {
            _geoLocationResult.value = NetworkResponse.Loading

            if (checkConnection()) {
                try {
                    val response = geoApi.getGeoLocation(city, Constant.apiKey)
                    if (response.isSuccessful) {
                        response.body()?.let { locations ->
                            _geoLocationResult.value = NetworkResponse.Success(locations)
                        } ?: run {
                            _geoLocationResult.value = NetworkResponse.Error("No data found")
                        }
                    } else {
                        _geoLocationResult.value = NetworkResponse.Error("API error: ${response.code()}")
                    }
                } catch (e: Exception) {
                    _geoLocationResult.value = NetworkResponse.Error("Network error: ${e.message}")
                }
            } else {
                _geoLocationResult.value = NetworkResponse.Error("No internet connection")
            }
        }
    }


    fun fetchWeatherForFavorites() {
        viewModelScope.launch {
            val favoritePlaces = preferencesManager.getFavoritePlaces()
            val weatherDataMap = mutableMapOf<String, WeatherModel>()

            favoritePlaces.forEach { place ->
                Log.i("fetchWeatherForFavorites", "${place.name} ${place.lat} ${place.lon}")
                val lat = place.lat.toString()
                val lon = place.lon.toString()

                if (checkConnection()) {
                    // Online mode - fetch fresh data
                    try {
                        val response = weatherApi.getCurrentWeather(
                            lat, lon,
                            Constant.apiKey,
                            unit
                        )
                        if (response.isSuccessful) {
                            response.body()?.let { weather ->
                                weatherDataMap[place.name] = weather
                                Log.i("saveWeatherDataFavourites", "$lat, $lon")
                                preferencesManager.saveWeatherData(lat,lon, gson.toJson(weather))
                            }
                        }
                    } catch (e: Exception) {
                        // Fall back to cached data if available
                        val cached = preferencesManager.getWeatherData(lat, lon)
                        cached?.let {
                            try {
                                weatherDataMap[place.name] = gson.fromJson(it, WeatherModel::class.java)
                            } catch (e: Exception) { /* ignore */
                            }
                        }
                    }
                } else {
                    // Offline mode - load cached data
                    val cached = preferencesManager.getWeatherData(lat, lon)
                    cached?.let {
                        try {
                            weatherDataMap[place.name] = gson.fromJson(it, WeatherModel::class.java)
                        } catch (e: Exception) { /* ignore */
                        }
                    }
                }
            }
            _favoriteWeatherData.value = weatherDataMap
        }
    }
}
