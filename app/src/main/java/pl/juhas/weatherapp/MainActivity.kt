package pl.juhas.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pl.juhas.weatherapp.ui.theme.WeatherAppTheme
import androidx.core.content.edit

class MainActivity : ComponentActivity() {
    private lateinit var weatherViewModel: WeatherViewModel

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        weatherViewModel = ViewModelProvider(
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

    override fun onStop() {
        Log.e("MainActivity", "onStop wywoływane - zatrzymywanie zadań")
        weatherViewModel.stopAllJobs()
        super.onStop()
    }

    override fun onPause() {
        Log.e("MainActivity", "onPause wywoływane - zatrzymywanie zadań")
        weatherViewModel.stopAllJobs()
        super.onPause()
    }

    override fun onDestroy() {
        Log.e("MainActivity", "onDestroy wywoływane - zatrzymywanie zadań")
        weatherViewModel.stopAllJobs()
        super.onDestroy()
    }
}

class WeatherViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

