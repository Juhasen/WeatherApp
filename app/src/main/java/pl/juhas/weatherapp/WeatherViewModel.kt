package pl.juhas.weatherapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.juhas.weatherapp.api.RetrofitInstance
import pl.juhas.weatherapp.api.Constant
import pl.juhas.weatherapp.api.NetworkResponse
import pl.juhas.weatherapp.api.WeatherModel

class WeatherViewModel :ViewModel() {

    private val weatherApi = RetrofitInstance.weatherAPI
    private val _weatherResult = MutableLiveData<NetworkResponse<WeatherModel>>()
    val weatherResult : LiveData<NetworkResponse<WeatherModel>> = _weatherResult

    fun getData(city: String) {
        viewModelScope.launch {
            _weatherResult.value = NetworkResponse.Loading
            try {
                val response = weatherApi.getWeather(city, Constant.apiKey)
                if (response.isSuccessful) {
                    response.body()?.let{
                        _weatherResult.value = NetworkResponse.Success(it)
                    } ?: run {
                        _weatherResult.value = NetworkResponse.Error("No data found")
                    }
                } else {
                    _weatherResult.value = NetworkResponse.Error("Error: ${response.code()}. Failed to fetch data.")
                }
            } catch (e: Exception) {
                _weatherResult.value = NetworkResponse.Error("Error: Failed to fetch data.")
            }

        }
    }
}