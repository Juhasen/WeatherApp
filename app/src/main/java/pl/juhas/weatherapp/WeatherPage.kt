package pl.juhas.weatherapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import pl.juhas.weatherapp.api.Forecast
import pl.juhas.weatherapp.api.ForecastModel
import pl.juhas.weatherapp.api.NetworkResponse
import pl.juhas.weatherapp.api.Weather
import pl.juhas.weatherapp.api.WeatherModel
import kotlin.math.roundToInt

@Composable
fun WeatherPage(viewModel: WeatherViewModel) {
    var city by remember { mutableStateOf("") }

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
                brush = Brush.verticalGradient(listOf(
                    Color(0xFF1B1B3A),
                    Color(0xFF3C2B8E),
                    Color(0xFFA142F4)
                ))
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
                val currentModel  = (current as NetworkResponse.Success<*>).data as WeatherModel
                val forecastModel = (forecast as NetworkResponse.Success<*>).data as ForecastModel

                // Current weather
                WeatherDetails(currentModel)

                // The toggle that shows either details or forecast
                WeatherDetailsWithToggle(currentModel, forecastModel)
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


@Composable
fun ErrorHandler(errorMessage: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = errorMessage,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun WeatherDetails(current: WeatherModel) {


    val iconUrl = "https://openweathermap.org/img/wn/${current.weather[0].icon}@2x.png"

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
                            Color(0xFFA142F4),
                            Color(0xFF3C2B8E),
                            Color(0xFF1B1B3A)
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
                // Location
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${current.name}, ${current.sys.country}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // Weather icon
                AsyncImage(
                    model = iconUrl,
                    contentDescription = current.weather[0].description,
                    modifier = Modifier.size(64.dp),
                    placeholder = painterResource(R.drawable.ic_placeholder),
                    error = painterResource(R.drawable.ic_error)
                )

                // Temperature
                Text(
                    text = "${current.main.temp.toFloat().roundToInt()}°",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Description
                Text(
                    text = current.weather[0].description.replaceFirstChar { it.uppercase() },
                    fontSize = 18.sp,
                    color = Color.White
                )

                //Refresh date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                )
                {
                    Text(
                        text = "Last updated: ${current.dt}",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherDetailsWithToggle(current: WeatherModel, forecast: ForecastModel) {
    // 0 = Details, 1 = Forecast
    var currentTab by remember { mutableStateOf(0) }


    // Toggle buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            Text("Details", color = Color.Black)
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
            Text("Forecast", color = Color.Black)
        }
    }

    // Conditional content area
    when (currentTab) {
        0 -> DetailedInfo(current)
        1 -> ForecastView(forecast)
    }
}

@Composable
fun ForecastView(x0: ForecastModel) {
    Text("Forecast View", color = Color.White, fontSize = 18.sp)
}

@Composable
fun DetailedInfo(x0: WeatherModel) {
    Text("Detailed Info", color = Color.White, fontSize = 18.sp)
}


//@Composable
//fun ForecastView(data: ForecastModel) {
//    // Group 3-hour entries by calendar date
//    val groupedByDate: Map<String, List<ForecastModel.ForecastItem>> = remember(data) {
//        data.list.groupBy { it.dt_txt.substringBefore(" ") }
//    }
//
//    // Sort dates chronologically
//    val sortedDates = remember(groupedByDate) { groupedByDate.keys.sorted() }
//
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp)
//    ) {
//        sortedDates.forEach { date ->
//            val dayItems = groupedByDate[date] ?: emptyList()
//
//            // Date header
//            item {
//                Text(
//                    text = LocalDate.parse(date)
//                        .format(DateTimeFormatter.ofPattern("EEE, MMM d")),
//                    color = Color.White,
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .background(Color(0xFF3C2B8E))
//                        .padding(vertical = 8.dp, horizontal = 16.dp)
//                )
//            }
//
//            // Horizontal list of forecasts
//            item {
//                LazyRow(
//                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
//                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    items(dayItems) { item ->
//                        ForecastCard(item)
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun ForecastCard(item: ForecastModel.For) {
//    Surface(
//        shape = RoundedCornerShape(16.dp),
//        tonalElevation = 6.dp,
//        modifier = Modifier
//            .size(width = 100.dp, height = 160.dp)
//            .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
//    ) {
//        Column(
//            modifier = Modifier
//                .background(Color(0xFF1B1B3A))
//                .fillMaxSize()
//                .padding(8.dp),
//            verticalArrangement = Arrangement.SpaceBetween,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            // Time label
//            val time = item.dt_txt.substringAfter(" ").substringBeforeLast(":")
//            Text(text = time, color = Color.White, fontSize = 14.sp)
//
//            // Weather icon
//            AsyncImage(
//                model = "https://openweathermap.org/img/wn/${item.weather[0].icon}@2x.png",
//                contentDescription = item.weather[0].description,
//                modifier = Modifier.size(48.dp)
//            )
//
//            // Temperature
//            Text(
//                text = "${item.main.temp.roundToInt()}°",
//                color = Color.White,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Bold
//            )
//
//            // Short description
//            Text(
//                text = item.weather[0].description.replaceFirstChar { it.uppercase() },
//                color = Color.LightGray,
//                fontSize = 12.sp
//            )
//        }
//    }

