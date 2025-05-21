package pl.juhas.weatherapp.composables

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import coil.compose.AsyncImage
import pl.juhas.weatherapp.FavoritePlace
import pl.juhas.weatherapp.R
import pl.juhas.weatherapp.WeatherViewModel
import pl.juhas.weatherapp.api.model.WeatherModel
import pl.juhas.weatherapp.ui.theme.DarkPurple
import pl.juhas.weatherapp.ui.theme.LightPurple
import pl.juhas.weatherapp.ui.theme.Purple
import kotlin.math.roundToInt

@Composable
fun GeneralCurrentWeatherInfo(
    current: WeatherModel,
    viewModel: WeatherViewModel,
) {
    val iconUrl = "https://openweathermap.org/img/wn/${current.weather[0].icon}@2x.png"

    // Observe favorite places from ViewModel
    val favoritePlacesCollection: Collection<FavoritePlace> by
    viewModel.favoritePlaces.collectAsState(initial = emptyList())

    val isFavorite = favoritePlacesCollection.any {
        it.name == current.name && it.country == current.sys.country &&
        it.lat == current.coord.lat && it.lon == current.coord.lon
    }

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
            // Top-left IconButton
            IconButton(
                onClick = {
                    if (isFavorite) {
                        viewModel.removeFavoritePlace(
                            current.name,
                            current.sys.country,
                            current.coord.lat,
                            current.coord.lon
                        )
                    } else {
                        Log.i("addFavoritePlaceGENERAL", "${current.name}, ${current.sys.country}, ${current.coord.lat}, ${current.coord.lon}")
                        viewModel.addFavoritePlace(
                            current.name,
                            current.sys.country,
                            current.coord.lat,
                            current.coord.lon
                        )
                    }
                },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color.Magenta else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
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
            }
        }
    }
}
