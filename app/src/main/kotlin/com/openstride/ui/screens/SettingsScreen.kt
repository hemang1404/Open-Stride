package com.openstride.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openstride.ui.theme.*
import com.openstride.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val isMetric by viewModel.isMetric.collectAsState()
    val interval by viewModel.trackingInterval.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StravaGrey)
    ) {
        // Header
        Surface(shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(StravaOrange),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SETTINGS",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PREFERENCES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = StravaTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Units Toggle
            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Units", fontWeight = FontWeight.Bold, color = StravaTextMain)
                        Text(
                            if (isMetric) "Metric (km)" else "Imperial (miles)",
                            fontSize = 12.sp,
                            color = StravaTextSecondary
                        )
                    }
                    Switch(
                        checked = isMetric,
                        onCheckedChange = { viewModel.toggleUnits() },
                        colors = SwitchDefaults.colors(checkedThumbColor = StravaOrange)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TRACKING LOGIC",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = StravaTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Interval Setting
            SettingsCard {
                Column {
                    Text("GPS Update Interval", fontWeight = FontWeight.Bold, color = StravaTextMain)
                    Text(
                        "${interval / 1000} second(s)",
                        fontSize = 12.sp,
                        color = StravaTextSecondary
                    )
                    Slider(
                        value = interval.toFloat(),
                        onValueChange = { viewModel.updateInterval(it.toLong()) },
                        valueRange = 1000f..5000f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = StravaOrange,
                            activeTrackColor = StravaOrange
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "OpenStride v1.0.0",
                fontSize = 12.sp,
                color = StravaTextSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
