package pl.juhas.weatherapp.api

import pl.juhas.weatherapp.api.model.ForecastModel
import pl.juhas.weatherapp.api.model.WeatherModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {

    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
    ) : Response<WeatherModel>

    @GET("forecast")
    suspend fun getForecast(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
    ) : Response<ForecastModel>
}