package pl.juhas.weatherapp.api.model

data class ForecastDay(
    val data: Forecast,
    val maxTemp: Int,
    val minTemp: Int?
)