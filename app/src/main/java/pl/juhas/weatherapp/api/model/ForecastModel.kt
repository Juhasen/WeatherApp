package pl.juhas.weatherapp.api.model

data class ForecastModel(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<Forecast>,
    val message: Int
)