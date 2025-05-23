package pl.juhas.weatherapp

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pl.juhas.weatherapp.screens.FavouriteScreen
import pl.juhas.weatherapp.screens.HomeScreen
import pl.juhas.weatherapp.screens.SettingsScreen
import pl.juhas.weatherapp.ui.theme.DarkPurple
import pl.juhas.weatherapp.ui.theme.LightPurple
import pl.juhas.weatherapp.ui.theme.Purple
import pl.juhas.weatherapp.ui.theme.unselectedColor
import kotlin.math.min

@Composable
fun WeatherPage(viewModel: WeatherViewModel, context: Context) {

    val navController = rememberNavController()
    val configuration = LocalConfiguration.current

    // Tablet - przekątna większąaniż 7 cali lub szerokością większą niż 600dp w KAŻDEJ orientacji
    val isTablet = configuration.smallestScreenWidthDp >= 600
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

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
        if (isTablet) {
            // pokazujemy wszystkie ekrany jednocześnie
            if (isLandscape) {
                Row(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f).fillMaxSize()) {
                        HomeScreen(viewModel)
                    }
                    Box(Modifier.weight(0.5f).fillMaxSize()) {
                        FavouriteScreen(viewModel, navController, isTablet = true)
                    }
                    Box(Modifier.weight(0.3f).fillMaxSize()) {
                        SettingsScreen(viewModel, isTablet = true)
                    }
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f).fillMaxSize()) {
                        HomeScreen(viewModel)
                    }
                    Box(Modifier.weight(0.5f).fillMaxSize()) {
                        FavouriteScreen(viewModel, navController, isTablet = true)
                    }
                    Box(Modifier.weight(0.3f).fillMaxSize()) {
                        SettingsScreen(viewModel, isTablet = true)
                    }
                }
            }
        } else {
            // klasyczna nawigacja - niezależnie od orientacji
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(viewModel)
                        }
                        composable("favourite") {
                            FavouriteScreen(viewModel, navController)
                        }
                        composable("settings") {
                            SettingsScreen(viewModel)
                        }
                        composable(
                            route = "home/{lat}/{lon}/{city}/{country}",
                            arguments = listOf(
                                navArgument("lat") { type = NavType.StringType },
                                navArgument("lon") { type = NavType.StringType },
                                navArgument("city") { type = NavType.StringType },
                                navArgument("country") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            HomeScreen(viewModel = viewModel, backStackEntry = backStackEntry)
                        }
                    }
                }
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: ""
                NavigationBar(
                    containerColor = DarkPurple,
                    contentColor = Color.White,
                ) {
                    NavigationBarItem(
                        selected = currentRoute.startsWith("home"),
                        onClick = {
                            if (!currentRoute.startsWith("home")) navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        },
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
                        onClick = {
                            if (currentRoute != "favourite") navController.navigate("favourite") {
                                popUpTo("home")
                            }
                        },
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
                        onClick = {
                            if (currentRoute != "settings") navController.navigate("settings") {
                                popUpTo("home")
                            }
                        },
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
}
