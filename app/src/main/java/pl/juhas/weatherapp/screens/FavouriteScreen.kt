package pl.juhas.weatherapp.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.juhas.weatherapp.WeatherViewModel
import kotlin.math.roundToInt

@Composable
fun FavouriteScreen(
    viewModel: WeatherViewModel,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    var removingKey by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { viewModel.fetchWeatherForFavorites() }

    val favoritePlaces by viewModel.favoritePlaces.collectAsState()
    val favoriteWeatherData by viewModel.favoriteWeatherData.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 42.dp)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = favoritePlaces,
            key = { it.name }
        ) { place ->
            val isRemoving = removingKey == place.name

            AnimatedVisibility(
                visible = !isRemoving,
                exit = fadeOut(animationSpec = tween(500))
            ) {
                FavoritePlaceItem(
                    placeName = "${place.name}, ${place.country}",
                    temperature = favoriteWeatherData[place.name]?.main?.temp
                        ?.roundToInt()?.toString() ?: "--",
                    weatherIconUrl = favoriteWeatherData[place.name]?.weather
                        ?.firstOrNull()?.icon
                        ?.let { "https://openweathermap.org/img/wn/$it@2x.png" },
                    onClick = {
                        navController.navigate("home/${place.lat}/${place.lon}/${place.name}/${place.country}")
                    },
                    onRemove = {
                        removingKey = place.name
                        scope.launch {
                            delay(500L)
                            viewModel.removeFavoritePlace(
                                place.name, place.country, place.lat, place.lon
                            )
                            removingKey = null
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FavoritePlaceItem(
    placeName: String,
    temperature: String,
    weatherIconUrl: String?,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    // state for the overflow menu
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.inversePrimary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // City name
            Text(
                text = placeName,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))

            // Temperature and icon, right-aligned
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$temperature°",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                weatherIconUrl?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Overflow menu button
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text("Remove")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onRemove()
                        }
                    )
                }
            }
        }
    }
}

