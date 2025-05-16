package pl.juhas.weatherapp.api.model

import pl.juhas.weatherapp.api.model.Wind

data class Forecast(
    val clouds: Clouds,
    val dt: Int,
    val dt_txt: String,
    val main: Main,
    val pop: Double,
    val rain: Rain,
    val sys: Sys,
    val visibility: Int,
    val weather: List<Weather>,
    val wind: Wind
)