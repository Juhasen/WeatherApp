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
import pl.juhas.weatherapp.ui.theme.DarkPurple
import pl.juhas.weatherapp.ui.theme.LightPurple
import pl.juhas.weatherapp.ui.theme.Purple
import pl.juhas.weatherapp.ui.theme.unselectedColor

@Composable
fun WeatherPage(viewModel: WeatherViewModel) {
    val navController = rememberNavController()

    Box(
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
                        SettingsScreen(viewModel)
                    }
                }
            }


            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            NavigationBar(
                containerColor = Purple,
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    selected = currentRoute == "home",
                    onClick = { navController.navigate("home") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = unselectedColor,
                        selectedTextColor = Color.White,
                        unselectedTextColor = unselectedColor,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == "favourite",
                    onClick = { navController.navigate("favourite") },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favourite") },
                    label = { Text("Favourite") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = unselectedColor,
                        selectedTextColor = Color.White,
                        unselectedTextColor = unselectedColor,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == "settings",
                    onClick = { navController.navigate("settings") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = unselectedColor,
                        selectedTextColor = Color.White,
                        unselectedTextColor = unselectedColor,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
