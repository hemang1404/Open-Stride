package com.openstride.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openstride.ui.theme.*

@Composable
fun PermissionRationaleScreen(onGrantClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StravaGrey)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = StravaOrange,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Track Your Adventures",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = StravaTextMain,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "OpenStride uses your location to record your path and calculate distance, even when the app is in the background or the screen is off.",
            fontSize = 16.sp,
            color = StravaTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onGrantClick,
            colors = ButtonDefaults.buttonColors(containerColor = StravaOrange),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(
                "GRANT PERMISSION",
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
