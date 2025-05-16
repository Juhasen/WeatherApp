package pl.juhas.weatherapp.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import pl.juhas.weatherapp.api.model.ForecastDay
import pl.juhas.weatherapp.ui.theme.LightPurple
import pl.juhas.weatherapp.ui.theme.Purple
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ForecastCard(item: ForecastDay) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        tonalElevation = 12.dp,
        modifier = Modifier
            .border(1.dp, Color.LightGray, RoundedCornerShape(50.dp))
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(LightPurple, Purple),
                    )
                )
                .padding(8.dp)
                .padding(vertical = 20.dp)
                .heightIn(min(150.dp, 200.dp)),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Max Temp
            Text(
                text = "${item.maxTemp}°",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // Min Temp (night)
            item.minTemp?.let {
                Text(
                    text = "${it}°",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Weather Icon
            AsyncImage(
                model = "https://openweathermap.org/img/wn/${item.data.weather[0].icon}@2x.png",
                contentDescription = item.data.weather[0].description,
                modifier = Modifier.size(64.dp)
            )

            // Day
            val date = item.data.dt_txt.substringBefore(" ")
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val localDate = LocalDate.parse(date, formatter)
            val dayOfWeek = localDate.dayOfWeek.name.take(3).uppercase()

            Text(
                text = dayOfWeek,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}