package pl.juhas.weatherapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.juhas.weatherapp.api.RetrofitInstance
import pl.juhas.weatherapp.api.Constant
import pl.juhas.weatherapp.api.ForecastModel
import pl.juhas.weatherapp.api.NetworkResponse
import pl.juhas.weatherapp.api.WeatherModel

class WeatherViewModel :ViewModel() {

    private val weatherApi = RetrofitInstance.weatherAPI

    private val _currentWeatherResult = MutableLiveData<NetworkResponse<WeatherModel>>()
    val currentWeatherResult : LiveData<NetworkResponse<WeatherModel>> = _currentWeatherResult

    fun getCurrentWeather(city: String) {
        viewModelScope.launch {
            _currentWeatherResult.value = NetworkResponse.Loading
            try {
                val response = weatherApi.getCurrentWeather(city, Constant.apiKey)
                if (response.isSuccessful) {
                    response.body()?.let{
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


    private val _forecastResult = MutableLiveData<NetworkResponse<ForecastModel>>()
    val forecastResult : LiveData<NetworkResponse<ForecastModel>> = _forecastResult

    fun getForecast(city: String) {
        viewModelScope.launch {
            _forecastResult.value = NetworkResponse.Loading
            try {
                val response = weatherApi.getForecast(city, Constant.apiKey)
                if (response.isSuccessful) {
                    response.body()?.let{
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