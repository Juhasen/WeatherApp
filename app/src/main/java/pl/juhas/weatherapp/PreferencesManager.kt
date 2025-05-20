package pl.juhas.weatherapp

import android.content.Context
import android.content.SharedPreferences
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
        private const val KEY_UNIT = "unit_preference" // Klucz dla jednostek
        private const val KEY_WEATHER_DATA = "weather_data" // Klucz dla danych pogodowych
    }

    // Dodaj ulubione miejsce
    fun addFavoritePlace(place: FavoritePlace) {
        val current = getFavoritePlaces().toMutableList()
        current.add(place)
        prefs.edit {
            putString(KEY_FAVORITES, gson.toJson(current))
        }
    }

    // Usuń ulubione miejsce
    fun removeFavoritePlace(place: FavoritePlace) {
        val current = getFavoritePlaces().toMutableList()
        current.remove(place)
        prefs.edit {
            putString(KEY_FAVORITES, gson.toJson(current))
        }
    }

    // Pobierz ulubione miejsca
    fun getFavoritePlaces(): List<FavoritePlace> {
        val json = prefs.getString(KEY_FAVORITES, "[]") ?: "[]"
        val type = object : TypeToken<List<FavoritePlace>>() {}.type
        return gson.fromJson(json, type)
    }

    // Zapisz preferowaną jednostkę
    fun saveUnitPreference(unit: String) {
        prefs.edit {
            putString(KEY_UNIT, unit)
        }
    }

    // Pobierz preferowaną jednostkę
    fun getUnitPreference(): String {
        return prefs.getString(KEY_UNIT, "Metric") ?: "Metric" // Domyślnie "Metric"
    }

    // Zapisz dane pogodowe dla ulubionych miejsc
    fun saveWeatherData(placeName: String, weatherData: String) {
        prefs.edit {
            putString("$KEY_WEATHER_DATA-$placeName", weatherData)
        }
    }

    // Pobierz dane pogodowe dla ulubionego miejsca
    fun getWeatherData(placeName: String): String? {
        return prefs.getString("$KEY_WEATHER_DATA-$placeName", null)
    }

    // Flow opakowujący listener
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
