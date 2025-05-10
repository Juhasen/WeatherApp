package pl.juhas.weatherapp.api

data class ForecastModel(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<Forecast>,
    val message: Int
)