package pl.juhas.weatherapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import pl.juhas.weatherapp.api.NetworkResponse
import pl.juhas.weatherapp.api.model.ForecastModel
import pl.juhas.weatherapp.api.model.WeatherModel
import pl.juhas.weatherapp.composables.ErrorHandler
import pl.juhas.weatherapp.composables.GeneralCurrentWeatherInfo
import pl.juhas.weatherapp.composables.WeatherInfoWithToggle

@Composable
fun WeatherPage(viewModel: WeatherViewModel) {
    var city by remember { mutableStateOf("") }
    val navController = rememberNavController()
    val current by viewModel.currentWeatherResult.observeAsState()
    val forecast by viewModel.forecastResult.observeAsState()

    // Kick off both requests when the user taps search
    fun load(city: String) {
        viewModel.getCurrentWeather(city)
        viewModel.getForecast(city)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF1B1B3A),
                        Color(0xFF3C2B8E),
                        Color(0xFFA142F4)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(30.dp),
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    singleLine = true,
                    label = { Text("Search for a location", color = Color.LightGray) },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color.White
                    ),
                    trailingIcon = {
                        IconButton(onClick = {
                            val q = city.ifBlank { "Zgierz" }
                            load(q)
                        }) {
                            Icon(Icons.Default.Search, null, tint = Color.White)
                        }
                    }
                )
            }

            // Show loading if either is loading
            if (current is NetworkResponse.Loading || forecast is NetworkResponse.Loading) {
                CircularProgressIndicator(color = Color.White)
                return@Column
            }

            // Show error if either failed
            val errorMessage = when {
                current is NetworkResponse.Error -> (current as NetworkResponse.Error).message
                forecast is NetworkResponse.Error -> (forecast as NetworkResponse.Error).message
                else -> null
            }
            if (errorMessage != null) {
                ErrorHandler(errorMessage)
                return@Column
            }

            // Only when both are success, render the UI
            if (current is NetworkResponse.Success<*> && forecast is NetworkResponse.Success<*>) {
                val currentModel = (current as NetworkResponse.Success<*>).data as WeatherModel
                val forecastModel = (forecast as NetworkResponse.Success<*>).data as ForecastModel

                // Current weather
                GeneralCurrentWeatherInfo(currentModel)

                // The toggle that shows either details or forecast
                WeatherInfoWithToggle(currentModel, forecastModel)
            } else {
                // Initial placeholder
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Enter a city name to retrieve the weather",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}
