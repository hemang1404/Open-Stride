package com.openstride.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.openstride.ui.viewmodel.TrackingViewModel
import java.util.Locale

@Composable
fun MainScreen(viewModel: TrackingViewModel = viewModel()) {
    val isTracking by viewModel.isTracking.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val distance by viewModel.currentDistance.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StravaLight)
    ) {
        // Top Header
        Surface(
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(StravaOrange),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "OPENSTRIDE",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }

        // Stats Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "DISTANCE",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = StravaTextSecondary
            )
            Text(
                text = String.format(Locale.US, "%.2f km", distance / 1000),
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                color = StravaTextMain
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "TIME", value = formatTime(timerSeconds))
                StatItem(label = "PACE", value = calculatePace(timerSeconds, distance))
            }
        }

        // Bottom Action Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { viewModel.toggleTracking() },
                modifier = Modifier
                    .size(if (isTracking) 120.dp else 160.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTracking) StravaDark else StravaOrange
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = if (isTracking) "FINISH" else "RECORD",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = StravaTextSecondary
        )
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = StravaTextMain
        )
    }
}

private fun formatTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format(Locale.US, "%d:%02d:%02d", h, m, s)
    } else {
        String.format(Locale.US, "%02d:%02d", m, s)
    }
}

private fun calculatePace(seconds: Long, distanceMeters: Double): String {
    if (distanceMeters < 10.0 || seconds == 0L) return "0:00"
    val km = distanceMeters / 1000.0
    val paceSeconds = (seconds / km).toLong()
    val m = paceSeconds / 60
    val s = paceSeconds % 60
    return String.format(Locale.US, "%d:%02d", m, s)
}
