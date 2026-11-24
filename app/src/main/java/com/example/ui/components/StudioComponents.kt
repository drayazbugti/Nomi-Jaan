package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FaceRetouchingNatural
import androidx.compose.material.icons.filled.FilterBAndW
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NoiseAware
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.PhotoFilter
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TagFaces
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VideoStable
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Vignette
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.BlurOn
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.ExportResolution
import com.example.model.ExportStatus
import com.example.model.StudioBadge
import com.example.model.StudioCatalog
import com.example.model.StudioToolId
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.BadgeHot
import com.example.ui.theme.BadgeNew
import com.example.ui.theme.BadgePro
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SurfaceBright
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePill
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextOnWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StudioBadgePill(
    badge: StudioBadge,
    modifier: Modifier = Modifier
) {
    if (badge == StudioBadge.NONE) return
    val bgColor = when (badge) {
        StudioBadge.HOT -> BadgeHot
        StudioBadge.NEW -> BadgeNew
        StudioBadge.PRO -> BadgePro
        StudioBadge.NONE -> Color.Transparent
    }
    val textColor = if (badge == StudioBadge.PRO) TextOnWhite else TextPrimary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomEnd = 8.dp, bottomStart = 2.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = badge.label,
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 11.sp
        )
    }
}

fun toolIconFor(id: StudioToolId): ImageVector = when (id) {
    StudioToolId.QUALITY_ENHANCER -> Icons.Filled.Hd
    StudioToolId.SMART_ENHANCE -> Icons.Filled.AutoAwesome
    StudioToolId.SUPER_RESOLUTION -> Icons.Filled.HighQuality
    StudioToolId.BATCH_ENHANCE -> Icons.Filled.Layers
    StudioToolId.AI_REMOVAL -> Icons.Filled.AutoFixHigh
    StudioToolId.FACE -> Icons.Filled.Face
    StudioToolId.BODY -> Icons.Outlined.AccessibilityNew
    StudioToolId.AI_RETOUCH -> Icons.Filled.FaceRetouchingNatural
    StudioToolId.COLOR_MATCH -> Icons.Filled.Palette
    StudioToolId.HAIR -> Icons.Filled.Brush
    StudioToolId.MAKEUP -> Icons.Filled.AutoFixHigh
    StudioToolId.ALL_TOOLS -> Icons.Filled.GridView
    StudioToolId.EDIT -> Icons.Filled.ContentCut
    StudioToolId.FILTERS -> Icons.Filled.FilterBAndW
    StudioToolId.ADJUST -> Icons.Filled.Tune
    StudioToolId.ENHANCE -> Icons.Filled.HighQuality
    StudioToolId.AI_EDIT -> Icons.Filled.AutoAwesome
    StudioToolId.EFFECTS -> Icons.Filled.PhotoFilter
    StudioToolId.TEXT -> Icons.Filled.TextFields
    StudioToolId.MOSAIC -> Icons.Outlined.BlurOn
    StudioToolId.REMOVAL -> Icons.Outlined.CleaningServices
    StudioToolId.OVERLAY -> Icons.Filled.Layers
    StudioToolId.ANIMATE -> Icons.Filled.PlayCircleOutline
    StudioToolId.CUTOUT -> Icons.Filled.PersonRemove
    StudioToolId.CHROMA -> Icons.Filled.Colorize
    StudioToolId.FREEZE -> Icons.Filled.AcUnit
    StudioToolId.REPLACE -> Icons.Filled.SwapHoriz
    StudioToolId.CAMERA_TRACKING -> Icons.Filled.CenterFocusStrong
    StudioToolId.VOCAL_SEPARATION -> Icons.Filled.RecordVoiceOver
    StudioToolId.NOISE_REDUCTION -> Icons.Filled.NoiseAware
    StudioToolId.DETECT_SILENCE -> Icons.Filled.GraphicEq
    StudioToolId.SEPARATE_AUDIO -> Icons.Filled.Mic
    StudioToolId.SOUND -> Icons.Filled.MusicNote
    StudioToolId.COPY -> Icons.Filled.ContentCopy
    StudioToolId.TRIM -> Icons.Filled.Crop
    StudioToolId.SPLIT -> Icons.Filled.ContentCut
    StudioToolId.MASK -> Icons.Filled.Vignette
    StudioToolId.STABILIZE -> Icons.Filled.VideoStable
    StudioToolId.CUT -> Icons.Filled.ContentCut
    StudioToolId.SPEED -> Icons.Filled.Speed
    StudioToolId.ANIMATION -> Icons.Filled.Animation
    StudioToolId.VOLUME -> Icons.Filled.VolumeUp
    StudioToolId.DELETE -> Icons.Filled.DeleteOutline
    StudioToolId.VIDEO_QUALITY -> Icons.Filled.Hd
    StudioToolId.FADE -> Icons.Filled.Gradient
    StudioToolId.POSITION -> Icons.Filled.OpenWith
    StudioToolId.SIZE -> Icons.Filled.PhotoSizeSelectLarge
    StudioToolId.MIRROR -> Icons.Filled.Flip
    StudioToolId.FLIP -> Icons.Filled.SwapVert
    StudioToolId.FIT -> Icons.Filled.FitScreen
    StudioToolId.AUDIO -> Icons.Filled.MusicNote
    StudioToolId.STICKERS -> Icons.Filled.TagFaces
    StudioToolId.FRAMES -> Icons.Filled.CropFree
    StudioToolId.MAGNIFIER -> Icons.Filled.ZoomIn
    StudioToolId.CANVAS -> Icons.Filled.AspectRatio
    StudioToolId.CROP -> Icons.Filled.Crop
    StudioToolId.ROTATE -> Icons.Filled.RotateRight
    StudioToolId.BG_REMOVER -> Icons.Filled.PersonRemove
    StudioToolId.TRANSITIONS -> Icons.Filled.MovieFilter
    StudioToolId.TEMPLATE -> Icons.Filled.ViewCarousel
    StudioToolId.SKIN_SMOOTH -> Icons.Filled.FaceRetouchingNatural
    StudioToolId.BRIGHTEN -> Icons.Filled.WbSunny
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPickSheet(
    title: String,
    subtitle: String,
    onDismiss: () -> Unit,
    onPickPhoto: () -> Unit,
    onPickVideo: () -> Unit,
    onSelectSample: (resId: Int, title: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceCard,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    onClick = {
                        onDismiss()
                        onPickPhoto()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                        .testTag("pick_photo_button"),
                    shape = RoundedCornerShape(18.dp),
                    color = SurfaceBright,
                    contentColor = TextOnWhite
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = "Pick photo from gallery",
                            tint = TextOnWhite
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Pick Photo",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextOnWhite
                        )
                    }
                }

                Surface(
                    onClick = {
                        onDismiss()
                        onPickVideo()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                        .testTag("pick_video_button"),
                    shape = RoundedCornerShape(18.dp),
                    color = SurfaceElevated,
                    border = BorderStroke(1.dp, BorderSubtle),
                    contentColor = TextPrimary
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VideoLibrary,
                            contentDescription = "Pick video from gallery",
                            tint = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Pick Video",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary
                        )
                    }
                }
            }

            Text(
                text = "Or start instantly with a Studio Sample",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SampleThumbTile(
                    resId = R.drawable.img_demo_skyline,
                    label = "Skyline",
                    modifier = Modifier.weight(1f),
                    testTag = "sample_skyline_btn",
                    onClick = {
                        onDismiss()
                        onSelectSample(R.drawable.img_demo_skyline, "Skyline")
                    }
                )
                SampleThumbTile(
                    resId = R.drawable.img_demo_portrait,
                    label = "Portrait",
                    modifier = Modifier.weight(1f),
                    testTag = "sample_portrait_btn",
                    onClick = {
                        onDismiss()
                        onSelectSample(R.drawable.img_demo_portrait, "Portrait")
                    }
                )
                SampleThumbTile(
                    resId = R.drawable.img_showcase_cinema,
                    label = "Neon Night",
                    modifier = Modifier.weight(1f),
                    testTag = "sample_cinema_btn",
                    onClick = {
                        onDismiss()
                        onSelectSample(R.drawable.img_showcase_cinema, "Neon Night")
                    }
                )
            }
        }
    }
}

@Composable
private fun SampleThumbTile(
    resId: Int,
    label: String,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(96.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceElevated,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Box {
            Image(
                painter = painterResource(id = resId),
                contentDescription = "$label sample",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xCC121112))
                        )
                    )
                    .padding(vertical = 6.dp, horizontal = 8.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllToolsSheet(
    initialQuery: String,
    onDismiss: () -> Unit,
    onSelectTool: (StudioToolId) -> Unit
) {
    var query by remember(initialQuery) { mutableStateOf(initialQuery) }
    val filtered = remember(query) {
        val q = query.trim()
        if (q.isEmpty()) StudioCatalog.allSearchableTools
        else StudioCatalog.allSearchableTools.filter {
            it.label.contains(q, ignoreCase = true) || it.category.contains(q, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bugti Editz Tool Suite",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close tools sheet", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search tools (Enhance, 4K, Retouch, Mosaic...)", color = TextMuted) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(999.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceElevated,
                    unfocusedContainerColor = SurfaceElevated,
                    focusedBorderColor = Color.White.copy(alpha = 0.25f),
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("all_tools_search_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(340.dp)
            ) {
                items(filtered, key = { it.name }) { tool ->
                    Surface(
                        onClick = {
                            onDismiss()
                            onSelectTool(tool)
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceElevated,
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("catalog_tool_${tool.name.lowercase()}")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = toolIconFor(tool),
                                contentDescription = tool.label,
                                tint = TextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = tool.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = tool.category,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProSuiteSheet(
    currentResolution: ExportResolution,
    onSelectResolution: (ExportResolution) -> Unit,
    onLaunch4kEnhancer: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFB57BFF), Color(0xFFFF6B4A)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Diamond, contentDescription = null, tint = TextPrimary)
                }
                Column {
                    Text(
                        text = "Bugti Editz Pro Engine",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "4K UHD export & multi-pass studio sharpening",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Text(
                text = "Default Export Resolution",
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExportResolution.entries.forEach { res ->
                    val selected = res == currentResolution
                    Surface(
                        onClick = { onSelectResolution(res) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) SurfaceBright else SurfaceElevated,
                        border = BorderStroke(1.dp, if (selected) SurfaceBright else BorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = res.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) TextOnWhite else TextPrimary
                            )
                            Text(
                                text = "${res.maxDim}px max",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (selected) TextOnWhite.copy(alpha = 0.7f) else TextMuted
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onDismiss()
                    onLaunch4kEnhancer()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceBright,
                    contentColor = TextOnWhite
                )
            ) {
                Text("Open Quality Enhancer", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ExportStatusDialog(
    status: ExportStatus,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    if (status == ExportStatus.Idle) return
    val context = LocalContext.current

    Dialog(onDismissRequest = {
        if (status !is ExportStatus.Processing) onDismiss()
    }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceCard,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (status) {
                    ExportStatus.Idle -> Unit
                    is ExportStatus.Processing -> {
                        val animatedProgress by animateFloatAsState(
                            targetValue = status.progress.coerceIn(0f, 1f),
                            animationSpec = tween(durationMillis = 240),
                            label = "studio_smooth_progress"
                        )
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF8B6EFF),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = status.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = status.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .testTag("studio_progress_bar"),
                            color = Color(0xFF8B6EFF),
                            trackColor = SurfacePill
                        )
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.testTag("studio_progress_percent")
                        )
                        TextButton(onClick = onCancel) {
                            Text("Cancel", color = TextSecondary)
                        }
                    }
                    is ExportStatus.Success -> {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Export succeeded",
                            tint = AccentTeal,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = if (status.isAudio) "Audio Saved!" else "Saved to Gallery!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = status.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = when {
                                            status.isAudio -> "audio/wav"
                                            status.isVideo -> "video/mp4"
                                            else -> "image/jpeg"
                                        }
                                        putExtra(Intent.EXTRA_STREAM, status.outputUri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(999.dp),
                                border = BorderStroke(1.dp, BorderSubtle)
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share", color = TextPrimary)
                            }
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SurfaceBright,
                                    contentColor = TextOnWhite
                                )
                            ) {
                                Text("Done", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    is ExportStatus.Error -> {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Export error",
                            tint = ErrorRed,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = status.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = status.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceBright,
                                contentColor = TextOnWhite
                            )
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}
