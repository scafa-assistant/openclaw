package de.openclaw.assistant.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    // Reduziert auf 3 Screens statt 5
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    
    val pages = listOf(
        OnboardingPageData(
            title = "Dein AI-Assistent",
            description = "Sprich natürlich mit OpenClaw. Er versteht dich und hilft dir bei allem.",
            icon = Icons.Default.Mic,
            color = MaterialTheme.colorScheme.primary
        ),
        OnboardingPageData(
            title = "Wähle dein Modell",
            description = "Nutze Gemini, Claude oder GPT. Wechsle jederzeit nach deinen Bedürfnissen.",
            icon = Icons.Default.Psychology,
            color = MaterialTheme.colorScheme.secondary
        ),
        OnboardingPageData(
            title = "Datenschutz first",
            description = "Deine Daten bleiben deine. Verschlüsselt und sicher gespeichert.",
            icon = Icons.Default.Security,
            color = MaterialTheme.colorScheme.tertiary
        )
    )
    
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skip button (oben rechts)
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onComplete,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text("Überspringen")
                }
            }
            
            Spacer(modifier = Modifier.weight(0.5f))
            
            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(3f)
            ) { page ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Icon
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(MaterialTheme.shapes.large)
                            .background(pages[page].color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = pages[page].icon,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = pages[page].color
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Title
                    Text(
                        text = pages[page].title,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Description
                    Text(
                        text = pages[page].description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(0.5f))
            
            // Page indicators
            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .width(if (pagerState.currentPage == index) 24.dp else 8.dp)
                            .height(8.dp)
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
            
            // Action button
            Button(
                onClick = {
                    if (pagerState.currentPage == 2) {
                        onComplete()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    if (pagerState.currentPage == 2) "Los geht's"
                    else "Weiter",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: androidx.compose.ui.graphics.Color
)
