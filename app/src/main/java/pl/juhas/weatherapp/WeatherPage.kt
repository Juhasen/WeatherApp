package pl.juhas.weatherapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pl.juhas.weatherapp.composables.FavouriteScreen
import pl.juhas.weatherapp.composables.HomeScreen
import pl.juhas.weatherapp.composables.SettingsScreen

@Composable
fun WeatherPage(viewModel: WeatherViewModel) {
    val navController = rememberNavController()

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
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Main content fills remaining height
            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home") {
                        HomeScreen(viewModel)
                    }
                    composable("favourite") {
                        FavouriteScreen()
                    }
                    composable("settings") {
                        SettingsScreen()
                    }
                }
            }


            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            NavigationBar(
                containerColor = Color(0xFF3C2B8E),
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    selected = currentRoute == "home",
                    onClick = { navController.navigate("home") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color(0xFFB3A9D6),
                        selectedTextColor = Color.White,
                        unselectedTextColor = Color(0xFFB3A9D6)
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == "favourite",
                    onClick = { navController.navigate("favourite") },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favourite") },
                    label = { Text("Favourite") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color(0xFFB3A9D6),
                        selectedTextColor = Color.White,
                        unselectedTextColor = Color(0xFFB3A9D6)
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == "settings",
                    onClick = { navController.navigate("settings") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color(0xFFB3A9D6),
                        selectedTextColor = Color.White,
                        unselectedTextColor = Color(0xFFB3A9D6)
                    )
                )
            }
        }
    }
}
