package pl.juhas.weatherapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"
    private const val GEO_BASE_URL = "https://api.openweathermap.org/geo/1.0/"

    private fun getInstance(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val weatherAPI: WeatherAPI = getInstance(WEATHER_BASE_URL).create(WeatherAPI::class.java)
    val geoAPI: GeoAPI = getInstance(GEO_BASE_URL).create(GeoAPI::class.java)
}