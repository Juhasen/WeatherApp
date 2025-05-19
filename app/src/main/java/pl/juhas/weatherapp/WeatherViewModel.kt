package pl.juhas.weatherapp

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.juhas.weatherapp.api.RetrofitInstance
import pl.juhas.weatherapp.api.Constant
import pl.juhas.weatherapp.api.model.ForecastModel
import pl.juhas.weatherapp.api.NetworkResponse
import pl.juhas.weatherapp.api.model.GeoLocationModel
import pl.juhas.weatherapp.api.model.WeatherModel

class WeatherViewModel : ViewModel() {

    private val weatherApi = RetrofitInstance.weatherAPI
    private val geoApi = RetrofitInstance.geoAPI

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


    fun updateUnit(newUnit: String) { // Zmieniono nazwę metody
        unit = newUnit
    }

    fun getGeoLocation(city: String) {
        viewModelScope.launch {
            _geoLocationResult.value = NetworkResponse.Loading
            try {
                val response = geoApi.getGeoLocation(city, Constant.apiKey)
                Log.i("GEO RESULT", response.toString())
                if (response.isSuccessful) {
                    response.body()?.let {
                        _geoLocationResult.value = NetworkResponse.Success(it)
                    } ?: run {
                        _geoLocationResult.value = NetworkResponse.Error("No data found")
                    }
                } else {
                    _geoLocationResult.value = NetworkResponse.Error("Error: ${response.code()}. Failed to fetch data.")
                }
            } catch (e: Exception) {
                _geoLocationResult.value = NetworkResponse.Error("Error: Failed to fetch data.")

            }
        }
    }

//    private fun refreshWeatherData() {
//        viewModelScope.launch {
//            // Ponownie załaduj dane pogodowe z nową jednostką
//            _currentWeatherResult.value = NetworkResponse.Loading
//            _forecastResult.value = NetworkResponse.Loading
//            getCurrentWeather(lastCity)
//            getForecast(lastCity)
//        }
//    }

    fun getCurrentWeather(lat: String, lon: String) {
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
                    _currentWeatherResult.value = NetworkResponse.Error("Error: ${response.code()}. Failed to fetch data.")
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
                    _forecastResult.value = NetworkResponse.Error("Error: ${response.code()}. Failed to fetch data.")
                }
            } catch (e: Exception) {
                _forecastResult.value = NetworkResponse.Error("Error: Failed to fetch data.")
            }
        }
    }
}
