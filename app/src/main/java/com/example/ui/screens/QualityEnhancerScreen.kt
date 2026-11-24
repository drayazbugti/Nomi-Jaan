package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.ShutterSpeed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.EnhanceMode
import com.example.model.EnhanceSegment
import com.example.model.StudioBadge
import com.example.ui.components.StudioBadgePill
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderMedium
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.SurfaceBright
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePill
import com.example.ui.theme.SurfacePillActive
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextOnWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun QualityEnhancerScreen(
    segment: EnhanceSegment,
    selectedMode: EnhanceMode,
    splitPosition: Float,
    isOnline: Boolean,
    onBackClick: () -> Unit,
    onSelectSegment: (EnhanceSegment) -> Unit,
    onSelectMode: (EnhanceMode) -> Unit,
    onSplitChange: (Float) -> Unit,
    onTryNowClick: () -> Unit,
    onBatchEnhanceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleModes = EnhanceMode.entries.filter { it.segment == segment && it != EnhanceMode.NONE }
    val demoResId = if (selectedMode == EnhanceMode.PORTRAIT) {
        R.drawable.img_demo_portrait
    } else if (selectedMode == EnhanceMode.NIGHT_DENOISE) {
        R.drawable.img_showcase_cinema
    } else {
        R.drawable.img_demo_skyline
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1D1815), BackgroundDark, BackgroundDark)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("qe_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = SurfaceElevated,
                    border = BorderStroke(1.dp, Color(0xFF4ADBC8).copy(alpha = 0.45f))
                ) {
                    Text(
                        text = "Offline 4K AI",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4ADBC8),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                // Title with sparkle symbols matching Screenshot 5
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "✦", color = TextPrimary, fontSize = 15.sp)
                    Text(
                        text = stringResource(R.string.quality_enhancer_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(text = "✦", color = TextPrimary, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = selectedMode.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 1:1 Interactive Before / After Demo Card
                BeforeAfterDemoCard(
                    resId = demoResId,
                    splitPosition = splitPosition,
                    modeLabel = selectedMode.title,
                    onSplitChange = onSplitChange
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Segmented Pill (General | Scenario)
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = SurfacePill,
                    modifier = Modifier
                        .width(244.dp)
                        .height(42.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    ) {
                        EnhanceSegment.entries.forEach { seg ->
                            val isActive = seg == segment
                            val bg by animateColorAsState(
                                targetValue = if (isActive) SurfacePillActive else Color.Transparent,
                                label = "segment_bg"
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(bg)
                                    .clickable { onSelectSegment(seg) }
                                    .testTag("qe_segment_${seg.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (seg == EnhanceSegment.GENERAL) {
                                        stringResource(R.string.segment_general)
                                    } else {
                                        stringResource(R.string.segment_scenario)
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isActive) TextPrimary else TextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // 4 Mode Tiles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    visibleModes.forEach { mode ->
                        QualityModeTile(
                            mode = mode,
                            selected = mode == selectedMode,
                            onClick = { onSelectMode(mode) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Primary "Try now" + "Batch Enhance" CTAs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = onBatchEnhanceClick,
                    modifier = Modifier
                        .weight(0.95f)
                        .height(54.dp)
                        .testTag("qe_batch_enhance_button"),
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFF282046),
                    border = BorderStroke(1.dp, Color(0xFF8B6EFF))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Batch Photos",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Button(
                    onClick = onTryNowClick,
                    modifier = Modifier
                        .weight(1.15f)
                        .height(54.dp)
                        .testTag("qe_try_now_button"),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceBright,
                        contentColor = TextOnWhite
                    )
                ) {
                    Text(
                        text = stringResource(R.string.cta_try_now),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextOnWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun BeforeAfterDemoCard(
    resId: Int,
    splitPosition: Float,
    modeLabel: String,
    onSplitChange: (Float) -> Unit
) {
    val mutedMatrix = ColorMatrix().apply {
        setToSaturation(0.75f)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(22.dp))
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    val fraction = change.position.x / size.width.toFloat()
                    onSplitChange(fraction)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onSplitChange(offset.x / size.width.toFloat())
                }
            }
            .testTag("qe_demo_compare_card")
    ) {
        val fullWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val splitWidthDp = maxWidth * splitPosition

        // Base layer: Enhanced "After" crisp image
        Image(
            painter = painterResource(id = resId),
            contentDescription = "Enhanced $modeLabel preview",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Left clipped layer: Soft / noisy "Before" simulation
        Box(
            modifier = Modifier
                .width(splitWidthDp)
                .fillMaxHeight()
                .clipToBounds()
        ) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = "Original before enhancement",
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(mutedMatrix),
                modifier = Modifier
                    .width(this@BoxWithConstraints.maxWidth)
                    .fillMaxHeight()
                    .blur(2.5.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x28000000))
            )
        }

        // Divider vertical line + drag handle
        Box(
            modifier = Modifier
                .offset { IntOffset((fullWidthPx * splitPosition).roundToInt() - 1, 0) }
                .width(2.dp)
                .fillMaxHeight()
                .background(Color.White.copy(alpha = 0.85f))
        )

        Surface(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset { IntOffset((fullWidthPx * splitPosition).roundToInt() - 36, 0) }
                .size(32.dp),
            shape = CircleShape,
            color = Color(0xCC121112),
            border = BorderStroke(1.5.dp, Color.White)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Compare,
                    contentDescription = "Drag to compare before and after",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Before / After corner pills
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            shape = RoundedCornerShape(999.dp),
            color = Color(0x99121112)
        ) {
            Text(
                text = "Before",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            shape = RoundedCornerShape(999.dp),
            color = Color(0xBB121112),
            border = BorderStroke(1.dp, BorderMedium)
        ) {
            Text(
                text = modeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(999.dp),
            color = Color(0xB8121112),
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Text(
                text = "Drag slider to compare",
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
private fun QualityModeTile(
    mode: EnhanceMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val boxBg by animateColorAsState(
        targetValue = if (selected) SurfaceBright else SurfaceElevated,
        label = "mode_box_bg"
    )
    val contentTint by animateColorAsState(
        targetValue = if (selected) TextOnWhite else TextSecondary,
        label = "mode_tint"
    )

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag("qe_mode_${mode.name.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(68.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(boxBg)
                    .border(
                        width = 1.dp,
                        color = if (selected) SurfaceBright else BorderSubtle,
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (mode) {
                    EnhanceMode.HD -> Icon(Icons.Filled.Hd, contentDescription = mode.title, tint = contentTint, modifier = Modifier.size(30.dp))
                    EnhanceMode.ULTRA_HD -> Icon(Icons.Filled.HighQuality, contentDescription = mode.title, tint = contentTint, modifier = Modifier.size(30.dp))
                    EnhanceMode.PORTRAIT -> Icon(Icons.Filled.Face, contentDescription = mode.title, tint = contentTint, modifier = Modifier.size(28.dp))
                    EnhanceMode.AI_UHD -> Icon(Icons.Filled.AutoAwesome, contentDescription = mode.title, tint = contentTint, modifier = Modifier.size(28.dp))
                    EnhanceMode.NIGHT_DENOISE -> Icon(Icons.Filled.NightlightRound, contentDescription = mode.title, tint = contentTint, modifier = Modifier.size(26.dp))
                    EnhanceMode.ANIME_4K -> Icon(Icons.Filled.HighQuality, contentDescription = mode.title, tint = contentTint, modifier = Modifier.size(30.dp))
                    EnhanceMode.OLD_PHOTO -> Icon(Icons.Filled.Restore, contentDescription = mode.title, tint = contentTint, modifier = Modifier.size(26.dp))
                    EnhanceMode.MOTION_FIX -> Icon(Icons.Filled.ShutterSpeed, contentDescription = mode.title, tint = contentTint, modifier = Modifier.size(26.dp))
                    EnhanceMode.NONE -> Unit
                }
            }

            if (mode.badge != StudioBadge.NONE) {
                StudioBadgePill(
                    badge = mode.badge,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = mode.title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) TextPrimary else TextMuted,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
