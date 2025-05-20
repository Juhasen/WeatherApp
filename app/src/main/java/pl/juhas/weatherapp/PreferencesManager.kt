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
