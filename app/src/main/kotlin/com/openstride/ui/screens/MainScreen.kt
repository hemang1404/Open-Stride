package com.openstride.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.CameraUpdateFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.openstride.data.model.TrackPoint
import com.openstride.ui.theme.*
import com.openstride.ui.viewmodel.TrackingViewModel
import java.util.Locale
import android.content.pm.PackageManager

@Composable
fun MainScreen(viewModel: TrackingViewModel = viewModel()) {
    val context = LocalContext.current
    val isTracking by viewModel.isTracking.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val distance by viewModel.currentDistance.collectAsState()
    val points by viewModel.sessionPoints.collectAsState()
    val hasPermission by viewModel.hasLocationPermission.collectAsState()
    val needsBackgroundPermission by viewModel.needsBackgroundPermission.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        viewModel.setHasPermission(granted)
        if (!granted) {
            viewModel.clearError() // Will show new error when trying to start
        }
    }

    // Background permission launcher (Android 10+)
    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setBackgroundPermissionGranted()
        }
        viewModel.clearError()
    }

    // Check permission on first composition
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.setHasPermission(granted)
    }

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

        // Map Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            MapLibreView(points = points)
            
            // Floating Stats (Strava style)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = String.format(Locale.US, "%.2f km", distance / 1000),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = StravaTextMain
                        )
                        Row {
                            StatItemSmall(label = "TIME", value = formatTime(timerSeconds))
                            Spacer(modifier = Modifier.width(16.dp))
                            StatItemSmall(label = "PACE", value = calculatePace(timerSeconds, distance))
                        }
                    }
                }
            }
        }

        // Error Message Snackbar
        errorMessage?.let { message ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    if (!hasPermission) {
                        TextButton(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                                viewModel.clearError()
                            }
                        ) {
                            Text("GRANT", color = Color.White)
                        }
                    } else if (needsBackgroundPermission && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        TextButton(
                            onClick = {
                                backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            }
                        ) {
                            Text("ALLOW", color = Color.White)
                        }
                    } else {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK", color = Color.White)
                        }
                    }
                },
                dismissAction = {
                    IconButton(onClick = { viewModel.clearError() }) {
                        Text("✕", color = Color.White)
                    }
                }
            ) {
                Text(message)
            }
        }

        // Bottom Action Area
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isTracking) {
                // Pause/Resume Button
                Button(
                    onClick = { viewModel.togglePause() },
                    modifier = Modifier.size(70.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaused) StravaOrange else StravaDark
                    )
                ) {
                    Text(
                        text = if (isPaused) "▶" else "⏸",
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(24.dp))
                
                // Stop Button
                Button(
                    onClick = { viewModel.toggleTracking() },
                    modifier = Modifier.size(70.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("■", fontSize = 24.sp, color = Color.White)
                }
            } else {
                // Big Start Button
                Button(
                    onClick = { viewModel.toggleTracking() },
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = StravaOrange)
                ) {
                    Text("START", fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun MapLibreView(points: List<TrackPoint>) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.default.copy(
            target = if (points.isNotEmpty()) 
                com.google.android.gms.maps.model.LatLng(points[0].latitude, points[0].longitude)
            else
                com.google.android.gms.maps.model.LatLng(0.0, 0.0),
            zoom = 15f
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        if (points.size > 1) {
            // Draw polyline for the route
            Polyline(
                points = points.map { 
                    com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude) 
                },
                color = StravaOrange,
                width = 5f
            )

            // Add marker at start
            Marker(
                state = MarkerState(
                    position = com.google.android.gms.maps.model.LatLng(
                        points[0].latitude, 
                        points[0].longitude
                    )
                ),
                title = "Start"
            )

            // Add marker at end
            Marker(
                state = MarkerState(
                    position = com.google.android.gms.maps.model.LatLng(
                        points.last().latitude, 
                        points.last().longitude
                    )
                ),
                title = "End"
            )

            // Center camera on the route
            LaunchedEffect(points.size) {
                if (points.size > 1) {
                    val bounds = com.google.android.gms.maps.model.LatLngBounds.Builder()
                        .apply {
                            points.forEach { 
                                include(com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude))
                            }
                        }
                        .build()
                    
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngBounds(bounds, 100),
                        durationMs = 1000
                    )
                }
            }
        }
    }
}

/**
 * Extension to convert Compose Color to Android Int Color
 */
fun Color.toArgb(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(),
    (red * 255).toInt(),
    (green * 255).toInt(),
    (blue * 255).toInt()
)

@Composable
fun StatItemSmall(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = StravaTextSecondary
        )
        Text(
            text = value,
            fontSize = 16.sp,
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
