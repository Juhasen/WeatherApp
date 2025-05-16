package pl.juhas.weatherapp.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.juhas.weatherapp.api.model.WeatherModel
import pl.juhas.weatherapp.ui.theme.DarkPurple
import pl.juhas.weatherapp.ui.theme.LightPurple
import pl.juhas.weatherapp.ui.theme.Purple
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetailedWeatherInfo(current: WeatherModel) {
    Surface(
        shape = RoundedCornerShape(30.dp),
        tonalElevation = 15.dp,
        shadowElevation = 15.dp,
        modifier = Modifier
            .padding(16.dp)
            .border(
                width = 2.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(30.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            LightPurple,
                            Purple,
                            DarkPurple
                        ),
                        start = Offset(1000f, 0f),
                        end = Offset(0f, 1000f)
                    )
                )
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Humidity
                Text(
                    text = "Humidity: ${current.main.humidity}%",
                    fontSize = 18.sp,
                    color = Color.White
                )

                // Wind speed
                Text(
                    text = "Wind Speed: ${current.wind.speed} m/s",
                    fontSize = 18.sp,
                    color = Color.White
                )

                // Pressure
                Text(
                    text = "Pressure: ${current.main.pressure} hPa",
                    fontSize = 18.sp,
                    color = Color.White
                )
                // Visibility
                Text(
                    text = "Visibility: ${current.visibility / 1000} km",
                    fontSize = 18.sp,
                    color = Color.White
                )
                // Sunrise
                Text(
                    text = "Sunrise: ${
                        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(current.sys.sunrise * 1000L))
                    }",
                    fontSize = 18.sp,
                    color = Color.White
                )
                // Sunset
                Text(
                    text = "Sunset: ${
                        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(current.sys.sunset * 1000L))
                    }",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }
    }
}