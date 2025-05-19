package pl.juhas.weatherapp.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.zIndex
import pl.juhas.weatherapp.WeatherViewModel
import pl.juhas.weatherapp.api.NetworkResponse
import pl.juhas.weatherapp.api.model.ForecastModel
import pl.juhas.weatherapp.api.model.GeoLocationModel
import pl.juhas.weatherapp.api.model.GeoLocationModelItem
import pl.juhas.weatherapp.api.model.WeatherModel
import pl.juhas.weatherapp.composables.ErrorHandler
import pl.juhas.weatherapp.composables.GeneralCurrentWeatherInfo
import pl.juhas.weatherapp.composables.WeatherInfoWithToggle
import pl.juhas.weatherapp.ui.theme.DarkPurple
import pl.juhas.weatherapp.ui.theme.LightPurple
import pl.juhas.weatherapp.ui.theme.Purple

@Composable
fun HomeScreen(viewModel: WeatherViewModel) {
    var localCity by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }

    fun load(lat: String, lon: String) {
        viewModel.getCurrentWeather(lat, lon)
        viewModel.getForecast(lat, lon)
    }

    fun previewCityOptionsLoad(city: String) {
        viewModel.getGeoLocation(city)
    }

    val cityOptions by viewModel.geoLocationResult.observeAsState()
    val current by viewModel.currentWeatherResult.observeAsState()
    val forecast by viewModel.forecastResult.observeAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        DarkPurple,
                        Purple,
                        LightPurple
                    )
                )
            )
            .padding(top = 20.dp)
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = localCity,
                onValueChange = { newCity ->
                    localCity = newCity
                },
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
                        Log.i("SZUKANIE MIASTA","HomeScreen: $localCity")
                        previewCityOptionsLoad(localCity)
                        showSuggestions = true
                    }) {
                        Icon(Icons.Default.Search, null, tint = Color.White)
                    }
                }
            )
        }

        if (showSuggestions && cityOptions is NetworkResponse.Success<*>) {
            val rawCities = (cityOptions as NetworkResponse.Success<*>).data as List<GeoLocationModelItem>
            val cities = rawCities.distinctBy { "${it.name}-${it.country}-${it.state}"}

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
            ) {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .background(Color.Black.copy(alpha = 0.85f))
                ) {
                    items(cities.size) { index ->
//                        Log.d("CITY OPTIONS", cities.joinToString("\n") { "${it.name}, ${it.state}, ${it.country} -> ${it.lat}, ${it.lon}" })
                        val city = cities[index]
                        Text(
                            text = "${city.name}, ${city.country}, ${city.state}, ${city.lat}, ${city.lon}",
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .background(Color(0x33000000), RoundedCornerShape(4.dp))
                                .padding(12.dp)
                                .clickable {
                                    Log.i("Coords of CITY", "HomeScreen: ${city.lat}, ${city.lon}")
                                    load(city.lat.toString(), city.lon.toString())
                                    localCity = "${city.name}, ${city.country}, ${city.state}"
                                    showSuggestions = false
                                }
                        )
                    }
                }
            }
        }

        if (current is NetworkResponse.Loading || forecast is NetworkResponse.Loading) {
            CircularProgressIndicator(color = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )
            return
        }

        val errorMessage = when {
            current is NetworkResponse.Error -> (current as NetworkResponse.Error).message
            forecast is NetworkResponse.Error -> (forecast as NetworkResponse.Error).message
            else -> null
        }
        if (errorMessage != null) {
            ErrorHandler(errorMessage)
            return
        }

        if (current is NetworkResponse.Success<*> && forecast is NetworkResponse.Success<*>) {
            val currentModel = (current as NetworkResponse.Success<*>).data as WeatherModel
            val forecastModel = (forecast as NetworkResponse.Success<*>).data as ForecastModel

            GeneralCurrentWeatherInfo(currentModel)
            WeatherInfoWithToggle(currentModel, forecastModel)
        } else {
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