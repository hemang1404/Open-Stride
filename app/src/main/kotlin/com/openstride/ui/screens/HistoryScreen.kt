package com.openstride.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.openstride.data.model.Session
import com.openstride.ui.theme.*
import com.openstride.ui.viewmodel.TrackingViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(viewModel: TrackingViewModel = viewModel()) {
    // We'll need a new ViewModel or update TrackingViewModel to fetch all sessions
    // For now, let's assume TrackingViewModel can provide them
    val sessions by viewModel.allSessions.collectAsState(initial = emptyList())

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
                    text = "ACTIVITIES",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }

        if (sessions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No activities yet. Go for a walk!", color = StravaTextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sessions) { session ->
                    SessionCard(session)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionCard(session: Session) {
    Card(
        onClick = { /* Navigate to detail */ },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StravaLight),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = formatDate(session.startTime),
                fontSize = 12.sp,
                color = StravaTextSecondary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Afternoon Walk", // Placeholder for dynamic title
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = StravaTextMain
            )
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = StravaGrey)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItemSmall("DISTANCE", String.format(Locale.US, "%.2f km", session.totalDistance / 1000.0))
                StatItemSmall("TIME", formatDuration(session.startTime, session.endTime))
                StatItemSmall("AVG PACE", calculateAvgPace(session))
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp)).toUpperCase(Locale.getDefault())
}

private fun formatDuration(start: Long, end: Long?): String {
    if (end == null) return "--"
    val seconds = (end - start) / 1000
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
}

private fun calculateAvgPace(session: Session): String {
    if (session.totalDistance < 10.0 || session.endTime == null) return "--"
    val seconds = (session.endTime - session.startTime) / 1000
    val km = session.totalDistance / 1000.0
    val paceSeconds = (seconds / km).toLong()
    val m = paceSeconds / 60
    val s = paceSeconds % 60
    return String.format("%d:%02d /km", m, s)
}
