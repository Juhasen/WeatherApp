package pl.juhas.weatherapp.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.juhas.weatherapp.WeatherViewModel
import pl.juhas.weatherapp.ui.theme.DarkPurple
import pl.juhas.weatherapp.ui.theme.LightPurple
import pl.juhas.weatherapp.ui.theme.Purple

@Composable
fun SettingsScreen(viewModel: WeatherViewModel) {
    val unitMap = mapOf(
        "Metric (°C)" to "metric",
        "Standard (°K)" to "standard",
        "Imperial (°F)" to "imperial"
    )

    val initialSelectedKey = unitMap.entries.find { it.value == viewModel.unit }?.key ?: "Metric (°C)"
    var selectedUnit by remember { mutableStateOf(initialSelectedKey) }

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
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

        val intervals = listOf(15, 30, 60)
        var selectedInterval by remember { mutableStateOf(viewModel.refreshInterval) }
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
                    Text("${interval}s")
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

