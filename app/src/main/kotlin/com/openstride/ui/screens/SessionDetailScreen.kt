package com.openstride.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.openstride.data.model.TrackPoint
import com.openstride.ui.theme.*
import com.openstride.ui.viewmodel.TrackingViewModel
import java.util.*

@Composable
fun SessionDetailScreen(sessionId: String, viewModel: TrackingViewModel = viewModel()) {
    // In a real app, we'd fetch the session and its points from the DB using the sessionId
    // Fetching session details once from repository
    val session by produceState<Session?>(initialValue = null) {
        value = viewModel.getSessionById(sessionId)
    }
    val points by produceState<List<TrackPoint>>(initialValue = emptyList()) {
        viewModel.getPointsForSession(sessionId).collect { value = it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StravaGrey)
    ) {
        // App Header
        Surface(shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(StravaOrange),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ACTIVITY DETAIL",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }

        session?.let { s ->
            // Map Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.LightGray)
            ) {
                MapLibreView(points = points)
            }

            // High Level Stats
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = StravaLight),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = s.mode.replace("_", " "),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StravaOrange
                    )
                    Text(
                        text = "Afternoon Walk", // Title can be dynamic later
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = StravaTextMain
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailStat("DISTANCE", String.format(Locale.US, "%.2f km", s.totalDistance / 1000.0))
                        DetailStat("PACE", calculatePaceForDetail(s))
                        DetailStat("TIME", formatDurationForDetail(s))
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    ConfidenceIndicator(s.confidenceScore)
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = StravaOrange)
        }
    }
}

@Composable
fun DetailStat(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 10.sp, color = StravaTextSecondary, fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 18.sp, color = StravaTextMain, fontWeight = FontWeight.Black)
    }
}

@Composable
fun ConfidenceIndicator(score: Int) {
    val color = when {
        score >= 80 -> Color(0xFF4CAF50) // Green
        score >= 50 -> Color(0xFFFFC107) // Amber
        else -> Color(0xFFF44336) // Red
    }
    
    val text = when {
        score >= 80 -> "HIGH CONFIDENCE"
        score >= 50 -> "MEDIUM CONFIDENCE"
        else -> "LOW CONFIDENCE"
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$text ($score%)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
    Text(
        text = "This score reflects GPS signal quality during your walk.",
        fontSize = 10.sp,
        color = StravaTextSecondary,
        modifier = Modifier.padding(top = 4.dp)
    )
}

private fun formatDurationForDetail(session: Session): String {
    val end = session.endTime ?: return "--"
    val seconds = (end - session.startTime) / 1000
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
}

private fun calculatePaceForDetail(session: Session): String {
    if (session.totalDistance < 10.0 || session.endTime == null) return "--"
    val seconds = (session.endTime - session.startTime) / 1000
    val km = session.totalDistance / 1000.0
    val paceSeconds = (seconds / km).toLong()
    val m = paceSeconds / 60
    val s = paceSeconds % 60
    return String.format("%d:%02d /km", m, s)
}
