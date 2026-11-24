package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.OutfitFontFamily
import kotlinx.coroutines.delay

val CyberCyan = Color(0xFF00E5FF)
val CyberPurple = Color(0xFF7C4DFF)
val NetOnlineGreen = Color(0xFF00E676)
val NetOfflineRed = Color(0xFFFF4D6D)

@Composable
fun BugtiSplashOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnim = true }

    val progress by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
        label = "splash_progress"
    )

    val infinite = rememberInfiniteTransition(label = "splash_ring")
    val ringAngle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    AnimatedVisibility(
        visible = visible,
        exit = fadeOut(animationSpec = tween(450)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF141E3C), Color(0xFF04060B)),
                        radius = 1200f
                    )
                )
                .testTag("bugti_splash_screen"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(CyberCyan, CyberPurple, CyberCyan)
                            )
                        )
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(25.dp))
                            .background(Color(0xFF070B14)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(78.dp)
                                .rotate(ringAngle)
                                .border(
                                    width = 1.5.dp,
                                    brush = Brush.sweepGradient(
                                        listOf(
                                            CyberCyan.copy(alpha = 0.65f),
                                            Color.Transparent,
                                            CyberPurple.copy(alpha = 0.65f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                        Text(
                            text = "BE",
                            fontFamily = OutfitFontFamily,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = CyberCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "BUGTI EDITZ",
                    style = TextStyle(
                        fontFamily = OutfitFontFamily,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 5.sp,
                        brush = Brush.linearGradient(
                            colors = listOf(Color.White, CyberCyan, Color(0xFFB388FF)),
                            start = Offset.Zero,
                            end = Offset(600f, 0f)
                        )
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "CLOUD BACKEND & LIVE SYNC",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 3.sp,
                        fontSize = 11.sp
                    ),
                    color = Color(0xFF93A0C2),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(26.dp))

                Box(
                    modifier = Modifier
                        .width(184.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(999.dp))
                            .background(Brush.horizontalGradient(listOf(CyberCyan, CyberPurple)))
                    )
                }
            }
        }
    }
}

@Composable
fun NetworkStatusPill(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = if (isOnline) NetOnlineGreen.copy(alpha = 0.14f) else NetOfflineRed.copy(alpha = 0.15f)
    val fg = if (isOnline) NetOnlineGreen else NetOfflineRed
    val border = if (isOnline) NetOnlineGreen.copy(alpha = 0.35f) else NetOfflineRed.copy(alpha = 0.45f)

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
        modifier = modifier.testTag("network_status_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Text(
                text = if (isOnline) "Online • Connected" else "Offline • No Internet",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = fg
            )
        }
    }
}

@Composable
fun NoInternetPopupDialog(
    onRetryClick: () -> Unit,
    onContinueOffline: () -> Unit
) {
    Dialog(onDismissRequest = onContinueOffline) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF101626),
            border = BorderStroke(1.dp, NetOfflineRed.copy(alpha = 0.5f)),
            shadowElevation = 16.dp,
            modifier = Modifier
                .widthIn(max = 340.dp)
                .fillMaxWidth()
                .testTag("no_internet_popup")
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(NetOfflineRed.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "!",
                        color = NetOfflineRed,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "No Internet Connection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Please check the internet connection",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF93A0C2),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onRetryClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NetOfflineRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("retry_connection_button")
                ) {
                    Text(
                        text = "Retry Connection",
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = onContinueOffline,
                    modifier = Modifier.testTag("dismiss_offline_popup_button")
                ) {
                    Text(
                        text = "Use Local Studio Offline",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF93A0C2)
                    )
                }
            }
        }
    }
}
