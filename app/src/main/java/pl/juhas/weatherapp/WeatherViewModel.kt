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

        // Wczytaj ostatnią lokalizację i dane pogodowe
        loadLastLocationOnStartup()

        startAutoRefresh()
        viewModelScope.launch {
            preferencesManager.favoritePlacesFlow
                .collect { places ->
                    _favoritePlaces.value = places
                    fetchWeatherForFavorites() // Pobierz dane pogodowe dla ulubionych miejsc
                }
        }
    }

    private fun loadLastLocationOnStartup() {
        viewModelScope.launch {
            // Pobierz zapisaną ostatnią lokalizację
            val (lat, lon) = preferencesManager.getLastLocation() ?: Pair("0.0", "0.0")
            if (lat != "0.0" && lon != "0.0") {
                lastLat = lat
                lastLon = lon

                // Próba pobrania danych z serwera jeśli jest internet
                if (checkConnection()) {
                    getCurrentWeather(lat, lon)
                    getForecast(lat, lon)
                } else {
                    // Wczytaj dane z pamięci podręcznej
                    val cachedWeatherData = preferencesManager.getWeatherData(lat, lon)
                    val cachedForecastData = preferencesManager.getForecastData(lat, lon)

                    if (cachedWeatherData != null) {
                        try {
                            val weather = gson.fromJson(cachedWeatherData, WeatherModel::class.java)
                            _currentWeatherResult.value = NetworkResponse.Success(weather)
                        } catch (e: Exception) {
                            Log.e("WeatherViewModel", "Error loading cached weather", e)
                            _currentWeatherResult.value = NetworkResponse.Error("Błąd podczas wczytywania danych pogodowych z pamięci podręcznej")
                        }
                    }

                    if (cachedForecastData != null) {
                        try {
                            val forecast = gson.fromJson(cachedForecastData, ForecastModel::class.java)
                            _forecastResult.value = NetworkResponse.Success(forecast)
                        } catch (e: Exception) {
                            Log.e("WeatherViewModel", "Error loading cached forecast", e)
                            _forecastResult.value = NetworkResponse.Error("Błąd podczas wczytywania prognozy pogody z pamięci podręcznej")
                        }
                    }
                }
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
                // Tworzę unikalny klucz dla każdej lokalizacji
                val uniqueKey = "${place.name}_${place.country}"

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
                                weatherDataMap[uniqueKey] = weather
                                Log.i("saveWeatherDataFavourites", "$lat, $lon")
                                preferencesManager.saveWeatherData(lat, lon, gson.toJson(weather))
                            }
                        }
                    } catch (e: Exception) {
                        // Fall back to cached data if available
                        val cached = preferencesManager.getWeatherData(lat, lon)
                        cached?.let {
                            try {
                                weatherDataMap[uniqueKey] = gson.fromJson(it, WeatherModel::class.java)
                            } catch (e: Exception) { /* ignore */
                            }
                        }
                    }
                } else {
                    // Offline mode - load cached data
                    val cached = preferencesManager.getWeatherData(lat, lon)
                    cached?.let {
                        try {
                            weatherDataMap[uniqueKey] = gson.fromJson(it, WeatherModel::class.java)
                        } catch (e: Exception) { /* ignore */
                        }
                    }
                }
            }
            _favoriteWeatherData.value = weatherDataMap
        }
    }

    /**
     * Synchronicznie pobiera zarówno aktualną pogodę, jak i prognozę dla wybranej lokalizacji.
     * Zapewnia spójność danych przy kliknięciu w ulubione miejsce.
     */
    fun loadFullWeatherData(lat: String, lon: String) {
        viewModelScope.launch {
            // Użyj wartości lastLat i lastLon do śledzenia aktualnie wybranej lokalizacji
            lastLat = lat
            lastLon = lon

            Log.i("loadFullWeatherData", "Loading full data for $lat, $lon")

            // Najpierw ustawić stan ładowania dla obu źródeł danych
            _currentWeatherResult.value = NetworkResponse.Loading
            _forecastResult.value = NetworkResponse.Loading

            if (checkConnection()) {
                // Online mode
                try {
                    // Pobierz aktualne dane pogodowe
                    val weatherResponse = weatherApi.getCurrentWeather(lat, lon, Constant.apiKey, unit)
                    if (weatherResponse.isSuccessful) {
                        weatherResponse.body()?.let { weather ->
                            _currentWeatherResult.value = NetworkResponse.Success(weather)
                            preferencesManager.saveWeatherData(lat, lon, gson.toJson(weather))
                            preferencesManager.saveLastLocation(lat, lon)
                        } ?: run {
                            _currentWeatherResult.value = NetworkResponse.Error("No weather data found")
                        }
                    } else {
                        _currentWeatherResult.value = NetworkResponse.Error("API error: ${weatherResponse.code()}")
                    }

                    // Pobierz dane prognozy
                    val forecastResponse = weatherApi.getForecast(lat, lon, Constant.apiKey, unit)
                    if (forecastResponse.isSuccessful) {
                        forecastResponse.body()?.let { forecast ->
                            _forecastResult.value = NetworkResponse.Success(forecast)
                            preferencesManager.saveForecastData(lat, lon, gson.toJson(forecast))
                        } ?: run {
                            _forecastResult.value = NetworkResponse.Error("No forecast data found")
                        }
                    } else {
                        _forecastResult.value = NetworkResponse.Error("API error: ${forecastResponse.code()}")
                    }
                } catch (e: Exception) {
                    // W przypadku błędu pobierania, spróbuj wczytać dane z pamięci podręcznej
                    Log.e("loadFullWeatherData", "Error fetching data", e)
                    loadCachedData(lat, lon)
                }
            } else {
                // Offline mode - pobierz dane z pamięci podręcznej
                loadCachedData(lat, lon)
            }
        }
    }

    /**
     * Pomocnicza metoda do ładowania danych z pamięci podręcznej.
     * Dodano normalizację współrzędnych dla zapewnienia spójności kluczy cache.
     */
    private fun loadCachedData(lat: String, lon: String) {
        // Normalizuj współrzędne żeby zapewnić spójność kluczy cache
        val normalizedLat = normalizeCoordinate(lat)
        val normalizedLon = normalizeCoordinate(lon)

        Log.i("loadCachedData", "Próba wczytania danych dla lokalizacji: $normalizedLat, $normalizedLon")

        // Sprawdzamy ostatnią lokalizację z zapisanych preferencji w celach debugowania
        val (savedLat, savedLon) = preferencesManager.getLastLocation() ?: Pair("0.0", "0.0")
        Log.i("loadCachedData", "Ostatnio zapisana lokalizacja: $savedLat, $savedLon")

        // Wczytaj dane aktualnej pogody z cache
        val cachedWeatherData = preferencesManager.getWeatherData(normalizedLat, normalizedLon)
        val cachedForecastData = preferencesManager.getForecastData(normalizedLat, normalizedLon)

        // Wyświetlmy obie wartości w logach
        Log.i("loadCachedData", "Dane pogody z cache: ${if (cachedWeatherData != null) "ZNALEZIONO" else "BRAK"}")
        Log.i("loadCachedData", "Dane prognozy z cache: ${if (cachedForecastData != null) "ZNALEZIONO" else "BRAK"}")

        var weatherLoaded = false
        var forecastLoaded = false

        // Wczytaj dane aktualnej pogody
        if (cachedWeatherData != null) {
            try {
                val weather = gson.fromJson(cachedWeatherData, WeatherModel::class.java)
                _currentWeatherResult.value = NetworkResponse.Success(weather)
                weatherLoaded = true
                Log.i("loadCachedData", "Pomyślnie wczytano dane pogodowe dla: $normalizedLat, $normalizedLon | Miasto: ${weather.name}")
            } catch (e: Exception) {
                Log.e("loadCachedData", "Error loading cached weather", e)
                _currentWeatherResult.value = NetworkResponse.Error("Błąd podczas wczytywania danych pogodowych z pamięci podręcznej")
            }
        } else {
            Log.e("loadCachedData", "Brak zapisanych danych pogodowych dla: $normalizedLat, $normalizedLon")
            _currentWeatherResult.value = NetworkResponse.Error("Brak zapisanych danych pogodowych dla tej lokalizacji")
        }

        // Wczytaj dane prognozy
        if (cachedForecastData != null) {
            try {
                val forecast = gson.fromJson(cachedForecastData, ForecastModel::class.java)
                _forecastResult.value = NetworkResponse.Success(forecast)
                forecastLoaded = true
                // Wyciągnij nazwę miasta z prognozy aby zweryfikować
                val forecastCity = forecast.city.name
                val forecastCountry = forecast.city.country
                Log.i("loadCachedData", "Pomyślnie wczytano prognozę dla: $normalizedLat, $normalizedLon | Miasto: $forecastCity, $forecastCountry")
            } catch (e: Exception) {
                Log.e("loadCachedData", "Error loading cached forecast", e)
                _forecastResult.value = NetworkResponse.Error("Błąd podczas wczytywania prognozy pogody z pamięci podręcznej")
            }
        } else {
            Log.e("loadCachedData", "Brak zapisanej prognozy dla: $normalizedLat, $normalizedLon")
            _forecastResult.value = NetworkResponse.Error("Brak zapisanej prognozy dla tej lokalizacji")
        }

        // Jeśli nie udało się załadować ani pogody ani prognozy, spróbuj użyć ostatnio zapisanej lokalizacji
        if (!weatherLoaded && !forecastLoaded) {
            Log.w("loadCachedData", "Nie udało się załadować danych dla $normalizedLat, $normalizedLon. Próba użycia ostatnio zapisanej lokalizacji.")
            fallbackToLastLocation()
        }
    }

    /**
     * Metoda awaryjna, która próbuje załadować dane z ostatnio zapisanej lokalizacji
     */
    private fun fallbackToLastLocation() {
        val (lat, lon) = preferencesManager.getLastLocation() ?: Pair("0.0", "0.0")

        if (isValidLocation(lat, lon)) {
            Log.i("fallbackToLastLocation", "Próba wczytania danych z ostatniej lokalizacji: $lat, $lon")

            val normalizedLat = normalizeCoordinate(lat)
            val normalizedLon = normalizeCoordinate(lon)

            // Wczytaj dane aktualnej pogody z cache
            val cachedWeatherData = preferencesManager.getWeatherData(normalizedLat, normalizedLon)
            if (cachedWeatherData != null) {
                try {
                    val weather = gson.fromJson(cachedWeatherData, WeatherModel::class.java)
                    _currentWeatherResult.value = NetworkResponse.Success(weather)
                    Log.i("fallbackToLastLocation", "Pomyślnie wczytano dane pogodowe dla ostatniej lokalizacji | Miasto: ${weather.name}")
                } catch (e: Exception) {
                    Log.e("fallbackToLastLocation", "Error loading cached weather from last location", e)
                }
            }

            // Wczytaj dane prognozy z cache
            val cachedForecastData = preferencesManager.getForecastData(normalizedLat, normalizedLon)
            if (cachedForecastData != null) {
                try {
                    val forecast = gson.fromJson(cachedForecastData, ForecastModel::class.java)
                    _forecastResult.value = NetworkResponse.Success(forecast)
                    Log.i("fallbackToLastLocation", "Pomyślnie wczytano prognozę dla ostatniej lokalizacji | Miasto: ${forecast.city.name}")
                } catch (e: Exception) {
                    Log.e("fallbackToLastLocation", "Error loading cached forecast from last location", e)
                }
            }
        } else {
            Log.e("fallbackToLastLocation", "Brak zapisanej ostatniej lokalizacji lub nieprawidłowy format")
        }
    }

    /**
     * Funkcja pomocnicza do normalizacji współrzędnych geograficznych.
     * Zapewnia spójny format klucza niezależnie od źródła współrzędnych.
     */
    private fun normalizeCoordinate(coord: String): String {
        return try {
            // Konwertujemy string na double i z powrotem na string z ustaloną precyzją
            val value = coord.toDouble()
            String.format("%.4f", value).trimEnd('0').trimEnd('.')
                .replace(",", ".") // Dla różnych ustawień lokalnych
        } catch (e: Exception) {
            Log.e("normalizeCoordinate", "Błąd podczas normalizacji współrzędnej: $coord", e)
            coord // Zwracamy oryginalną wartość jeśli jest problem
        }
    }
}
