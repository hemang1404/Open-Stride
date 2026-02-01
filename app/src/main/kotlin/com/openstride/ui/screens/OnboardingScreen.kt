package com.openstride.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.*
import com.openstride.R
import com.openstride.ui.theme.*

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState()
    val pages = listOf(
        OnboardingPage.Welcome,
        OnboardingPage.Permission,
        OnboardingPage.Features
    )

    Box(modifier = Modifier.fillMaxSize().background(StravaGrey)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Skip button
            if (pagerState.currentPage < pages.size - 1) {
                TextButton(
                    onClick = onComplete,
                    modifier = Modifier.align(Alignment.End).padding(16.dp)
                ) {
                    Text(
                        stringResource(R.string.onboarding_skip),
                        color = StravaTextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Pager
            HorizontalPager(
                count = pages.size,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(pages[page])
            }

            // Page indicator
            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                activeColor = StravaOrange,
                inactiveColor = StravaTextSecondary
            )

            // Next/Get Started button
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StravaOrange),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (pagerState.currentPage < pages.size - 1) {
                        stringResource(R.string.onboarding_next)
                    } else {
                        stringResource(R.string.onboarding_get_started)
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = page.icon,
            contentDescription = null,
            tint = StravaOrange,
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = stringResource(page.title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = StravaTextMain,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(page.description),
            fontSize = 16.sp,
            color = StravaTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

sealed class OnboardingPage(
    val icon: ImageVector,
    val title: Int,
    val description: Int
) {
    object Welcome : OnboardingPage(
        Icons.Default.DirectionsWalk,
        R.string.onboarding_welcome_title,
        R.string.onboarding_welcome_message
    )

    object Permission : OnboardingPage(
        Icons.Default.LocationOn,
        R.string.onboarding_permission_title,
        R.string.onboarding_permission_message
    )

    object Features : OnboardingPage(
        Icons.Default.Star,
        R.string.onboarding_features_title,
        R.string.onboarding_features_message
    )
}
