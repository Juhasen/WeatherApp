package pl.juhas.weatherapp

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class FavoritePlace(
    val name: String,
    val country: String,
    val lat: Double,
    val lon: Double
)

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_FAVORITES = "favorite_places"
        private const val KEY_UNIT = "unit_preference"
    }

    // Weather data storage by coordinates
    fun saveWeatherData(lat: String, lon: String, weatherData: String) {
        val key = "weather_${lat}_${lon}"
        Log.i("saveWeatherDataCurrent", "$lat, $lon")
        prefs.edit {
            putString(key, weatherData)
        }
    }

    fun getWeatherData(lat: String, lon: String): String? {
        val key = "weather_${lat}_${lon}"
        return prefs.getString(key, null)
    }

    // Forecast data storage by coordinates
    fun saveForecastData(lat: String, lon: String, forecastData: String) {
        val key = "forecast_${lat}_${lon}"
        prefs.edit {
            putString(key, forecastData)
        }
    }

    fun getForecastData(lat: String, lon: String): String? {
        val key = "forecast_${lat}_${lon}"
        return prefs.getString(key, null)
    }

    // Favorite places methods (unchanged)
    fun addFavoritePlace(place: FavoritePlace) {
        val current = getFavoritePlaces().toMutableList()
        current.add(place)
        prefs.edit {
            putString(KEY_FAVORITES, gson.toJson(current))
        }
    }

    fun removeFavoritePlace(place: FavoritePlace) {
        val current = getFavoritePlaces().toMutableList()
        current.remove(place)
        prefs.edit {
            putString(KEY_FAVORITES, gson.toJson(current))
        }
    }

    fun getFavoritePlaces(): List<FavoritePlace> {
        val json = prefs.getString(KEY_FAVORITES, "[]") ?: "[]"
        val type = object : TypeToken<List<FavoritePlace>>() {}.type
        return gson.fromJson(json, type)
    }

    // Unit preference methods (unchanged)
    fun saveUnitPreference(unit: String) {
        prefs.edit {
            putString(KEY_UNIT, unit)
        }
    }

    fun getUnitPreference(): String {
        return prefs.getString(KEY_UNIT, "Metric") ?: "Metric"
    }

    fun saveLastLocation(lat: String, lon: String) {
        prefs.edit {
            putString("last_location", "$lat,$lon")
        }
    }

    fun getLastLocation(): Pair<String, String>? {
        val lastLocation = prefs.getString("last_location", null) ?: return null
        val (lat, lon) = lastLocation.split(",")
        return Pair(lat, lon)
    }

    // Flow for favorite places (unchanged)
    val favoritePlacesFlow: Flow<List<FavoritePlace>> = callbackFlow {
        trySend(getFavoritePlaces())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_FAVORITES) {
                trySend(getFavoritePlaces())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}