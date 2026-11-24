package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AspectRatioOption
import com.example.model.CanvasFitMode
import com.example.model.SpeedCurvePreset
import com.example.model.SpeedModeTab
import com.example.model.StudioCatalog
import com.example.model.StudioEditState
import com.example.model.TransitionCategory
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.SurfaceBright
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePill
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextOnWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs
import kotlin.math.roundToInt

// ==================== ROTATE WITH INTERACTIVE DEGREE RULER ====================

@Composable
internal fun RotateRulerPanel(
    editState: StudioEditState,
    onRotateLeft90: () -> Unit,
    onRotate90: () -> Unit,
    onFlipHorizontal: () -> Unit,
    onFlipVertical: () -> Unit,
    onSetStraighten: (Float, Boolean) -> Unit,
    onResetRotate: () -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val totalAngle = (editState.rotationDegrees + editState.straightenDegrees).roundToInt()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = AccentPurple.copy(alpha = 0.22f),
            border = BorderStroke(1.dp, AccentPurple)
        ) {
            Text(
                text = "${editState.straightenDegrees.roundToInt()}° (Total ${totalAngle}°)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 5.dp)
                    .testTag("rotate_degree_badge")
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SmallChipButton("-90°", Icons.Filled.RotateLeft, testTag = "editor_rotate_left_90_btn", onClick = onRotateLeft90)
            SmallChipButton("+90°", Icons.Filled.RotateRight, testTag = "editor_rotate_90_btn", onClick = onRotate90)
            SmallChipButton(
                label = "Flip",
                icon = Icons.Filled.SwapVert,
                selected = editState.flipVertical,
                testTag = "editor_rotate_flip_vert_btn",
                onClick = onFlipVertical
            )
            SmallChipButton(
                label = "Mirror",
                icon = Icons.Filled.Flip,
                selected = editState.flipHorizontal,
                testTag = "editor_rotate_flip_btn",
                onClick = onFlipHorizontal
            )
            TextButton(onClick = onResetRotate, modifier = Modifier.testTag("editor_rotate_reset_btn")) {
                Text("Reset", style = MaterialTheme.typography.labelSmall, color = AccentTeal)
            }
        }
    }

    // Interactive Horizontal Degree Ruler (-180°..+180°)
    val currentDeg = editState.straightenDegrees.coerceIn(-180f, 180f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceElevated)
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                .pointerInput(currentDeg) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        val deltaDeg = -(dragAmount / 4.2f)
                        onSetStraighten((currentDeg + deltaDeg).coerceIn(-180f, 180f), false)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val frac = (offset.x / size.width.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
                        val tappedDeg = -90f + frac * 180f
                        onSetStraighten(tappedDeg.roundToInt().toFloat(), true)
                    }
                }
                .testTag("rotate_degree_ruler"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2f
                val stepPx = 10.dp.toPx()
                val startTick = (currentDeg - 45f).toInt()
                val endTick = (currentDeg + 45f).toInt()
                for (deg in startTick..endTick step 3) {
                    val x = centerX + (deg - currentDeg) * (stepPx / 3f)
                    if (x in 8f..(size.width - 8f)) {
                        val isMajor = deg % 15 == 0
                        val isHighlightMark = deg in listOf(-58, -15, 0, 15, 21)
                        val tickHeight = when {
                            deg == 0 -> size.height * 0.52f
                            isMajor -> size.height * 0.40f
                            else -> size.height * 0.22f
                        }
                        val color = when {
                            abs(deg - currentDeg) < 2f -> AccentPurple
                            isHighlightMark -> Color.White.copy(alpha = 0.85f)
                            else -> Color.White.copy(alpha = 0.32f)
                        }
                        drawLine(
                            color = color,
                            start = Offset(x, size.height - tickHeight - 6.dp.toPx()),
                            end = Offset(x, size.height - 6.dp.toPx()),
                            strokeWidth = if (isMajor) 2.2.dp.toPx() else 1.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
                // Center Purple Needle
                drawLine(
                    color = AccentPurple,
                    start = Offset(centerX, 4.dp.toPx()),
                    end = Offset(centerX, size.height - 2.dp.toPx()),
                    strokeWidth = 2.6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Quick degree mark presets including -58°, -15°, 0°, 15°, 21°
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(listOf(-58f, -15f, 0f, 15f, 21f, 45f)) { preset ->
                val isSel = abs(currentDeg - preset) < 1f
                Surface(
                    onClick = { onSetStraighten(preset, true) },
                    shape = RoundedCornerShape(999.dp),
                    color = if (isSel) SurfaceBright else SurfacePill
                ) {
                    Text(
                        text = "${preset.toInt()}°",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) TextOnWhite else TextSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }

    StudioCircleSlider(
        value = currentDeg,
        valueRange = -180f..180f,
        onValueChange = { deg -> onSetStraighten(deg, false) }
    )

    ToolConfirmCancelBar(
        title = "Rotate",
        onCancel = onCancel,
        onConfirm = onConfirm
    )
}

@Composable
private fun SmallChipButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean = false,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) SurfaceBright else SurfaceElevated,
        border = BorderStroke(1.dp, if (selected) SurfaceBright else BorderSubtle),
        modifier = Modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) TextOnWhite else TextPrimary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) TextOnWhite else TextPrimary
            )
        }
    }
}

// ==================== POSITION PANEL ====================

@Composable
internal fun PositionControlPanel(
    editState: StudioEditState,
    onSetPosition: (Float, Float, Boolean) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Text(
        text = "Drag clip directly on preview or use sliders",
        style = MaterialTheme.typography.labelSmall,
        color = AccentTeal
    )

    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        val presets = listOf(
            "Center" to (0f to 0f),
            "Top" to (0f to -0.22f),
            "Bottom" to (0f to 0.22f),
            "Left" to (-0.22f to 0f),
            "Right" to (0.22f to 0f)
        )
        items(presets) { (name, coords) ->
            val isSel = abs(editState.clipOffsetX - coords.first) < 0.03f &&
                abs(editState.clipOffsetY - coords.second) < 0.03f
            Surface(
                onClick = { onSetPosition(coords.first, coords.second, true) },
                shape = RoundedCornerShape(999.dp),
                color = if (isSel) SurfaceBright else SurfaceElevated,
                border = BorderStroke(1.dp, if (isSel) SurfaceBright else BorderSubtle),
                modifier = Modifier.testTag("position_preset_${name.lowercase()}")
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSel) TextOnWhite else TextPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }

    LabeledStudioSlider(
        label = "Horizontal (X)",
        value = editState.clipOffsetX,
        valueRange = -0.6f..0.6f,
        displayValue = "${(editState.clipOffsetX * 100).roundToInt()}%",
        onValueChange = { x -> onSetPosition(x, editState.clipOffsetY, false) }
    )
    LabeledStudioSlider(
        label = "Vertical (Y)",
        value = editState.clipOffsetY,
        valueRange = -0.6f..0.6f,
        displayValue = "${(editState.clipOffsetY * 100).roundToInt()}%",
        onValueChange = { y -> onSetPosition(editState.clipOffsetX, y, false) }
    )

    ToolConfirmCancelBar(
        title = "Position",
        onCancel = onCancel,
        onConfirm = onConfirm
    )
}

// ==================== SIZE (W / H SCALE) PANEL ====================

@Composable
internal fun SizeScalePanel(
    editState: StudioEditState,
    onSetSize: (Float, Float, Boolean) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    var lockAspect by remember { mutableStateOf(true) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
            items(listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f)) { s ->
                val isSel = abs(editState.clipScaleX - s) < 0.04f && abs(editState.clipScaleY - s) < 0.04f
                Surface(
                    onClick = { onSetSize(s, s, true) },
                    shape = RoundedCornerShape(999.dp),
                    color = if (isSel) SurfaceBright else SurfaceElevated,
                    border = BorderStroke(1.dp, if (isSel) SurfaceBright else BorderSubtle),
                    modifier = Modifier.testTag("size_preset_${(s * 100).toInt()}")
                ) {
                    Text(
                        text = "${(s * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) TextOnWhite else TextPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Surface(
            onClick = { lockAspect = !lockAspect },
            shape = RoundedCornerShape(999.dp),
            color = if (lockAspect) AccentPurple.copy(alpha = 0.22f) else SurfaceElevated,
            border = BorderStroke(1.dp, if (lockAspect) AccentPurple else BorderSubtle),
            modifier = Modifier.testTag("size_aspect_lock_btn")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (lockAspect) Icons.Filled.Link else Icons.Filled.LinkOff,
                    contentDescription = "Lock width and height ratio",
                    tint = if (lockAspect) AccentTeal else TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (lockAspect) "Locked" else "Free W/H",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (lockAspect) AccentTeal else TextSecondary
                )
            }
        }
    }

    LabeledStudioSlider(
        label = "Width (W)",
        value = editState.clipScaleX,
        valueRange = 0.35f..2.5f,
        displayValue = "${(editState.clipScaleX * 100).roundToInt()}%",
        onValueChange = { w ->
            if (lockAspect) {
                val ratio = if (editState.clipScaleX > 0.01f) editState.clipScaleY / editState.clipScaleX else 1f
                onSetSize(w, (w * ratio).coerceIn(0.35f, 2.5f), false)
            } else {
                onSetSize(w, editState.clipScaleY, false)
            }
        }
    )

    LabeledStudioSlider(
        label = "Height (H)",
        value = editState.clipScaleY,
        valueRange = 0.35f..2.5f,
        displayValue = "${(editState.clipScaleY * 100).roundToInt()}%",
        onValueChange = { h ->
            if (lockAspect) {
                val ratio = if (editState.clipScaleY > 0.01f) editState.clipScaleX / editState.clipScaleY else 1f
                onSetSize((h * ratio).coerceIn(0.35f, 2.5f), h, false)
            } else {
                onSetSize(editState.clipScaleX, h, false)
            }
        }
    )

    ToolConfirmCancelBar(
        title = "Size (W: ${(editState.clipScaleX * 100).roundToInt()}% H: ${(editState.clipScaleY * 100).roundToInt()}%)",
        onCancel = onCancel,
        onConfirm = onConfirm
    )
}

// ==================== FIT PANEL ====================

@Composable
internal fun FitModePanel(
    editState: StudioEditState,
    onSelectFitMode: (CanvasFitMode) -> Unit,
    onSelectAspectRatio: (AspectRatioOption) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Text("Canvas Fit Framing", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(CanvasFitMode.entries, key = { it.name }) { mode ->
            val selected = mode == editState.canvasFitMode
            Surface(
                onClick = { onSelectFitMode(mode) },
                shape = RoundedCornerShape(14.dp),
                color = if (selected) SurfaceBright else SurfaceElevated,
                border = BorderStroke(1.dp, if (selected) SurfaceBright else BorderSubtle),
                modifier = Modifier.testTag("fit_mode_${mode.name.lowercase()}")
            ) {
                Text(
                    text = mode.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) TextOnWhite else TextPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                )
            }
        }
    }

    Text("Canvas Aspect Ratio", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(AspectRatioOption.entries.filter { it != AspectRatioOption.FREEFORM }, key = { it.name }) { opt ->
            val selected = opt == editState.aspectRatio
            Surface(
                onClick = { onSelectAspectRatio(opt) },
                shape = RoundedCornerShape(14.dp),
                color = if (selected) AccentPurple else SurfaceElevated,
                border = BorderStroke(1.dp, if (selected) AccentPurple else BorderSubtle),
                modifier = Modifier.testTag("fit_aspect_${opt.name.lowercase()}")
            ) {
                Text(
                    text = opt.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) Color.White else TextPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }

    ToolConfirmCancelBar(
        title = "Fit · ${editState.canvasFitMode.label}",
        onCancel = onCancel,
        onConfirm = onConfirm
    )
}

// ==================== FADE CONTROL PANEL ====================

@Composable
internal fun FadeControlPanel(
    editState: StudioEditState,
    onSetFade: (fadeInMs: Int?, fadeOutMs: Int?, recordUndo: Boolean) -> Unit,
    onToggleAudioFade: (Boolean) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    LabeledStudioSlider(
        label = "Fade In Duration",
        value = editState.videoFadeInMs / 1000f,
        valueRange = 0f..3.0f,
        displayValue = "%.1fs".format(editState.videoFadeInMs / 1000f),
        onValueChange = { sec -> onSetFade((sec * 1000f).roundToInt(), null, false) }
    )
    LabeledStudioSlider(
        label = "Fade Out Duration",
        value = editState.videoFadeOutMs / 1000f,
        valueRange = 0f..3.0f,
        displayValue = "%.1fs".format(editState.videoFadeOutMs / 1000f),
        onValueChange = { sec -> onSetFade(null, (sec * 1000f).roundToInt(), false) }
    )

    if (editState.audioTrack != null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Apply Smooth Audio Fade In/Out", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
            Switch(
                checked = editState.audioTrack.fadeInOut,
                onCheckedChange = onToggleAudioFade,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AccentPurple)
            )
        }
    }

    ToolConfirmCancelBar(
        title = "Fade (In %.1fs / Out %.1fs)".format(editState.videoFadeInMs / 1000f, editState.videoFadeOutMs / 1000f),
        onCancel = onCancel,
        onConfirm = onConfirm
    )
}

// ==================== INTERACTIVE TRIMMER PANEL ====================

@Composable
internal fun TrimInteractivePanel(
    isVideo: Boolean,
    isPlaying: Boolean,
    totalDurationMs: Long,
    editState: StudioEditState,
    onTogglePlayPause: () -> Unit,
    onUpdateTrim: (Float, Float) -> Unit,
    onResetTrim: () -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val durSecTotal = (totalDurationMs.coerceAtLeast(1000L) / 1000f)
    val startSec = durSecTotal * editState.trimStartFraction
    val endSec = durSecTotal * editState.trimEndFraction
    val activeSec = (endSec - startSec).coerceAtLeast(0.1f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = onTogglePlayPause,
                shape = CircleShape,
                color = AccentPurple,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("trim_play_pause_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause preview" else "Play preview",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Column {
                Text(
                    text = "%.2fs".format(activeSec),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentTeal,
                    modifier = Modifier.testTag("trim_duration_readout")
                )
                Text(
                    text = if (isVideo) "%.2fs – %.2fs".format(startSec, endSec) else "Trim Window",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        if (editState.trimStartFraction > 0.01f || editState.trimEndFraction < 0.99f) {
            TextButton(onClick = onResetTrim, modifier = Modifier.testTag("trim_reset_btn")) {
                Text("Reset", color = TextPrimary)
            }
        }
    }

    // Quick Duration Presets: Original, 0.1s, 0.3s, 1s, 2.5s
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        item {
            val isOrig = editState.trimStartFraction <= 0.01f && editState.trimEndFraction >= 0.99f
            Surface(
                onClick = onResetTrim,
                shape = RoundedCornerShape(999.dp),
                color = if (isOrig) SurfaceBright else SurfaceElevated,
                border = BorderStroke(1.dp, if (isOrig) SurfaceBright else BorderSubtle),
                modifier = Modifier.testTag("trim_preset_original")
            ) {
                Text(
                    text = "Original",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isOrig) FontWeight.Bold else FontWeight.Medium,
                    color = if (isOrig) TextOnWhite else TextPrimary,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                )
            }
        }
        items(listOf(0.1f, 0.3f, 1.0f, 2.5f)) { targetSec ->
            val targetFrac = (targetSec / durSecTotal).coerceIn(0.03f, 1f)
            val isMatch = abs((editState.trimEndFraction - editState.trimStartFraction) - targetFrac) < 0.02f
            Surface(
                onClick = {
                    val start = editState.trimStartFraction.coerceAtMost(1f - targetFrac)
                    onUpdateTrim(start, (start + targetFrac).coerceAtMost(1f))
                },
                shape = RoundedCornerShape(999.dp),
                color = if (isMatch) AccentPurple else SurfaceElevated,
                border = BorderStroke(1.dp, if (isMatch) AccentPurple else BorderSubtle),
                modifier = Modifier.testTag("trim_preset_${targetSec}s")
            ) {
                Text(
                    text = if (targetSec == 1.0f) "1s" else "${targetSec}s",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isMatch) FontWeight.Bold else FontWeight.Medium,
                    color = if (isMatch) Color.White else TextPrimary,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                )
            }
        }
    }

    StudioCircleRangeSlider(
        value = editState.trimStartFraction..editState.trimEndFraction,
        onValueChange = { range ->
            val start = range.start.coerceIn(0f, 0.94f)
            val end = range.endInclusive.coerceAtLeast(start + 0.03f).coerceAtMost(1f)
            onUpdateTrim(start, end)
        },
        modifier = Modifier.testTag("trim_panel_range_slider")
    )

    ToolConfirmCancelBar(
        title = "Trim · %.2fs".format(activeSec),
        onCancel = onCancel,
        onConfirm = onConfirm
    )
}

// ==================== VOLUME (0% - 300% BOOST) PANEL ====================

@Composable
internal fun VolumeBoostPanel(
    isVideo: Boolean,
    editState: StudioEditState,
    onUpdateEditState: (Boolean, (StudioEditState) -> StudioEditState) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val activeVol = if (editState.muteAudio) 0f else editState.audioVolume.coerceIn(0f, 3.0f)
    val pct = (activeVol * 100).roundToInt()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            onClick = { onUpdateEditState(true) { it.copy(muteAudio = !it.muteAudio) } },
            shape = RoundedCornerShape(14.dp),
            color = if (editState.muteAudio) SurfaceBright else SurfaceElevated,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.testTag("volume_mute_toggle_btn")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = if (editState.muteAudio) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                    contentDescription = "Mute audio",
                    tint = if (editState.muteAudio) TextOnWhite else TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (editState.muteAudio) "Muted" else "Sound On",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (editState.muteAudio) TextOnWhite else TextPrimary
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(999.dp),
            color = if (pct > 100) AccentPurple.copy(alpha = 0.24f) else SurfaceElevated,
            border = BorderStroke(1.dp, if (pct > 100) AccentPurple else BorderSubtle)
        ) {
            Text(
                text = if (pct > 100) "$pct% Boost" else "$pct%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (pct > 100) AccentTeal else TextPrimary,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("volume_percent_badge")
            )
        }
    }

    LabeledStudioSlider(
        label = if (isVideo) "Volume (0 – 300)" else "Clip Volume (0 – 300)",
        value = activeVol,
        valueRange = 0f..3.0f,
        displayValue = "$pct%",
        onValueChange = { v ->
            onUpdateEditState(false) { it.copy(audioVolume = v.coerceIn(0f, 3.0f), muteAudio = v < 0.02f) }
        }
    )

    val track = editState.audioTrack
    if (track != null) {
        LabeledStudioSlider(
            label = "Audio Track (${track.trackTitle})",
            value = track.volume,
            valueRange = 0f..2.0f,
            displayValue = "${(track.volume * 100).roundToInt()}%",
            onValueChange = { v ->
                onUpdateEditState(false) { it.copy(audioTrack = track.copy(volume = v.coerceIn(0f, 2.0f))) }
            }
        )
    }

    ToolConfirmCancelBar(
        title = "Volume · $pct%",
        onCancel = onCancel,
        onConfirm = onConfirm
    )
}

// ==================== SPEED CONTROLS (CURVE & REGULAR) ====================

@Composable
fun SpeedCurveAndRegularPopup(
    editState: StudioEditState,
    totalDurationMs: Long,
    isVideo: Boolean,
    onSelectTab: (SpeedModeTab) -> Unit,
    onSelectCurvePreset: (SpeedCurvePreset) -> Unit,
    onUpdateCurvePoint: (Int, Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onReset: () -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val speedClamped = editState.playbackSpeed.coerceIn(0.25f, 4.0f)
    val speedFraction = ((speedClamped - 0.25f) / 3.75f).coerceIn(0f, 1f)
    val tierLabel = when {
        speedClamped < 0.85f -> "Slow-Mo"
        speedClamped <= 1.15f -> "Normal"
        speedClamped <= 2.35f -> "Fast"
        else -> "Hyper"
    }
    val origSec = (totalDurationMs.coerceAtLeast(1_000L) / 1000f)
    val newSec = (origSec / speedClamped).coerceAtLeast(0.3f)

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("speed_progress_popup"),
            shape = RoundedCornerShape(22.dp),
            color = SurfaceCard,
            border = BorderStroke(1.dp, AccentPurple)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header + Duration Readout + Speed Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AccentPurple.copy(alpha = 0.2f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Speed,
                                contentDescription = "Speed Control",
                                tint = AccentPurple,
                                modifier = Modifier.padding(8.dp).size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Speed",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isVideo) "Orig %.1fs → New %.1fs".format(origSec, newSec)
                                else "Motion & pacing %.1fx".format(speedClamped),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                modifier = Modifier.testTag("speed_duration_readout")
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (speedClamped == 1f) SurfaceElevated else AccentPurple.copy(alpha = 0.24f),
                        border = BorderStroke(1.dp, if (speedClamped == 1f) BorderSubtle else AccentPurple)
                    ) {
                        Text(
                            text = "${"%.2f".format(speedClamped)}x · $tierLabel",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (speedClamped == 1f) TextPrimary else AccentTeal,
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("speed_popup_value_badge")
                        )
                    }
                }

                // Mode Tabs: Curve | Regular
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpeedModeTab.entries.forEach { tab ->
                        val isSel = tab == editState.speedModeTab
                        Surface(
                            onClick = { onSelectTab(tab) },
                            shape = RoundedCornerShape(999.dp),
                            color = if (isSel) AccentPurple else SurfacePill,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("speed_tab_${tab.name.lowercase()}")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }

                if (editState.speedModeTab == SpeedModeTab.CURVE) {
                    // Interactive Speed Curve Graph
                    var selectedPointIdx by remember { mutableIntStateOf(2) }
                    val points = editState.customSpeedCurvePoints

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(104.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            // 1.0x reference line
                            val baseLineY = h - ((1.0f - 0.25f) / 3.75f) * h
                            drawLine(
                                color = Color.White.copy(alpha = 0.22f),
                                start = Offset(0f, baseLineY),
                                end = Offset(w, baseLineY),
                                strokeWidth = 1.dp.toPx()
                            )

                            val path = Path()
                            points.forEachIndexed { idx, spd ->
                                val x = (idx.toFloat() / (points.size - 1).coerceAtLeast(1)) * w
                                val normY = ((spd.coerceIn(0.25f, 4.0f) - 0.25f) / 3.75f)
                                val y = h - normY * h
                                if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            drawPath(
                                path = path,
                                color = AccentPurple,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )

                            points.forEachIndexed { idx, spd ->
                                val x = (idx.toFloat() / (points.size - 1).coerceAtLeast(1)) * w
                                val normY = ((spd.coerceIn(0.25f, 4.0f) - 0.25f) / 3.75f)
                                val y = h - normY * h
                                val isActivePt = idx == selectedPointIdx
                                drawCircle(
                                    color = if (isActivePt) AccentTeal else Color.White,
                                    radius = if (isActivePt) 6.dp.toPx() else 4.dp.toPx(),
                                    center = Offset(x, y)
                                )
                            }
                        }
                    }

                    // Point selector + slider to adjust custom curve beats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        points.forEachIndexed { idx, spd ->
                            val isSel = idx == selectedPointIdx
                            Surface(
                                onClick = { selectedPointIdx = idx },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) AccentPurple.copy(alpha = 0.24f) else SurfaceElevated,
                                border = BorderStroke(1.dp, if (isSel) AccentPurple else BorderSubtle),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "P${idx + 1} ${"%.1f".format(spd)}x",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (isSel) AccentTeal else TextSecondary,
                                    maxLines = 1,
                                    modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp)
                                )
                            }
                        }
                    }

                    StudioCircleSlider(
                        value = points.getOrElse(selectedPointIdx) { 1f },
                        valueRange = 0.25f..4.0f,
                        onValueChange = { v -> onUpdateCurvePoint(selectedPointIdx, v) }
                    )

                    // 8 Curve Presets
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(SpeedCurvePreset.entries, key = { it.name }) { preset ->
                            val isSel = preset == editState.speedCurvePreset
                            Surface(
                                onClick = { onSelectCurvePreset(preset) },
                                shape = RoundedCornerShape(999.dp),
                                color = if (isSel) SurfaceBright else SurfaceElevated,
                                border = BorderStroke(1.dp, if (isSel) SurfaceBright else BorderSubtle),
                                modifier = Modifier.testTag("speed_curve_${preset.name.lowercase()}")
                            ) {
                                Text(
                                    text = preset.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) TextOnWhite else TextPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Regular Speed Progress Bar + Circular Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0.25x Slow", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                text = "Progress ${(speedFraction * 100).roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentTeal,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text("4.0x Fast", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                        LinearProgressIndicator(
                            progress = { speedFraction },
                            color = AccentPurple,
                            trackColor = SurfaceElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .testTag("speed_progress_bar")
                        )
                    }

                    StudioCircleSlider(
                        value = speedClamped,
                        valueRange = 0.25f..4.0f,
                        onValueChange = { raw ->
                            val snapped = (raw * 20f).roundToInt() / 20f
                            onSpeedChange(snapped.coerceIn(0.25f, 4.0f))
                        },
                        modifier = Modifier.testTag("speed_popup_slider")
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.5f, 2.0f, 3.0f, 4.0f)) { preset ->
                            val isSel = abs(speedClamped - preset) < 0.03f
                            Surface(
                                onClick = { onSpeedChange(preset) },
                                shape = RoundedCornerShape(999.dp),
                                color = if (isSel) SurfaceBright else SurfaceElevated,
                                border = BorderStroke(1.dp, if (isSel) AccentPurple else BorderSubtle),
                                modifier = Modifier.testTag("speed_preset_${preset}")
                            ) {
                                Text(
                                    text = "${preset}x",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) TextOnWhite else TextPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }

                // Bottom Bar: X Cancel, Reset, ✓ Confirm
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("speed_popup_cancel_btn")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel speed", tint = TextSecondary)
                    }

                    OutlinedButton(
                        onClick = onReset,
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.weight(1f).testTag("speed_popup_reset_btn")
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset 1.0x", color = TextPrimary)
                    }

                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple, contentColor = Color.White),
                        modifier = Modifier.weight(1f).testTag("speed_popup_done_btn")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirm", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
internal fun SpeedInlineUpgradePanel(
    editState: StudioEditState,
    totalDurationMs: Long,
    onSelectCurvePreset: (SpeedCurvePreset) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onOpenSpeedPopup: () -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val speed = editState.playbackSpeed.coerceIn(0.25f, 4.0f)
    val origSec = (totalDurationMs.coerceAtLeast(1_000L) / 1000f)
    val newSec = (origSec / speed).coerceAtLeast(0.3f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = if (editState.speedModeTab == SpeedModeTab.CURVE) {
                    "Curve: ${editState.speedCurvePreset.label} (${"%.2f".format(speed)}x)"
                } else {
                    "Regular Speed: ${"%.2f".format(speed)}x"
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AccentTeal
            )
            Text(
                text = "Orig %.1fs → New %.1fs".format(origSec, newSec),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
        Button(
            onClick = onOpenSpeedPopup,
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple, contentColor = Color.White),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
            modifier = Modifier.height(34.dp).testTag("editor_open_speed_popup_btn")
        ) {
            Icon(Icons.Filled.Timeline, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text("Curve / Speed", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(SpeedCurvePreset.entries, key = { it.name }) { preset ->
            val isSel = preset == editState.speedCurvePreset && editState.speedModeTab == SpeedModeTab.CURVE
            Surface(
                onClick = { onSelectCurvePreset(preset) },
                shape = RoundedCornerShape(999.dp),
                color = if (isSel) SurfaceBright else SurfaceElevated,
                border = BorderStroke(1.dp, if (isSel) SurfaceBright else BorderSubtle)
            ) {
                Text(
                    text = preset.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSel) TextOnWhite else TextPrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }

    StudioCircleSlider(
        value = speed,
        valueRange = 0.25f..4.0f,
        onValueChange = { raw ->
            val snapped = (raw * 20f).roundToInt() / 20f
            onSpeedChange(snapped.coerceIn(0.25f, 4.0f))
        }
    )

    ToolConfirmCancelBar(
        title = "Speed · ${"%.2f".format(speed)}x",
        onCancel = onCancel,
        onConfirm = onConfirm
    )
}

// ==================== JUNCTION TRANSITION LIBRARY PANEL ====================

@Composable
internal fun JunctionTransitionLibraryPanel(
    editState: StudioEditState,
    playheadFraction: Float,
    junctionIndex: Int?,
    onSelectTransition: (String) -> Unit,
    onUpdateDurationMs: (Int) -> Unit,
    onApplyToAllJunctions: (String, Int) -> Unit,
    onSplitHere: (Float) -> Unit,
    onReplayTransition: () -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val segs = editState.timelineSegments
    val activeJunction = junctionIndex?.takeIf { it in 0 until segs.lastIndex }
        ?: if (segs.size >= 2) 0 else null
    val currentTransitionId = if (activeJunction != null) {
        segs[activeJunction].transitionAfterId
    } else {
        editState.transitionEffectId
    }
    val currentDurationMs = if (activeJunction != null) {
        segs[activeJunction].transitionAfterDurationMs
    } else {
        editState.animateDurationMs
    }

    var selectedCategory by remember { mutableStateOf(TransitionCategory.ALL) }

    // Junction banner or split prompt
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (activeJunction != null) {
                    "Clip ${activeJunction + 1} ↔ Clip ${activeJunction + 2} Transition"
                } else {
                    "Clip Transition Library (100 Effects)"
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AccentTeal
            )
            if (activeJunction == null) {
                Text(
                    text = "Split clip on timeline to place transitions between clips",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        if (segs.size < 2) {
            Surface(
                onClick = { onSplitHere(playheadFraction) },
                shape = RoundedCornerShape(999.dp),
                color = AccentPurple,
                modifier = Modifier.testTag("transition_quick_split_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.ContentCut, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                    Text("Split Clip", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        } else {
            Surface(
                onClick = { onApplyToAllJunctions(currentTransitionId, currentDurationMs) },
                shape = RoundedCornerShape(999.dp),
                color = SurfaceElevated,
                border = BorderStroke(1.dp, AccentPurple),
                modifier = Modifier.testTag("transition_apply_all_btn")
            ) {
                Text(
                    text = "Apply All",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }

    // Category Pills + None + Preview
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        item {
            val isNone = currentTransitionId == "none"
            Surface(
                onClick = { onSelectTransition("none") },
                shape = RoundedCornerShape(999.dp),
                color = if (isNone) SurfaceBright else SurfaceElevated,
                border = BorderStroke(1.dp, if (isNone) SurfaceBright else BorderSubtle)
            ) {
                Text(
                    text = "None",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isNone) TextOnWhite else TextPrimary,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                )
            }
        }
        if (currentTransitionId != "none") {
            item {
                Surface(
                    onClick = onReplayTransition,
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFF282046),
                    border = BorderStroke(1.dp, AccentTeal),
                    modifier = Modifier.testTag("editor_replay_transition_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(13.dp))
                        Text("Preview", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AccentTeal)
                    }
                }
            }
        }
        items(TransitionCategory.entries, key = { it.name }) { cat ->
            val isSel = cat == selectedCategory
            Surface(
                onClick = { selectedCategory = cat },
                shape = RoundedCornerShape(999.dp),
                color = if (isSel) SurfaceBright else SurfacePill,
                border = BorderStroke(1.dp, if (isSel) SurfaceBright else BorderSubtle)
            ) {
                Text(
                    text = cat.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSel) TextOnWhite else TextSecondary,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                )
            }
        }
    }

    val visibleTransitions = remember(selectedCategory) {
        if (selectedCategory == TransitionCategory.ALL) {
            StudioCatalog.transitionEffects
        } else {
            StudioCatalog.transitionEffects.filter { it.category == selectedCategory }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp)
    ) {
        items(visibleTransitions, key = { it.id }) { spec ->
            val isSel = currentTransitionId == spec.id
            Surface(
                onClick = { onSelectTransition(spec.id) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSel) Color(0xFF282046) else SurfaceElevated,
                border = BorderStroke(1.dp, if (isSel) AccentPurple else BorderSubtle),
                modifier = Modifier.testTag("editor_transition_${spec.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MovieFilter,
                        contentDescription = null,
                        tint = if (isSel) AccentTeal else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = spec.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${spec.category.label} · ${spec.durationMs}ms",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (isSel) AccentTeal else TextMuted,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }

    LabeledStudioSlider(
        label = "Transition Duration",
        value = currentDurationMs / 1000f,
        valueRange = 0.2f..2.0f,
        displayValue = "%.1fs".format(currentDurationMs / 1000f),
        onValueChange = { sec -> onUpdateDurationMs((sec * 1000f).roundToInt()) }
    )

    ToolConfirmCancelBar(
        title = StudioCatalog.transitionEffects.firstOrNull { it.id == currentTransitionId }?.name ?: "Transition",
        onCancel = onCancel,
        onConfirm = onConfirm
    )
}
