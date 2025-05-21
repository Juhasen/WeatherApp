package pl.juhas.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pl.juhas.weatherapp.ui.theme.WeatherAppTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create ViewModel with factory that passes context
        val weatherViewModel = ViewModelProvider(
            this,
            WeatherViewModelFactory(applicationContext)
        )[WeatherViewModel::class.java]

        setContent {
            WeatherAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    WeatherPage(weatherViewModel, this)
                }
            }
        }
    }
}

// Add this factory class in the same file or a separate file
class WeatherViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}