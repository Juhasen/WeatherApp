package pl.juhas.weatherapp.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.juhas.weatherapp.api.model.ForecastDay
import pl.juhas.weatherapp.api.model.ForecastModel
import kotlin.collections.filter
import kotlin.collections.first
import kotlin.collections.firstOrNull
import kotlin.collections.maxOfOrNull
import kotlin.math.roundToInt

@Composable
fun ForecastView(data: ForecastModel) {
    val groupedByDate = remember(data) { data.list.groupBy { it.dt_txt.substringBefore(" ") } }
    val sortedDates = remember(groupedByDate) { groupedByDate.keys.sorted() }

    val dailyForecasts = remember(sortedDates) {
        sortedDates.mapNotNull { date ->
            val dayEntries = groupedByDate[date] ?: return@mapNotNull null
            val maxTemp = dayEntries.maxOfOrNull { it.main.temp } ?: return@mapNotNull null

            val nightTemps = dayEntries.filter {
                it.dt_txt.endsWith("03:00:00")
            }
            val minNightTemp = nightTemps.minOfOrNull { it.main.temp }

            val targetEntry = dayEntries.firstOrNull { it.dt_txt.endsWith("15:00:00") }
                ?: dayEntries.first()

            ForecastDay(
                data = targetEntry,
                maxTemp = maxTemp.roundToInt(),
                minTemp = minNightTemp?.roundToInt()
            )
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(dailyForecasts.drop(1)) { forecastDay ->
            ForecastCard(forecastDay)
        }
    }
}