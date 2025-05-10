package pl.juhas.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import pl.juhas.weatherapp.ui.theme.WeatherAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val weatherViewModel = ViewModelProvider(this)[WeatherViewModel::class.java]

        setContent {
            WeatherAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    WeatherPage(weatherViewModel)
                }
            }
        }
    }
}