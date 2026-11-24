package com.example.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.NoiseAware
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VideoStable
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.media.SilenceAnalysisResult
import com.example.model.BackgroundReplaceMode
import com.example.model.ClipTransitionAnimation
import com.example.model.MaskShapeType
import com.example.model.StabilizeLevel
import com.example.model.StudioCatalog
import com.example.model.StudioEditState
import com.example.model.StudioToolId
import com.example.model.TrackingTarget
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentTeal
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
import kotlin.math.roundToInt

@Composable
fun SpeedProgressPopup(
    speed: Float,
    totalDurationMs: Long,
    isVideo: Boolean,
    onSpeedChange: (Float) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val speedClamped = speed.coerceIn(0.25f, 4.0f)
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
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .testTag("speed_progress_popup"),
            shape = RoundedCornerShape(22.dp),
            color = SurfaceCard,
            border = BorderStroke(1.dp, AccentPurple)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                                text = "Clip Speed Control",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isVideo) {
                                    "Orig %.1fs → New %.1fs".format(origSec, newSec)
                                } else {
                                    "Motion & playback pacing"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
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

                // Real-time Speed Progress Bar
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

                // Smooth Circular Thumb Slider
                StudioCircleSlider(
                    value = speedClamped,
                    valueRange = 0.25f..4.0f,
                    onValueChange = { raw ->
                        val snapped = (raw * 20f).roundToInt() / 20f
                        onSpeedChange(snapped.coerceIn(0.25f, 4.0f))
                    },
                    modifier = Modifier.testTag("speed_popup_slider")
                )

                // Preset Speed Pills
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.5f, 2.0f, 3.0f, 4.0f)) { preset ->
                        val isSel = kotlin.math.abs(speedClamped - preset) < 0.03f
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                        onClick = onDismiss,
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple, contentColor = Color.White),
                        modifier = Modifier.weight(1f).testTag("speed_popup_done_btn")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply Speed", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
internal fun SpeedInlinePanel(
    speed: Float,
    totalDurationMs: Long,
    onSpeedChange: (Float) -> Unit,
    onOpenSpeedPopup: () -> Unit
) {
    val speedFraction = ((speed - 0.25f) / 3.75f).coerceIn(0f, 1f)
    val origSec = (totalDurationMs.coerceAtLeast(1_000L) / 1000f)
    val newSec = (origSec / speed.coerceIn(0.25f, 4.0f)).coerceAtLeast(0.3f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Current Speed: ${"%.2f".format(speed)}x",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AccentTeal
            )
            Text(
                text = "Est. duration %.1fs (from %.1fs)".format(newSec, origSec),
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
            Text("Speed Popup", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }

    LinearProgressIndicator(
        progress = { speedFraction },
        color = AccentPurple,
        trackColor = SurfaceElevated,
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(999.dp))
    )

    StudioCircleSlider(
        value = speed.coerceIn(0.25f, 4.0f),
        valueRange = 0.25f..4.0f,
        onValueChange = { raw ->
            val snapped = (raw * 20f).roundToInt() / 20f
            onSpeedChange(snapped.coerceIn(0.25f, 4.0f))
        }
    )
}

@Composable
internal fun CutToolPanel(
    playheadFraction: Float,
    totalDurationMs: Long,
    editState: StudioEditState,
    onSplitTimeline: (Float) -> Unit,
    onSelectSegment: (String) -> Unit,
    onDeleteSelectedSegment: () -> Unit
) {
    val cutTimeSec = (totalDurationMs / 1000f) * playheadFraction
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { onSplitTimeline(playheadFraction) },
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple, contentColor = Color.White),
            modifier = Modifier.weight(1f).testTag("cut_panel_split_btn")
        ) {
            Icon(Icons.Filled.ContentCut, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Split Clip at %.1fs".format(cutTimeSec), fontWeight = FontWeight.Bold)
        }

        if (editState.timelineSegments.size > 1) {
            OutlinedButton(
                onClick = onDeleteSelectedSegment,
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, ErrorRed),
                modifier = Modifier.testTag("cut_panel_delete_seg_btn")
            ) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete Part", color = ErrorRed)
            }
        }
    }

    Text(
        text = "Timeline Clips (${editState.timelineSegments.size}) — Tap any part to select",
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondary
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(editState.timelineSegments, key = { it.id }) { seg ->
            val isSel = seg.id == editState.selectedSegmentId
            Surface(
                onClick = { onSelectSegment(seg.id) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSel) Color(0xFF282046) else SurfaceElevated,
                border = BorderStroke(1.dp, if (isSel) AccentPurple else BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)) {
                    Text(
                        text = seg.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${(seg.startFraction * 100).toInt()}%–${(seg.endFraction * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSel) AccentTeal else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
internal fun TrimToolPanel(
    isVideo: Boolean,
    totalDurationMs: Long,
    editState: StudioEditState,
    onUpdateTrim: (Float, Float) -> Unit,
    onResetTrim: () -> Unit
) {
    val startSec = (totalDurationMs / 1000f) * editState.trimStartFraction
    val endSec = (totalDurationMs / 1000f) * editState.trimEndFraction
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isVideo) {
                "Trim Range: %.1fs – %.1fs".format(startSec, endSec)
            } else {
                "Active Range: ${(editState.trimStartFraction * 100).toInt()}% – ${(editState.trimEndFraction * 100).toInt()}%"
            },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = AccentTeal
        )
        if (editState.trimStartFraction > 0.01f || editState.trimEndFraction < 0.99f) {
            TextButton(onClick = onResetTrim) {
                Text("Reset Trim", color = TextPrimary)
            }
        }
    }

    StudioCircleRangeSlider(
        value = editState.trimStartFraction..editState.trimEndFraction,
        onValueChange = { range ->
            val start = range.start.coerceIn(0f, 0.9f)
            val end = range.endInclusive.coerceAtLeast(start + 0.08f).coerceAtMost(1f)
            onUpdateTrim(start, end)
        },
        modifier = Modifier.testTag("trim_panel_range_slider")
    )
}

@Composable
internal fun FreezeToolPanel(
    playheadFraction: Float,
    editState: StudioEditState,
    onCaptureFreeze: (Float, Long) -> Unit,
    onUpdateFreezeDuration: (String, Long) -> Unit,
    onDeleteSelectedSegment: () -> Unit
) {
    val selectedSeg = editState.timelineSegments.firstOrNull { it.id == editState.selectedSegmentId }
    var nextDurationMs by remember(selectedSeg?.id, selectedSeg?.freezeDurationMs) {
        mutableLongStateOf(selectedSeg?.takeIf { it.isFrozen }?.freezeDurationMs ?: 3_000L)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onCaptureFreeze(playheadFraction, nextDurationMs) },
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple, contentColor = Color.White),
            modifier = Modifier.weight(1f).testTag("freeze_capture_btn")
        ) {
            Icon(Icons.Filled.AcUnit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Freeze Current Frame", fontWeight = FontWeight.Bold)
        }

        if (selectedSeg?.isFrozen == true && editState.timelineSegments.size > 1) {
            OutlinedButton(
                onClick = onDeleteSelectedSegment,
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, ErrorRed),
                modifier = Modifier.testTag("freeze_remove_btn")
            ) {
                Text("Unfreeze", color = ErrorRed)
            }
        }
    }

    if (selectedSeg?.isFrozen == true && !selectedSeg.frozenFramePath.isNullOrBlank()) {
        val thumbBmp = remember(selectedSeg.frozenFramePath) {
            runCatching { BitmapFactory.decodeFile(selectedSeg.frozenFramePath) }.getOrNull()
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (thumbBmp != null) {
                Image(
                    bitmap = thumbBmp.asImageBitmap(),
                    contentDescription = "Captured frozen frame",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.5.dp, AccentTeal, RoundedCornerShape(8.dp))
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Frozen Still Frame Active (${nextDurationMs / 1000}s)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentTeal
                )
                Text(
                    text = "Captured on timeline segment",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }

    LabeledStudioSlider(
        label = "Still Hold Duration",
        value = (nextDurationMs / 1000f).coerceIn(1f, 10f),
        valueRange = 1f..10f,
        displayValue = "${(nextDurationMs / 1000L)}s",
        onValueChange = { sec ->
            val ms = (sec.roundToInt() * 1000L).coerceIn(1_000L, 10_000L)
            nextDurationMs = ms
            if (selectedSeg?.isFrozen == true) {
                onUpdateFreezeDuration(selectedSeg.id, ms)
            }
        }
    )
}

@Composable
internal fun CopyToolPanel(
    editState: StudioEditState,
    onDuplicateSegment: () -> Unit,
    onSelectSegment: (String) -> Unit
) {
    Button(
        onClick = onDuplicateSegment,
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple, contentColor = Color.White),
        modifier = Modifier.fillMaxWidth().testTag("copy_duplicate_segment_btn")
    ) {
        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Duplicate Selected Clip (${editState.timelineSegments.size} on track)", fontWeight = FontWeight.Bold)
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(editState.timelineSegments, key = { it.id }) { seg ->
            val isSel = seg.id == editState.selectedSegmentId
            Surface(
                onClick = { onSelectSegment(seg.id) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSel) SurfaceBright else SurfaceElevated,
                border = BorderStroke(1.dp, if (isSel) AccentPurple else BorderSubtle)
            ) {
                Text(
                    text = seg.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSel) TextOnWhite else TextPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
internal fun ReplaceToolPanel(
    isVideo: Boolean,
    onReplaceClip: () -> Unit
) {
    Text(
        text = if (isVideo) "Swap the current video clip while preserving your filters, speed & overlays."
        else "Replace the active photo while preserving your color grades, frames & text.",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary
    )
    Button(
        onClick = onReplaceClip,
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple, contentColor = Color.White),
        modifier = Modifier.fillMaxWidth().testTag("replace_clip_btn")
    ) {
        Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Pick Replacement Photo or Video", fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun DeleteToolPanel(
    editState: StudioEditState,
    hasSelectedText: Boolean,
    hasSelectedSticker: Boolean,
    onDeleteSelectedSegment: () -> Unit,
    onDeleteActiveTarget: () -> Unit,
    onResetAllEdits: () -> Unit
) {
    val activeLabel = when {
        editState.timelineSegments.size > 1 -> "Delete Selected Timeline Segment"
        hasSelectedText -> "Delete Selected Text Box"
        hasSelectedSticker -> "Delete Selected Sticker"
        editState.audioTrack != null -> "Remove Attached Audio Track"
        editState.overlayImageUriString != null -> "Remove Picture Overlay"
        editState.chromaEnabled -> "Disable Chroma Key"
        editState.maskShape != MaskShapeType.NONE -> "Clear Active Mask"
        else -> "Reset All Active Edits"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = {
                if (editState.timelineSegments.size > 1) onDeleteSelectedSegment()
                else onDeleteActiveTarget()
            },
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White),
            modifier = Modifier.weight(1f).testTag("delete_active_target_btn")
        ) {
            Icon(Icons.Filled.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(activeLabel, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onResetAllEdits,
            shape = RoundedCornerShape(999.dp),
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.testTag("delete_reset_all_btn")
        ) {
            Text("Reset All", color = TextPrimary)
        }
    }
}

@Composable
internal fun MaskToolPanel(
    isVideo: Boolean,
    editState: StudioEditState,
    onUpdateEditState: (Boolean, (StudioEditState) -> StudioEditState) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(MaskShapeType.entries) { shape ->
            val isSel = editState.maskShape == shape
            Surface(
                onClick = { onUpdateEditState(true) { it.copy(maskShape = shape) } },
                shape = RoundedCornerShape(14.dp),
                color = if (isSel) SurfaceBright else SurfaceElevated,
                border = BorderStroke(1.dp, if (isSel) AccentPurple else BorderSubtle),
                modifier = Modifier.testTag("mask_shape_${shape.name.lowercase()}")
            ) {
                Text(
                    text = shape.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSel) TextOnWhite else TextPrimary,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)
                )
            }
        }
    }

    if (editState.maskShape != MaskShapeType.NONE) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isVideo) "Shape guide active on preview" else "Drag mask reticle on canvas or adjust sliders",
                style = MaterialTheme.typography.labelSmall,
                color = AccentTeal,
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Invert", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                Switch(
                    checked = editState.maskInvert,
                    onCheckedChange = { inv -> onUpdateEditState(true) { it.copy(maskInvert = inv) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AccentPurple),
                    modifier = Modifier.testTag("mask_invert_switch")
                )
            }
        }

        LabeledStudioSlider(
            label = "Mask Size",
            value = editState.maskScale,
            valueRange = 0.2f..1.25f,
            displayValue = "${(editState.maskScale * 100).toInt()}%",
            onValueChange = { s -> onUpdateEditState(false) { it.copy(maskScale = s) } }
        )
        LabeledStudioSlider(
            label = "Edge Feather",
            value = editState.maskFeather,
            valueRange = 0f..0.6f,
            displayValue = "${(editState.maskFeather * 100).toInt()}%",
            onValueChange = { f -> onUpdateEditState(false) { it.copy(maskFeather = f) } }
        )
        LabeledStudioSlider(
            label = "Angle / Rotation",
            value = editState.maskRotation,
            valueRange = -180f..180f,
            displayValue = "${editState.maskRotation.toInt()}°",
            onValueChange = { r -> onUpdateEditState(false) { it.copy(maskRotation = r) } }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                LabeledStudioSlider(
                    label = "Pos X",
                    value = editState.maskCenterX,
                    valueRange = 0.1f..0.9f,
                    displayValue = "${(editState.maskCenterX * 100).toInt()}%",
                    onValueChange = { x -> onUpdateEditState(false) { it.copy(maskCenterX = x) } }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                LabeledStudioSlider(
                    label = "Pos Y",
                    value = editState.maskCenterY,
                    valueRange = 0.1f..0.9f,
                    displayValue = "${(editState.maskCenterY * 100).toInt()}%",
                    onValueChange = { y -> onUpdateEditState(false) { it.copy(maskCenterY = y) } }
                )
            }
        }
    }
}

@Composable
internal fun ChromaToolPanel(
    isVideo: Boolean,
    editState: StudioEditState,
    onUpdateEditState: (Boolean, (StudioEditState) -> StudioEditState) -> Unit
) {
    val chromaColors = listOf(
        0xFF00C853L to "Green",
        0xFF2962FFL to "Blue",
        0xFF00E5FFL to "Cyan",
        0xFFD500F9L to "Magenta",
        0xFFFFFFFFL to "White",
        0xFF121212L to "Dark"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Chroma Key Backdrop", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(
                text = if (isVideo) "Key color saved for frame export & overlays" else "Remove green/blue screen & swap backdrop",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
        Switch(
            checked = editState.chromaEnabled,
            onCheckedChange = { en -> onUpdateEditState(true) { it.copy(chromaEnabled = en) } },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AccentPurple),
            modifier = Modifier.testTag("chroma_enable_switch")
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        chromaColors.forEach { (hex, label) ->
            val isSel = editState.chromaKeyColorHex == hex
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable {
                        onUpdateEditState(true) { it.copy(chromaEnabled = true, chromaKeyColorHex = hex) }
                    }
                    .testTag("chroma_color_${label.lowercase()}")
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(hex))
                        .border(if (isSel) 2.5.dp else 1.dp, if (isSel) Color.White else BorderSubtle, CircleShape)
                )
                Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = if (isSel) AccentTeal else TextMuted)
            }
        }
    }

    if (editState.chromaEnabled) {
        LabeledStudioSlider(
            label = "Key Similarity",
            value = editState.chromaSimilarity,
            valueRange = 0.05f..0.75f,
            displayValue = "${(editState.chromaSimilarity * 100).toInt()}%",
            onValueChange = { v -> onUpdateEditState(false) { it.copy(chromaSimilarity = v) } }
        )
        LabeledStudioSlider(
            label = "Edge Spill Smoothness",
            value = editState.chromaSmoothness,
            valueRange = 0.02f..0.45f,
            displayValue = "${(editState.chromaSmoothness * 100).toInt()}%",
            onValueChange = { v -> onUpdateEditState(false) { it.copy(chromaSmoothness = v) } }
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(BackgroundReplaceMode.entries) { bg ->
                val isSel = editState.bgReplaceMode == bg
                Surface(
                    onClick = { onUpdateEditState(true) { it.copy(bgReplaceMode = bg) } },
                    shape = RoundedCornerShape(999.dp),
                    color = if (isSel) SurfaceBright else SurfaceElevated
                ) {
                    Text(
                        text = bg.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSel) TextOnWhite else TextPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun AnimateAndAnimationPanel(
    tool: StudioToolId,
    editState: StudioEditState,
    onSelectClipAnimation: (ClipTransitionAnimation) -> Unit,
    onUpdateDurationMs: (Int) -> Unit,
    onReplayTransition: () -> Unit,
    onSelectTransitionsTool: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (tool == StudioToolId.ANIMATE) "Clip Entrance Motion" else "Combo & In/Out Animation",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(
                onClick = onReplayTransition,
                shape = RoundedCornerShape(999.dp),
                color = Color(0xFF282046),
                border = BorderStroke(1.dp, AccentTeal),
                modifier = Modifier.testTag("animate_replay_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(14.dp))
                    Text("Preview", style = MaterialTheme.typography.labelSmall, color = AccentTeal, fontWeight = FontWeight.Bold)
                }
            }
            Surface(
                onClick = onSelectTransitionsTool,
                shape = RoundedCornerShape(999.dp),
                color = SurfaceElevated,
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Text(
                    text = "100 FX",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(ClipTransitionAnimation.entries) { anim ->
            val selected = anim == editState.clipAnimation
            Surface(
                onClick = { onSelectClipAnimation(anim) },
                shape = RoundedCornerShape(14.dp),
                color = if (selected) AccentPurple else SurfaceElevated,
                border = BorderStroke(1.dp, if (selected) AccentPurple else BorderSubtle),
                modifier = Modifier.testTag("animate_preset_${anim.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Animation,
                        contentDescription = null,
                        tint = if (selected) Color.White else AccentTeal,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = anim.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) Color.White else TextPrimary,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }

    LabeledStudioSlider(
        label = "Animation Speed / Duration",
        value = editState.animateDurationMs.toFloat(),
        valueRange = 250f..2000f,
        displayValue = "${editState.animateDurationMs} ms",
        onValueChange = { ms -> onUpdateDurationMs(ms.roundToInt()) }
    )
}

@Composable
internal fun CameraTrackingToolPanel(
    editState: StudioEditState,
    onUpdateEditState: (Boolean, (StudioEditState) -> StudioEditState) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.CenterFocusStrong, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(16.dp))
                Text("Subject Camera Lock", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Text(
                text = "Tap preview canvas to move tracking anchor (${(editState.trackingAnchorX * 100).toInt()}%, ${(editState.trackingAnchorY * 100).toInt()}%)",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
        Switch(
            checked = editState.cameraTrackingEnabled,
            onCheckedChange = { en -> onUpdateEditState(true) { it.copy(cameraTrackingEnabled = en) } },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AccentPurple),
            modifier = Modifier.testTag("camera_tracking_switch")
        )
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(TrackingTarget.entries) { target ->
            val isSel = editState.cameraTrackingEnabled && editState.cameraTrackingTarget == target
            Surface(
                onClick = {
                    onUpdateEditState(true) {
                        it.copy(cameraTrackingEnabled = true, cameraTrackingTarget = target)
                    }
                },
                shape = RoundedCornerShape(14.dp),
                color = if (isSel) SurfaceBright else SurfaceElevated,
                border = BorderStroke(1.dp, if (isSel) AccentPurple else BorderSubtle)
            ) {
                Text(
                    text = target.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSel) TextOnWhite else TextPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }

    if (editState.cameraTrackingEnabled) {
        LabeledStudioSlider(
            label = "Framing Zoom Lock Strength",
            value = editState.cameraTrackingStrength,
            valueRange = 0.2f..1f,
            displayValue = "${(editState.cameraTrackingStrength * 100).toInt()}%",
            onValueChange = { s -> onUpdateEditState(false) { it.copy(cameraTrackingStrength = s) } }
        )
    }
}

@Composable
internal fun StabilizeToolPanel(
    editState: StudioEditState,
    onUpdateEditState: (Boolean, (StudioEditState) -> StudioEditState) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(StabilizeLevel.entries) { lvl ->
            val isSel = editState.stabilizeLevel == lvl
            Surface(
                onClick = { onUpdateEditState(true) { it.copy(stabilizeLevel = lvl) } },
                shape = RoundedCornerShape(14.dp),
                color = if (isSel) SurfaceBright else SurfaceElevated,
                border = BorderStroke(1.dp, if (isSel) AccentPurple else BorderSubtle),
                modifier = Modifier.testTag("stabilize_level_${lvl.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.VideoStable,
                        contentDescription = null,
                        tint = if (isSel) TextOnWhite else AccentTeal,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = lvl.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) TextOnWhite else TextPrimary
                    )
                }
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = editState.stabilizeLevel.detail,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Horizon Lock", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
            Switch(
                checked = editState.stabilizeHorizonLock,
                onCheckedChange = { h -> onUpdateEditState(true) { it.copy(stabilizeHorizonLock = h) } },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AccentPurple)
            )
        }
    }
}

@Composable
internal fun VocalSeparationToolPanel(
    editState: StudioEditState,
    onRunVocalSeparation: (Boolean) -> Unit,
    onExtractFromDeviceVideo: (Uri) -> Unit,
    onOpenSoundPanel: () -> Unit
) {
    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> if (uri != null) onExtractFromDeviceVideo(uri) }

    val activeTrack = editState.audioTrack
    val vocalOn = activeTrack?.vocalSeparation == true
    Text(
        text = if (activeTrack != null) {
            "Active track: ${activeTrack.trackTitle} (${if (vocalOn) "Vocals Isolated" else "Full Mix"})"
        } else {
            "Isolate center vocals cleanly or pick any video to separate voice from music."
        },
        style = MaterialTheme.typography.bodySmall,
        color = if (vocalOn) AccentTeal else TextSecondary
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { onRunVocalSeparation(!vocalOn) },
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (vocalOn) AccentTeal else AccentPurple,
                contentColor = if (vocalOn) Color.Black else Color.White
            ),
            modifier = Modifier.weight(1f).testTag("vocal_sep_apply_btn")
        ) {
            Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (vocalOn) "Restore Full Mix" else "Isolate Vocals", fontWeight = FontWeight.Bold)
        }
        OutlinedButton(
            onClick = { videoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
            shape = RoundedCornerShape(999.dp),
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Text("Pick Video", color = TextPrimary)
        }
        OutlinedButton(
            onClick = onOpenSoundPanel,
            shape = RoundedCornerShape(999.dp),
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Text("Sound", color = TextPrimary)
        }
    }
}

@Composable
internal fun NoiseReductionToolPanel(
    editState: StudioEditState,
    onApplyNoiseReduction: (Boolean, Float) -> Unit,
    onExtractFromDeviceVideo: (Uri) -> Unit
) {
    val track = editState.audioTrack
    var strength by remember(track?.rawFilePath, track?.noiseReductionStrength) {
        mutableFloatStateOf(track?.noiseReductionStrength ?: 0.78f)
    }
    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> if (uri != null) onExtractFromDeviceVideo(uri) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Filled.NoiseAware, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(16.dp))
            Text(
                text = if (track?.noiseReduction == true) "Background Static Suppressed" else "Clean Bad Audio & Suppress Static",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (track?.noiseReduction == true) AccentTeal else TextPrimary
            )
        }
    }

    LabeledStudioSlider(
        label = "Denoise Strength (Hum + Hiss Gate)",
        value = strength,
        valueRange = 0.2f..1.0f,
        displayValue = "${(strength * 100).toInt()}%",
        onValueChange = { strength = it }
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { onApplyNoiseReduction(true, strength) },
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple, contentColor = Color.White),
            modifier = Modifier.weight(1f).testTag("noise_reduction_apply_btn")
        ) {
            Icon(Icons.Filled.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Apply Noise Reduction", fontWeight = FontWeight.Bold)
        }

        if (track?.noiseReduction == true) {
            OutlinedButton(
                onClick = { onApplyNoiseReduction(false, strength) },
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.testTag("noise_reduction_clear_btn")
            ) {
                Text("Bypass", color = TextPrimary)
            }
        } else {
            OutlinedButton(
                onClick = { videoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Text("Pick Video", color = TextPrimary)
            }
        }
    }
}

@Composable
internal fun DetectSilenceToolPanel(
    silenceAnalysis: SilenceAnalysisResult?,
    thresholdDb: Float,
    minSilenceMs: Long,
    onScanSilence: (Float, Long) -> Unit,
    onTrimSilences: (Float, Long) -> Unit
) {
    var thresh by remember(thresholdDb) { mutableFloatStateOf(thresholdDb) }
    var minPause by remember(minSilenceMs) { mutableLongStateOf(minSilenceMs) }

    LabeledStudioSlider(
        label = "Silence Floor Threshold",
        value = thresh,
        valueRange = -55f..-18f,
        displayValue = "${thresh.toInt()} dB",
        onValueChange = { thresh = it }
    )
    LabeledStudioSlider(
        label = "Minimum Pause Duration",
        value = minPause.toFloat(),
        valueRange = 200f..1200f,
        displayValue = "${minPause} ms",
        onValueChange = { minPause = it.roundToInt().toLong() }
    )

    if (silenceAnalysis != null) {
        Text(
            text = if (silenceAnalysis.silentCount > 0) {
                "Found ${silenceAnalysis.silentCount} silent gaps (${silenceAnalysis.silentDurationMs / 1000f}s total)"
            } else {
                "Audio looks tight — no long silent pauses detected"
            },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (silenceAnalysis.silentCount > 0) AccentTeal else TextSecondary
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { onScanSilence(thresh, minPause) },
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceBright, contentColor = TextOnWhite),
            modifier = Modifier.weight(1f).testTag("detect_silence_scan_btn")
        ) {
            Icon(Icons.Filled.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Scan Silence", fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = { onTrimSilences(thresh, minPause) },
            enabled = (silenceAnalysis?.silentCount ?: 1) > 0,
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple, contentColor = Color.White),
            modifier = Modifier.weight(1f).testTag("detect_silence_cut_btn")
        ) {
            Icon(Icons.Filled.ContentCut, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Cut Silences", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun SeparateAudioToolPanel(
    isVideo: Boolean,
    editState: StudioEditState,
    onSeparateCurrentVideo: () -> Unit,
    onExtractFromDeviceVideo: (Uri) -> Unit,
    onSaveProcessedAudio: () -> Unit,
    onRemoveTrack: () -> Unit
) {
    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> if (uri != null) onExtractFromDeviceVideo(uri) }

    val track = editState.audioTrack
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isVideo) {
            Button(
                onClick = onSeparateCurrentVideo,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple, contentColor = Color.White),
                modifier = Modifier.weight(1f).testTag("separate_current_video_audio_btn")
            ) {
                Icon(Icons.Filled.ContentCut, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Extract Clip Audio", fontWeight = FontWeight.Bold)
            }
        }
        Button(
            onClick = { videoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated, contentColor = TextPrimary),
            modifier = Modifier.weight(1f).testTag("separate_pick_video_audio_btn")
        ) {
            Icon(Icons.Filled.MovieFilter, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Video to Audio")
        }
    }

    if (track != null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.AudioFile, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(16.dp))
                Text(
                    text = "${track.trackTitle} · ${track.durationMs / 1000}s",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onSaveProcessedAudio, modifier = Modifier.testTag("separate_save_wav_btn")) {
                    Icon(Icons.Filled.Download, contentDescription = "Save audio", tint = AccentTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save WAV", color = AccentTeal)
                }
                TextButton(onClick = onRemoveTrack) {
                    Icon(Icons.Filled.Close, contentDescription = "Remove", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
internal fun VolumeToolPanel(
    isVideo: Boolean,
    editState: StudioEditState,
    onUpdateEditState: (Boolean, (StudioEditState) -> StudioEditState) -> Unit,
    onToggleFadeInOut: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            onClick = { onUpdateEditState(true) { it.copy(muteAudio = !it.muteAudio) } },
            shape = RoundedCornerShape(14.dp),
            color = if (editState.muteAudio) SurfaceBright else SurfaceElevated,
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

        Box(modifier = Modifier.weight(1f)) {
            LabeledStudioSlider(
                label = if (isVideo) "Video Master Volume" else "Clip Volume",
                value = if (editState.muteAudio) 0f else editState.audioVolume,
                valueRange = 0f..1.5f,
                displayValue = "${((if (editState.muteAudio) 0f else editState.audioVolume) * 100).toInt()}%",
                onValueChange = { v ->
                    onUpdateEditState(false) { it.copy(audioVolume = v, muteAudio = v < 0.02f) }
                }
            )
        }
    }

    val track = editState.audioTrack
    if (track != null) {
        LabeledStudioSlider(
            label = "Audio Track (${track.trackTitle})",
            value = track.volume,
            valueRange = 0f..1f,
            displayValue = "${(track.volume * 100).toInt()}%",
            onValueChange = { v ->
                onUpdateEditState(false) { it.copy(audioTrack = track.copy(volume = v)) }
            }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Smooth Audio Fade In & Out", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
            Switch(
                checked = track.fadeInOut,
                onCheckedChange = { f -> onToggleFadeInOut(f) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AccentPurple),
                modifier = Modifier.testTag("volume_fade_switch")
            )
        }
    }
}
