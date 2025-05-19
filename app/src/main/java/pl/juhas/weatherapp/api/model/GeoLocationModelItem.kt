package pl.juhas.weatherapp.api.model

data class GeoLocationModelItem(
    val country: String,
    val lat: Double,
    val local_names: LocalNames,
    val lon: Double,
    val name: String,
    val state: String
)