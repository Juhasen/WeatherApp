package pl.juhas.weatherapp.composables

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import pl.juhas.weatherapp.api.model.ForecastModel
import pl.juhas.weatherapp.api.model.WeatherModel

@Composable
fun WeatherInfoWithToggle(current: WeatherModel, forecast: ForecastModel) {
    // Używamy city.id jako unikalny klucz do wymuszenia rekomponowania
    key(forecast.city.id) {
        Log.i("WeatherInfoWithToggle", "Recomposing with forecast for: ${forecast.city.name}")
        ForecastView(forecast)
    }

    DetailedWeatherInfo(current)
}
