package pl.juhas.weatherapp.screens
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavBackStackEntry
import pl.juhas.weatherapp.WeatherViewModel
import pl.juhas.weatherapp.api.NetworkResponse
import pl.juhas.weatherapp.api.model.ForecastModel
import pl.juhas.weatherapp.api.model.GeoLocationModelItem
import pl.juhas.weatherapp.api.model.WeatherModel
import pl.juhas.weatherapp.composables.ErrorHandler
import pl.juhas.weatherapp.composables.GeneralCurrentWeatherInfo
import pl.juhas.weatherapp.composables.OfflineDataBanner
import pl.juhas.weatherapp.composables.WeatherInfoWithToggle
import pl.juhas.weatherapp.ui.theme.DarkPurple
import pl.juhas.weatherapp.ui.theme.LightPurple
import pl.juhas.weatherapp.ui.theme.Purple

@Composable
fun HomeScreen(viewModel: WeatherViewModel, backStackEntry: NavBackStackEntry? = null) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var localCity by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }

    // Zmienna do śledzenia, czy używamy danych offline
    var isOfflineMode by remember { mutableStateOf(!viewModel.checkConnection()) }

    // Pobierz szerokość, długość geograficzną, nazwę miasta i kraj z argumentów nawigacji
    val latFromNav = backStackEntry?.arguments?.getString("lat")
    val lonFromNav = backStackEntry?.arguments?.getString("lon")
    val cityNameFromNav = backStackEntry?.arguments?.getString("city")
    val countryFromNav = backStackEntry?.arguments?.getString("country")

    fun load(lat: String, lon: String) {
        Log.i("Coordinates LOAD", "HomeScreen: $lat, $lon")
        viewModel.getCurrentWeather(lat, lon)
        viewModel.getForecast(lat, lon)
    }

    val initialized = remember { mutableStateOf(false) }

    LaunchedEffect(latFromNav, lonFromNav) {
        if (!latFromNav.isNullOrEmpty() && !lonFromNav.isNullOrEmpty() && !initialized.value) {
            load(latFromNav, lonFromNav)
            Log.i("Coordinates from NAV", "HomeScreen: $latFromNav, $lonFromNav")
            localCity = listOfNotNull(cityNameFromNav, countryFromNav).joinToString(", ")
            initialized.value = true
        }
    }


    fun previewCityOptionsLoad(city: String) {
        viewModel.getGeoLocation(city)
    }

    val cityOptions by viewModel.geoLocationResult.observeAsState()
    val current by viewModel.currentWeatherResult.observeAsState()
    val forecast by viewModel.forecastResult.observeAsState()

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.screenHeightDp > configuration.screenWidthDp

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
            .then(if (isPortrait) Modifier.padding(top = 30.dp) else Modifier)
    ) {
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
                        // Sprawdź, czy jest połączenie z internetem przed wyszukiwaniem
                        val hasConnection = viewModel.checkConnection()
                        if (hasConnection) {
                            Log.i("SZUKANIE MIASTA", "HomeScreen: $localCity")
                            previewCityOptionsLoad(localCity)
                            showSuggestions = true
                            isOfflineMode = false
                        } else {
                            isOfflineMode = true
                        }
                    }) {
                        Icon(Icons.Default.Search, null, tint = Color.White)
                    }
                }
            )
        }

        // Pokaż baner offline, gdy jesteśmy w trybie offline i mamy dane pogodowe
        if (isOfflineMode && current is NetworkResponse.Success<*>) {
            val weatherModel = (current as NetworkResponse.Success<*>).data as WeatherModel
            OfflineDataBanner(
                city = "${weatherModel.name}, ${weatherModel.sys.country}"
            )
        }

        fun formatCoord(coord: Double): String {
            return "%.4f".format(coord).trimEnd('0').trimEnd('.').replace(",", ".")
        }

        val context = LocalContext.current
        if (showSuggestions && cityOptions is NetworkResponse.Success<*>) {
            val rawCities = (cityOptions as NetworkResponse.Success<*>).data as List<GeoLocationModelItem>
            val cities = rawCities.distinctBy { "${it.name}-${it.country}-${it.state}" }
            if (cities.isEmpty()) {
                Toast.makeText(context, "No results found", Toast.LENGTH_SHORT).show()
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .border(
                            width = 2.dp,
                            color = Color.LightGray,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .background(Color.Black.copy(alpha = 0.1f))
                        .padding(vertical = 16.dp)
                ) {

                    val gradientBrush = Brush.horizontalGradient(
                        colors = listOf(Purple, LightPurple),
                        tileMode = TileMode.Clamp
                    )

                    items(cities.size) { index ->
                        val city = cities[index]
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .background(brush = gradientBrush, shape = RoundedCornerShape(20.dp))
                                .clickable {
                                    keyboardController?.hide()
                                    val latFormatted = formatCoord(city.lat)
                                    val lonFormatted = formatCoord(city.lon)
                                    load(latFormatted, lonFormatted)
                                    Log.i("Coordinates", "Load po click: $latFormatted, $lonFormatted")
                                    localCity = listOfNotNull(
                                        city.name,
                                        city.state,
                                        city.country
                                    ).joinToString(", ")
                                    showSuggestions = false
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                val cityName = if (city.state != null) {
                                    "${city.name}, ${city.state}, ${city.country}"
                                } else {
                                    "${city.name}, ${city.country}"
                                }
                                Text(
                                    text = cityName,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (current is NetworkResponse.Loading || forecast is NetworkResponse.Loading) {
            CircularProgressIndicator(
                color = Color.White,
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

            // Zaktualizuj nazwę miasta w polu wyszukiwania, jeśli jest puste
            if (localCity.isEmpty()) {
                localCity = "${currentModel.name}, ${currentModel.sys.country}"
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    GeneralCurrentWeatherInfo(
                        currentModel,
                        viewModel = viewModel
                    )
                }
                item {
                    WeatherInfoWithToggle(currentModel, forecastModel)
                }
            }
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
