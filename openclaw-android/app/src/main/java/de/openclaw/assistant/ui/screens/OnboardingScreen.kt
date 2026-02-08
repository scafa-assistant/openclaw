package de.openclaw.assistant.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPage(page = page)
        }
        
        // Page indicators
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(5) { index ->
                Box(
                    modifier = Modifier
                        .size(if (pagerState.currentPage == index) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                )
            }
        }
        
        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (pagerState.currentPage > 0) {
                TextButton(onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                }) {
                    Text("Zurück")
                }
            } else {
                Spacer(modifier = Modifier.width(64.dp))
            }
            
            Button(
                onClick = {
                    if (pagerState.currentPage == 4) {
                        onComplete()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }
            ) {
                Text(if (pagerState.currentPage == 4) "Los geht's" else "Weiter")
            }
        }
        
        // Skip button
        if (pagerState.currentPage < 4) {
            TextButton(
                onClick = onComplete,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Überspringen")
            }
        }
    }
}

@Composable
private fun OnboardingPage(page: Int) {
    val pages = listOf(
        OnboardingPageData(
            title = "Willkommen bei OpenClaw",
            description = "Dein AI-Assistent, der Google Assistant ersetzt und dir mit intelligenter Sprachsteuerung hilft.",
            icon = Icons.Default.Assistant,
            color = MaterialTheme.colorScheme.primary
        ),
        OnboardingPageData(
            title = "Sprachsteuerung",
            description = "Tippe auf das Mikrofon oder nutze den Widget. Dein Assistent ist immer bereit.",
            icon = Icons.Default.Mic,
            color = Color(0xFF4CAF50)
        ),
        OnboardingPageData(
            title = "Wähle dein AI-Modell",
            description = "Nutze Gemini, Claude oder GPT. Wechsle jederzeit nach deinen Bedürfnissen.",
            icon = Icons.Default.Psychology,
            color = Color(0xFF9C27B0)
        ),
        OnboardingPageData(
            title = "Ersetze deinen Assistenten",
            description = "Setze OpenClaw als Standard-Assistent ein für direkten Zugriff über die Home-Taste.",
            icon = Icons.Default.Home,
            color = Color(0xFFFF9800)
        ),
        OnboardingPageData(
            title = "Datenschutz first",
            description = "Deine Daten bleiben deine. Verschlüsselte Übertragung, keine Weitergabe.",
            icon = Icons.Default.Security,
            color = Color(0xFFF44336)
        )
    )
    
    val data = pages[page]
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = data.icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = data.color
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = data.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = data.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

private data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)
