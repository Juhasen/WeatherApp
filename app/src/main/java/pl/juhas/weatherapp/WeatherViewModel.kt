package pl.juhas.weatherapp

import android.content.Context
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
        preferencesManager.saveUnitPreference(newUnit) // Zapisz preferencję w SharedPreferences
        refreshWeatherData()
    }

    fun getGeoLocation(city: String) {
        viewModelScope.launch {
            _geoLocationResult.value = NetworkResponse.Loading
            try {
                val response = geoApi.getGeoLocation(city, Constant.apiKey)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _geoLocationResult.value = NetworkResponse.Success(it)
                        val location = it.firstOrNull()
                        if (location != null) {
                            getCurrentWeather(location.lat.toString(), location.lon.toString())
                            getForecast(location.lat.toString(), location.lon.toString())
                        }
                    } ?: run {
                        _geoLocationResult.value = NetworkResponse.Error("No data found")
                    }
                } else {
                    _geoLocationResult.value =
                        NetworkResponse.Error("Error: ${response.code()}. Failed to fetch data.")
                }
            } catch (e: Exception) {
                _geoLocationResult.value = NetworkResponse.Error("Error: Failed to fetch data.")
            }
        }
    }

    private fun refreshWeatherData() {
        viewModelScope.launch {
            _currentWeatherResult.value = NetworkResponse.Loading
            _forecastResult.value = NetworkResponse.Loading
            getCurrentWeather(lastLat, lastLon)
            getForecast(lastLat, lastLon)
        }
    }

    fun getCurrentWeather(lat: String, lon: String) {
        lastLat = lat
        lastLon = lon

        viewModelScope.launch {
            _currentWeatherResult.value = NetworkResponse.Loading
            try {
                val response = weatherApi.getCurrentWeather(lat, lon, Constant.apiKey, unit)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _currentWeatherResult.value = NetworkResponse.Success(it)
                    } ?: run {
                        _currentWeatherResult.value = NetworkResponse.Error("No data found")
                    }
                } else {
                    _currentWeatherResult.value =
                        NetworkResponse.Error("Error: ${response.code()}. Failed to fetch data.")
                }
            } catch (e: Exception) {
                _currentWeatherResult.value = NetworkResponse.Error("Error: Failed to fetch data.")
            }
        }
    }

    fun getForecast(lat: String, lon: String) {
        viewModelScope.launch {
            _forecastResult.value = NetworkResponse.Loading
            try {
                val response = weatherApi.getForecast(lat, lon, Constant.apiKey, unit)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _forecastResult.value = NetworkResponse.Success(it)
                    } ?: run {
                        _forecastResult.value = NetworkResponse.Error("No data found")
                    }
                } else {
                    _forecastResult.value =
                        NetworkResponse.Error("Error: ${response.code()}. Failed to fetch data.")
                }
            } catch (e: Exception) {
                _forecastResult.value = NetworkResponse.Error("Error: Failed to fetch data.")
            }
        }
    }

    fun fetchWeatherForFavorites() {
        viewModelScope.launch {
            val favoritePlaces = preferencesManager.getFavoritePlaces()
            val weatherDataMap = mutableMapOf<String, WeatherModel>()

            favoritePlaces.forEach { place ->
                try {
                    val response = weatherApi.getCurrentWeather(
                        place.lat.toString(),
                        place.lon.toString(),
                        Constant.apiKey,
                        unit
                    )
                    if (response.isSuccessful) {
                        response.body()?.let { weatherModel ->
                            weatherDataMap[place.name] = weatherModel
                            preferencesManager.saveWeatherData(place.name, gson.toJson(weatherModel))
                        }
                    }
                } catch (e: Exception) {
                    // Obsługa błędów
                }
            }
            _favoriteWeatherData.value = weatherDataMap
        }
    }
}
