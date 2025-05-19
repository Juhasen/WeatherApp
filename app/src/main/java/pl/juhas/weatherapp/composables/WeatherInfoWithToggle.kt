package pl.juhas.weatherapp.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pl.juhas.weatherapp.api.model.ForecastModel
import pl.juhas.weatherapp.api.model.WeatherModel

@Composable
fun WeatherInfoWithToggle(current: WeatherModel, forecast: ForecastModel) {
    // 0 = Details, 1 = Forecast
    var currentTab by remember { mutableStateOf(0) }


    // Toggle buttons
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { currentTab = 0 },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentTab == 0) Color.White else Color.Gray.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
        ) {
            Text("Forecast", color = Color.Black)
        }
        Button(
            onClick = { currentTab = 1 },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentTab == 1) Color.White else Color.Gray.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        ) {
            Text("Details", color = Color.Black)
        }
    }

    // Conditional content area
    when (currentTab) {
        0 -> ForecastView(forecast)
        1 -> DetailedWeatherInfo(current)
    }
}