package pl.juhas.weatherapp.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.juhas.weatherapp.WeatherViewModel
import pl.juhas.weatherapp.ui.theme.DarkPurple
import pl.juhas.weatherapp.ui.theme.LightPurple
import pl.juhas.weatherapp.ui.theme.Purple

@Composable
fun SettingsScreen(viewModel: WeatherViewModel, isTablet: Boolean = false) {
    val unitMap = mapOf(
        "Metric (°C)" to "metric",
        "Standard (°K)" to "standard",
        "Imperial (°F)" to "imperial"
    )
    val initialSelectedKey = unitMap.entries.find { it.value == viewModel.unit }?.key ?: "Metric (°C)"
    var selectedUnit by remember { mutableStateOf(initialSelectedKey) }
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.screenHeightDp > configuration.screenWidthDp

    LazyColumn(
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
            .padding(top = 20.dp , start = 16.dp, end = 16.dp, bottom = 16.dp)
            .then(if (isPortrait) Modifier.padding(top = 30.dp) else Modifier),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isTablet && isPortrait) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Select Units",
                            color = Color.White,
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ToggleButtonGroup(
                            options = unitMap.keys.toList(),
                            selectedOption = selectedUnit,
                            onOptionSelected = { selectedKey ->
                                selectedUnit = selectedKey
                                viewModel.updateUnit(unitMap[selectedKey] ?: "metric")
                            }
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Refresh interval",
                            color = Color.White,
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        val intervals = listOf(0, 15, 30, 60)
                        var selectedInterval by remember { mutableIntStateOf(viewModel.refreshInterval) }
                        if (!isPortrait) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                intervals.forEach { interval ->
                                    val isSelected = selectedInterval == interval
                                    Button(
                                        onClick = {
                                            selectedInterval = interval
                                            viewModel.updateRefreshInterval(interval)
                                        },
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) LightPurple else DarkPurple,
                                            contentColor = Color.White
                                        ),
                                        border = BorderStroke(1.dp, Color(0xFF878787)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(if (interval == 0) "Off" else "${interval}s")
                                    }
                                }
                            }
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                intervals.forEach { interval ->
                                    val isSelected = selectedInterval == interval
                                    Button(
                                        onClick = {
                                            selectedInterval = interval
                                            viewModel.updateRefreshInterval(interval)
                                        },
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) LightPurple else DarkPurple,
                                            contentColor = Color.White
                                        ),
                                        border = BorderStroke(1.dp, Color(0xFF878787)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (interval == 0) "Off" else "${interval}s")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { viewModel.updateRefreshInterval(viewModel.refreshInterval); viewModel.updateUnit(viewModel.unit) },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightPurple,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color(0xFF878787)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Refresh now")
                }
            }
        } else {
            item {
                Text(
                    text = "Select Units",
                    color = Color.White,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                ToggleButtonGroup(
                    options = unitMap.keys.toList(),
                    selectedOption = selectedUnit,
                    onOptionSelected = { selectedKey ->
                        selectedUnit = selectedKey
                        viewModel.updateUnit(unitMap[selectedKey] ?: "metric")
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Refresh interval",
                    color = Color.White,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                val intervals = listOf(0, 15, 30, 60)
                var selectedInterval by remember { mutableIntStateOf(viewModel.refreshInterval) }
                if (isTablet && !isPortrait) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        intervals.forEach { interval ->
                            val isSelected = selectedInterval == interval
                            Button(
                                onClick = {
                                    selectedInterval = interval
                                    viewModel.updateRefreshInterval(interval)
                                },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) LightPurple else DarkPurple,
                                    contentColor = Color.White
                                ),
                                border = BorderStroke(1.dp, Color(0xFF878787)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (interval == 0) "Off" else "${interval}s")
                            }
                        }
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        intervals.forEach { interval ->
                            val isSelected = selectedInterval == interval
                            Button(
                                onClick = {
                                    selectedInterval = interval
                                    viewModel.updateRefreshInterval(interval)
                                },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) LightPurple else DarkPurple,
                                    contentColor = Color.White
                                ),
                                border = BorderStroke(1.dp, Color(0xFF878787)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (interval == 0) "Off" else "${interval}s")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { viewModel.refreshWeatherData() },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Purple,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color(0xFF878787)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Refresh now")
                }
            }
        }
    }
}

@Composable
fun ToggleButtonGroup(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption

            Button(
                onClick = { onOptionSelected(option) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) LightPurple else DarkPurple,
                    contentColor = Color.White
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color(0xFF878787)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
            ) {
                Text(
                    text = option.uppercase(),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

