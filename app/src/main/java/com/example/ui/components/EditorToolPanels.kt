package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.media.AudioDSPProcessor
import com.example.media.SilenceAnalysisResult
import com.example.model.AspectRatioOption
import com.example.model.AudioMenuTab
import com.example.model.BackgroundReplaceMode
import com.example.model.CanvasFitMode
import com.example.model.ClipTransitionAnimation
import com.example.model.EffectMode
import com.example.model.EnhanceMode
import com.example.model.FilterPreset
import com.example.model.FontLanguageFilter
import com.example.model.FrameStyle
import com.example.model.NormCropRect
import com.example.model.OverlayMode
import com.example.model.SpeedCurvePreset
import com.example.model.StickerCategory
import com.example.model.StudioAudioTrackState
import com.example.model.StudioCatalog
import com.example.model.StudioEditState
import com.example.model.StudioFilterSpec
import com.example.model.StudioMusicPreset
import com.example.model.StudioTemplate
import com.example.model.StudioToolId
import com.example.model.TextAnimationPreset
import com.example.model.TextArtPreset
import com.example.model.TextBoxStylePreset
import com.example.model.TextOverlayItem
import com.example.model.TextToolTab
import com.example.model.TransitionCategory
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
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun EditorContextualToolPanel(
    tool: StudioToolId,
    isVideo: Boolean,
    isPlaying: Boolean = true,
    playheadFraction: Float,
    totalDurationMs: Long = 10_000L,
    editState: StudioEditState,
    selectedTextOverlayId: String?,
    selectedStickerId: String? = null,
    selectedTransitionJunctionIndex: Int? = null,
    favoriteFontIds: Set<String>,
    silenceAnalysis: SilenceAnalysisResult? = null,
    silenceThresholdDb: Float = -35f,
    silenceMinMs: Long = 350L,
    onTogglePlayPause: () -> Unit = {},
    onSelectTool: (StudioToolId) -> Unit = {},
    onOpenSpeedPopup: () -> Unit = {},
    onUpdateEditState: (Boolean, (StudioEditState) -> StudioEditState) -> Unit,
    onSelectEnhanceMode: (EnhanceMode) -> Unit,
    onSelectFilter: (FilterPreset) -> Unit,
    onSelectFilterSpec: (StudioFilterSpec) -> Unit = {},
    onSelectEffect: (EffectMode) -> Unit,
    onSelectFxEffect: (String) -> Unit = {},
    onSelectTransitionEffect: (String) -> Unit,
    onApplyTransitionToAllJunctions: (String, Int) -> Unit = { _, _ -> },
    onReplayTransition: () -> Unit,
    onSelectOverlay: (OverlayMode) -> Unit,
    onSetOverlayImageUri: (Uri?) -> Unit = {},
    onSelectAspectRatio: (AspectRatioOption) -> Unit,
    onResetFreeformCrop: () -> Unit,
    onSelectFrame: (FrameStyle) -> Unit,
    onRotateLeft90: () -> Unit,
    onRotate90: () -> Unit,
    onRotate180: () -> Unit,
    onSetStraighten: (Float, Boolean) -> Unit,
    onResetRotate: () -> Unit,
    onFlipHorizontal: () -> Unit,
    onFlipVertical: () -> Unit = {},
    onSetClipPosition: (Float, Float, Boolean) -> Unit = { _, _, _ -> },
    onSetClipSize: (Float, Float, Boolean) -> Unit = { _, _, _ -> },
    onSelectFitMode: (CanvasFitMode) -> Unit = {},
    onSetVideoFade: (Int?, Int?, Boolean) -> Unit = { _, _, _ -> },
    onSelectSpeedCurvePreset: (SpeedCurvePreset) -> Unit = {},
    onApplyBgRemoval: (BackgroundReplaceMode) -> Unit,
    onClearBgRemoval: () -> Unit,
    onSplitTimeline: (Float) -> Unit,
    onSelectSegment: (String) -> Unit = {},
    onDuplicateSegment: () -> Unit = {},
    onCaptureFreeze: (Float, Long) -> Unit = { _, _ -> },
    onUpdateFreezeDuration: (String, Long) -> Unit = { _, _ -> },
    onReplaceClip: () -> Unit = {},
    onDeleteSelectedSegment: () -> Unit,
    onDeleteActiveTarget: () -> Unit = {},
    onResetAllEdits: () -> Unit = {},
    onSelectClipAnimation: (ClipTransitionAnimation) -> Unit,
    onCommitText: (String, Long?) -> Unit,
    onAddNewTextBox: () -> Unit,
    onUpdateSelectedTextStyle: (
        colorArgb: Long?,
        fontId: String?,
        artPreset: TextArtPreset?,
        stylePreset: TextBoxStylePreset?,
        animationPreset: TextAnimationPreset?,
        scale: Float?,
        rotationDeg: Float?,
        recordUndo: Boolean
    ) -> Unit,
    onToggleFavoriteFont: (String) -> Unit,
    onAddSticker: (label: String, isBadge: Boolean) -> Unit,
    onClearMosaic: () -> Unit,
    onClearRemoval: () -> Unit,
    onApplyTemplate: (StudioTemplate) -> Unit,
    onSelectBuiltInAudio: (StudioMusicPreset) -> Unit,
    onImportAudioUri: (Uri) -> Unit,
    onExtractAudioFromDeviceVideo: (Uri) -> Unit,
    onSeparateCurrentVideoAudio: () -> Unit,
    onAttachRecordedAudio: (File, Long) -> Unit,
    onApplyAudioDsp: (reverb: Float, vocalSep: Boolean, noiseRed: Boolean) -> Unit,
    onRunOneTapAudioFeature: (vocalSep: Boolean?, noiseRed: Boolean?, noiseStrength: Float?, fadeInOut: Boolean?) -> Unit = { _, _, _, _ -> },
    onScanSilence: (Float, Long) -> Unit = { _, _ -> },
    onTrimSilences: (Float, Long) -> Unit = { _, _ -> },
    onSaveProcessedAudio: () -> Unit,
    onRemoveAudioTrack: () -> Unit,
    onCancelToolChanges: (StudioEditState) -> Unit = { _ -> },
    onConfirmToolChanges: (StudioEditState) -> Unit = { _ -> },
    onClosePanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entrySnapshot = remember(tool, selectedTransitionJunctionIndex) { editState }
    val handleCancel = { onCancelToolChanges(entrySnapshot) }
    val handleConfirm = { onConfirmToolChanges(entrySnapshot) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceCard,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 310.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tool.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onClosePanel, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Close tool panel",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            when (tool) {
                StudioToolId.ENHANCE,
                StudioToolId.AI_EDIT,
                StudioToolId.VIDEO_QUALITY,
                StudioToolId.QUALITY_ENHANCER -> EnhancePanel(
                    editState = editState,
                    onSelectEnhanceMode = onSelectEnhanceMode,
                    onUpdateStrength = { v -> onUpdateEditState(false) { it.copy(enhanceStrength = v) } }
                )

                StudioToolId.BG_REMOVER, StudioToolId.CUTOUT -> BgRemoverToolPanel(
                    isVideo = isVideo,
                    editState = editState,
                    onApplyBgRemoval = onApplyBgRemoval,
                    onClearBgRemoval = onClearBgRemoval,
                    onUpdateFeather = { v -> onUpdateEditState(false) { it.copy(bgEdgeFeather = v) } }
                )

                StudioToolId.CROP -> CropToolPanel(
                    editState = editState,
                    onSelectAspectRatio = onSelectAspectRatio,
                    onResetFreeform = onResetFreeformCrop
                )

                StudioToolId.ROTATE -> RotateRulerPanel(
                    editState = editState,
                    onRotateLeft90 = onRotateLeft90,
                    onRotate90 = onRotate90,
                    onFlipHorizontal = onFlipHorizontal,
                    onFlipVertical = onFlipVertical,
                    onSetStraighten = onSetStraighten,
                    onResetRotate = onResetRotate,
                    onCancel = handleCancel,
                    onConfirm = handleConfirm
                )

                StudioToolId.FILTERS, StudioToolId.COLOR_MATCH -> FilterAndAdjustPanel(
                    editState = editState,
                    onSelectFilterSpec = onSelectFilterSpec,
                    onUpdateIntensity = { v -> onUpdateEditState(false) { it.copy(filterIntensity = v) } },
                    onUpdateEditState = onUpdateEditState,
                    onCancel = handleCancel,
                    onConfirm = handleConfirm
                )

                StudioToolId.ADJUST -> AdjustPanel(
                    editState = editState,
                    onUpdateEditState = onUpdateEditState
                )

                StudioToolId.EDIT -> ClipEditPanel(
                    isVideo = isVideo,
                    playheadFraction = playheadFraction,
                    editState = editState,
                    onSelectTool = onSelectTool,
                    onRotate90 = onRotate90,
                    onFlipHorizontal = onFlipHorizontal,
                    onSplitTimeline = onSplitTimeline,
                    onDeleteSelectedSegment = onDeleteSelectedSegment,
                    onSelectAspectRatio = onSelectAspectRatio,
                    onResetFreeformCrop = onResetFreeformCrop,
                    onSelectClipAnimation = onSelectClipAnimation,
                    onOpenSpeedPopup = onOpenSpeedPopup,
                    onUpdateEditState = onUpdateEditState
                )

                StudioToolId.CUT, StudioToolId.SPLIT -> CutToolPanel(
                    playheadFraction = playheadFraction,
                    totalDurationMs = totalDurationMs,
                    editState = editState,
                    onSplitTimeline = onSplitTimeline,
                    onSelectSegment = onSelectSegment,
                    onDeleteSelectedSegment = onDeleteSelectedSegment
                )

                StudioToolId.TRIM -> TrimInteractivePanel(
                    isVideo = isVideo,
                    isPlaying = isPlaying,
                    totalDurationMs = totalDurationMs,
                    editState = editState,
                    onTogglePlayPause = onTogglePlayPause,
                    onUpdateTrim = { s, e ->
                        onUpdateEditState(false) { it.copy(trimStartFraction = s, trimEndFraction = e) }
                    },
                    onResetTrim = {
                        onUpdateEditState(true) { it.copy(trimStartFraction = 0f, trimEndFraction = 1f) }
                    },
                    onCancel = handleCancel,
                    onConfirm = handleConfirm
                )

                StudioToolId.SPEED -> SpeedInlineUpgradePanel(
                    editState = editState,
                    totalDurationMs = totalDurationMs,
                    onSelectCurvePreset = onSelectSpeedCurvePreset,
                    onSpeedChange = { s -> onUpdateEditState(false) { it.copy(playbackSpeed = s) } },
                    onOpenSpeedPopup = onOpenSpeedPopup,
                    onCancel = handleCancel,
                    onConfirm = handleConfirm
                )

                StudioToolId.ANIMATION, StudioToolId.ANIMATE -> AnimateAndAnimationPanel(
                    tool = tool,
                    editState = editState,
                    onSelectClipAnimation = onSelectClipAnimation,
                    onUpdateDurationMs = { ms ->
                        onUpdateEditState(false) { it.copy(animateDurationMs = ms) }
                    },
                    onReplayTransition = onReplayTransition,
                    onSelectTransitionsTool = { onSelectTool(StudioToolId.TRANSITIONS) }
                )

                StudioToolId.VOLUME -> VolumeBoostPanel(
                    isVideo = isVideo,
                    editState = editState,
                    onUpdateEditState = onUpdateEditState,
                    onCancel = handleCancel,
                    onConfirm = handleConfirm
                )

                StudioToolId.POSITION -> PositionControlPanel(
                    editState = editState,
                    onSetPosition = onSetClipPosition,
                    onCancel = handleCancel,
                    onConfirm = handleConfirm
                )

                StudioToolId.SIZE -> SizeScalePanel(
                    editState = editState,
                    onSetSize = onSetClipSize,
                    onCancel = handleCancel,
                    onConfirm = handleConfirm
                )

                StudioToolId.FIT -> FitModePanel(
                    editState = editState,
                    onSelectFitMode = onSelectFitMode,
                    onSelectAspectRatio = onSelectAspectRatio,
                    onCancel = handleCancel,
                    onConfirm = handleConfirm
                )

                StudioToolId.FADE -> FadeControlPanel(
                    editState = editState,
                    onSetFade = onSetVideoFade,
                    onToggleAudioFade = { f -> onRunOneTapAudioFeature(null, null, null, f) },
                    onCancel = handleCancel,
                    onConfirm = handleConfirm
                )

                StudioToolId.DELETE -> DeleteToolPanel(
                    editState = editState,
                    hasSelectedText = selectedTextOverlayId != null,
                    hasSelectedSticker = selectedStickerId != null,
                    onDeleteSelectedSegment = onDeleteSelectedSegment,
                    onDeleteActiveTarget = onDeleteActiveTarget,
                    onResetAllEdits = onResetAllEdits
                )

                StudioToolId.COPY -> CopyToolPanel(
                    editState = editState,
                    onDuplicateSegment = onDuplicateSegment,
                    onSelectSegment = onSelectSegment
                )

                StudioToolId.FREEZE -> FreezeToolPanel(
                    playheadFraction = playheadFraction,
                    editState = editState,
                    onCaptureFreeze = onCaptureFreeze,
                    onUpdateFreezeDuration = onUpdateFreezeDuration,
                    onDeleteSelectedSegment = onDeleteSelectedSegment
                )

                StudioToolId.REPLACE -> ReplaceToolPanel(
                    isVideo = isVideo,
                    onReplaceClip = onReplaceClip
                )

                StudioToolId.CHROMA -> ChromaToolPanel(
                    isVideo = isVideo,
                    editState = editState,
                    onUpdateEditState = onUpdateEditState
                )

                StudioToolId.MASK -> MaskToolPanel(
                    isVideo = isVideo,
                    editState = editState,
                    onUpdateEditState = onUpdateEditState
                )

                StudioToolId.CAMERA_TRACKING -> CameraTrackingToolPanel(
                    editState = editState,
                    onUpdateEditState = onUpdateEditState
                )

                StudioToolId.STABILIZE -> StabilizeToolPanel(
                    editState = editState,
                    onUpdateEditState = onUpdateEditState
                )

                StudioToolId.VOCAL_SEPARATION -> VocalSeparationToolPanel(
                    editState = editState,
                    onRunVocalSeparation = { enable -> onRunOneTapAudioFeature(enable, null, null, null) },
                    onExtractFromDeviceVideo = onExtractAudioFromDeviceVideo,
                    onOpenSoundPanel = { onSelectTool(StudioToolId.SOUND) }
                )

                StudioToolId.NOISE_REDUCTION -> NoiseReductionToolPanel(
                    editState = editState,
                    onApplyNoiseReduction = { enable, strength ->
                        onRunOneTapAudioFeature(null, enable, strength, null)
                    },
                    onExtractFromDeviceVideo = onExtractAudioFromDeviceVideo
                )

                StudioToolId.DETECT_SILENCE -> DetectSilenceToolPanel(
                    silenceAnalysis = silenceAnalysis,
                    thresholdDb = silenceThresholdDb,
                    minSilenceMs = silenceMinMs,
                    onScanSilence = onScanSilence,
                    onTrimSilences = onTrimSilences
                )

                StudioToolId.SEPARATE_AUDIO -> SeparateAudioToolPanel(
                    isVideo = isVideo,
                    editState = editState,
                    onSeparateCurrentVideo = onSeparateCurrentVideoAudio,
                    onExtractFromDeviceVideo = onExtractAudioFromDeviceVideo,
                    onSaveProcessedAudio = onSaveProcessedAudio,
                    onRemoveTrack = onRemoveAudioTrack
                )

                StudioToolId.TRANSITIONS -> JunctionTransitionLibraryPanel(
                    editState = editState,
                    playheadFraction = playheadFraction,
                    junctionIndex = selectedTransitionJunctionIndex,
                    onSelectTransition = onSelectTransitionEffect,
                    onUpdateDurationMs = { ms ->
                        val jIdx = selectedTransitionJunctionIndex
                        if (jIdx != null && jIdx in 0 until editState.timelineSegments.lastIndex) {
                            onUpdateEditState(false) { st ->
                                st.copy(
                                    animateDurationMs = ms,
                                    timelineSegments = st.timelineSegments.mapIndexed { idx, seg ->
                                        if (idx == jIdx) seg.copy(transitionAfterDurationMs = ms) else seg
                                    }
                                )
                            }
                        } else {
                            onUpdateEditState(false) { it.copy(animateDurationMs = ms) }
                        }
                    },
                    onApplyToAllJunctions = onApplyTransitionToAllJunctions,
                    onSplitHere = onSplitTimeline,
                    onReplayTransition = onReplayTransition,
                    onCancel = handleCancel,
                    onConfirm = handleConfirm
                )

                StudioToolId.EFFECTS -> FxEffectsPickerPanel(
                    editState = editState,
                    onSelectFx = onSelectFxEffect,
                    onUpdateFxIntensity = { v -> onUpdateEditState(false) { it.copy(fxIntensity = v) } },
                    onCancel = handleCancel,
                    onConfirm = handleConfirm
                )

                StudioToolId.OVERLAY -> OverlaysPanel(
                    editState = editState,
                    onSelectOverlay = onSelectOverlay,
                    onUpdateIntensity = { v -> onUpdateEditState(false) { it.copy(overlayIntensity = v) } },
                    onSetOverlayImageUri = onSetOverlayImageUri,
                    onUpdateOverlayImageOpacity = { op ->
                        onUpdateEditState(false) { it.copy(overlayImageOpacity = op) }
                    }
                )

                StudioToolId.TEXT -> TextToolPanel(
                    selectedTextOverlay = editState.textOverlays.firstOrNull { it.id == selectedTextOverlayId }
                        ?: editState.textOverlays.lastOrNull(),
                    favoriteFontIds = favoriteFontIds,
                    onCommitText = onCommitText,
                    onAddNewTextBox = onAddNewTextBox,
                    onUpdateSelectedTextStyle = onUpdateSelectedTextStyle,
                    onToggleFavoriteFont = onToggleFavoriteFont
                )

                StudioToolId.STICKERS -> StickersToolPanel(onAddSticker = onAddSticker)

                StudioToolId.FRAMES -> FramesToolPanel(
                    selected = editState.frameStyle,
                    onSelect = onSelectFrame
                )

                StudioToolId.CANVAS -> CanvasRatioPanel(
                    selected = editState.aspectRatio,
                    hasCustomFreeform = !editState.freeformCrop.isFull,
                    onSelect = onSelectAspectRatio,
                    onResetFreeform = onResetFreeformCrop
                )

                StudioToolId.AUDIO, StudioToolId.SOUND -> AudioToolPanel(
                    isVideo = isVideo,
                    editState = editState,
                    onUpdateEditState = onUpdateEditState,
                    onSelectBuiltInAudio = onSelectBuiltInAudio,
                    onImportAudioUri = onImportAudioUri,
                    onExtractAudioFromDeviceVideo = onExtractAudioFromDeviceVideo,
                    onSeparateCurrentVideoAudio = onSeparateCurrentVideoAudio,
                    onAttachRecordedAudio = onAttachRecordedAudio,
                    onApplyAudioDsp = onApplyAudioDsp,
                    onSaveProcessedAudio = onSaveProcessedAudio,
                    onRemoveAudioTrack = onRemoveAudioTrack
                )

                StudioToolId.MOSAIC -> TouchBrushPanel(
                    hint = if (isVideo) "Mosaic privacy blur is optimized for Photo edits" else "Tap or drag on the photo above to pixelate areas",
                    count = editState.mosaicPoints.size,
                    brushSize = null,
                    onBrushSizeChange = {},
                    onClear = onClearMosaic
                )

                StudioToolId.REMOVAL, StudioToolId.AI_REMOVAL -> TouchBrushPanel(
                    hint = if (isVideo) "Object removal heal brush works on Photo frames" else "Tap spots on the photo above to softly heal & remove objects",
                    count = editState.removalPoints.size,
                    brushSize = editState.removalBrushSize,
                    onBrushSizeChange = { b -> onUpdateEditState(false) { it.copy(removalBrushSize = b) } },
                    onClear = onClearRemoval
                )

                StudioToolId.TEMPLATE -> TemplateQuickPanel(onApplyTemplate = onApplyTemplate)

                StudioToolId.FACE,
                StudioToolId.SKIN_SMOOTH,
                StudioToolId.BRIGHTEN,
                StudioToolId.AI_RETOUCH,
                StudioToolId.BODY,
                StudioToolId.HAIR,
                StudioToolId.MAKEUP -> RetouchPanel(
                    activeTool = tool,
                    editState = editState,
                    onUpdateEditState = onUpdateEditState
                )

                else -> Unit
            }
        }
    }
}

@Composable
private fun EnhancePanel(
    editState: StudioEditState,
    onSelectEnhanceMode: (EnhanceMode) -> Unit,
    onUpdateStrength: (Float) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(EnhanceMode.entries) { mode ->
            val selected = mode == editState.enhanceMode
            Surface(
                onClick = { onSelectEnhanceMode(mode) },
                shape = RoundedCornerShape(14.dp),
                color = if (selected) SurfaceBright else SurfaceElevated,
                border = BorderStroke(1.dp, if (selected) SurfaceBright else BorderSubtle),
                modifier = Modifier.testTag("editor_enhance_${mode.name.lowercase()}")
            ) {
                Text(
                    text = mode.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) TextOnWhite else TextPrimary,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                )
            }
        }
    }
    if (editState.enhanceMode != EnhanceMode.NONE) {
        LabeledStudioSlider(
            label = "Clarity Strength",
            value = editState.enhanceStrength,
            valueRange = 0.1f..1f,
            displayValue = "${(editState.enhanceStrength * 100).toInt()}%",
            onValueChange = onUpdateStrength
        )
    }
}

@Composable
private fun BgRemoverToolPanel(
    isVideo: Boolean,
    editState: StudioEditState,
    onApplyBgRemoval: (BackgroundReplaceMode) -> Unit,
    onClearBgRemoval: () -> Unit,
    onUpdateFeather: (Float) -> Unit
) {
    if (isVideo) {
        Text(
            text = "Background Remover (Keep Person) works on Photo portraits. Pick a photo to cut out the subject.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onApplyBgRemoval(editState.bgReplaceMode) },
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6C47FF),
                contentColor = Color.White
            ),
            modifier = Modifier
                .weight(1f)
                .testTag("editor_bg_remove_btn")
        ) {
            Icon(
                imageVector = Icons.Filled.PersonRemove,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (editState.removeBackground) "Re-Detect Person" else "Remove Background (Keep Person)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (editState.removeBackground) {
            Surface(
                onClick = onClearBgRemoval,
                shape = RoundedCornerShape(999.dp),
                color = SurfaceElevated,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.testTag("editor_bg_restore_btn")
            ) {
                Text(
                    text = "Restore",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                )
            }
        }
    }

    Text(
        text = "Backdrop Style (Person Kept Intact)",
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondary
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(BackgroundReplaceMode.entries) { bgMode ->
            val isSel = editState.removeBackground && editState.bgReplaceMode == bgMode
            Surface(
                onClick = { onApplyBgRemoval(bgMode) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSel) SurfaceBright else SurfaceElevated,
                border = BorderStroke(1.dp, if (isSel) Color(0xFF8B6EFF) else BorderSubtle),
                modifier = Modifier.testTag("editor_bg_mode_${bgMode.name.lowercase()}")
            ) {
                Text(
                    text = bgMode.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSel) TextOnWhite else TextPrimary,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)
                )
            }
        }
    }

    if (editState.removeBackground) {
        LabeledStudioSlider(
            label = "Edge Feather Smoothness",
            value = editState.bgEdgeFeather,
            valueRange = 0f..0.9f,
            displayValue = "${(editState.bgEdgeFeather * 100).toInt()}%",
            onValueChange = onUpdateFeather
        )
    }
}

@Composable
private fun CropToolPanel(
    editState: StudioEditState,
    onSelectAspectRatio: (AspectRatioOption) -> Unit,
    onResetFreeform: () -> Unit
) {
    Text(
        text = "Choose an aspect ratio or drag Freeform handles on the canvas",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary
    )
    CanvasRatioPanel(
        selected = editState.aspectRatio,
        hasCustomFreeform = !editState.freeformCrop.isFull,
        onSelect = onSelectAspectRatio,
        onResetFreeform = onResetFreeform
    )
}

@Composable
private fun RotateToolPanel(
    editState: StudioEditState,
    onRotateLeft90: () -> Unit,
    onRotate90: () -> Unit,
    onRotate180: () -> Unit,
    onFlipHorizontal: () -> Unit,
    onSetStraighten: (Float, Boolean) -> Unit,
    onResetRotate: () -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Surface(
                onClick = onRotateLeft90,
                shape = RoundedCornerShape(14.dp),
                color = SurfaceElevated,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.testTag("editor_rotate_left_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Filled.RotateLeft, contentDescription = "Rotate left 90", tint = TextPrimary, modifier = Modifier.size(16.dp))
                    Text("-90°", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
            }
        }
        item {
            Surface(
                onClick = onRotate90,
                shape = RoundedCornerShape(14.dp),
                color = SurfaceElevated,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.testTag("editor_rotate_right_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Filled.RotateRight, contentDescription = "Rotate right 90", tint = TextPrimary, modifier = Modifier.size(16.dp))
                    Text("+90°", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
            }
        }
        item {
            Surface(
                onClick = onRotate180,
                shape = RoundedCornerShape(14.dp),
                color = SurfaceElevated,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.testTag("editor_rotate_180_btn")
            ) {
                Text(
                    text = "180°",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                )
            }
        }
        item {
            Surface(
                onClick = onFlipHorizontal,
                shape = RoundedCornerShape(14.dp),
                color = if (editState.flipHorizontal) SurfaceBright else SurfaceElevated,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.testTag("editor_rotate_flip_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Filled.Flip, contentDescription = "Mirror", tint = if (editState.flipHorizontal) TextOnWhite else TextPrimary, modifier = Modifier.size(16.dp))
                    Text("Mirror", style = MaterialTheme.typography.labelMedium, color = if (editState.flipHorizontal) TextOnWhite else TextPrimary)
                }
            }
        }
        if (editState.rotationDegrees != 0 || editState.straightenDegrees != 0f || editState.flipHorizontal) {
            item {
                Surface(
                    onClick = onResetRotate,
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceElevated,
                    border = BorderStroke(1.dp, AccentTeal)
                ) {
                    Text(
                        text = "Reset",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentTeal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                    )
                }
            }
        }
    }

    LabeledStudioSlider(
        label = "Straighten Angle",
        value = editState.straightenDegrees,
        valueRange = -45f..45f,
        displayValue = "${editState.straightenDegrees.toInt()}°",
        onValueChange = { deg -> onSetStraighten(deg, false) }
    )
}

@Composable
private fun FiltersPanel(
    editState: StudioEditState,
    onSelectFilter: (FilterPreset) -> Unit,
    onUpdateIntensity: (Float) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(FilterPreset.entries) { preset ->
            val selected = preset == editState.filterPreset
            Surface(
                onClick = { onSelectFilter(preset) },
                shape = RoundedCornerShape(14.dp),
                color = if (selected) SurfaceBright else SurfaceElevated,
                border = BorderStroke(1.dp, if (selected) SurfaceBright else BorderSubtle),
                modifier = Modifier.testTag("editor_filter_${preset.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(preset.accentHex))
                    )
                    Text(
                        text = preset.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) TextOnWhite else TextPrimary
                    )
                }
            }
        }
    }
    if (editState.filterPreset != FilterPreset.NATURAL) {
        LabeledStudioSlider(
            label = "Intensity",
            value = editState.filterIntensity,
            valueRange = 0f..1f,
            displayValue = "${(editState.filterIntensity * 100).toInt()}%",
            onValueChange = onUpdateIntensity
        )
    }
}

@Composable
internal fun AdjustPanel(
    editState: StudioEditState,
    onUpdateEditState: (Boolean, (StudioEditState) -> StudioEditState) -> Unit
) {
    var activeSlider by remember { mutableStateOf("Brightness") }
    val options = listOf("Brightness", "Contrast", "Saturation", "Color Balance", "Sharpen", "Vignette")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options) { opt ->
            val selected = opt == activeSlider
            Surface(
                onClick = { activeSlider = opt },
                shape = RoundedCornerShape(999.dp),
                color = if (selected) SurfaceBright else SurfacePill,
                modifier = Modifier.testTag("adjust_tab_${opt.lowercase().replace(" ", "_")}")
            ) {
                Text(
                    text = opt,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) TextOnWhite else TextSecondary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }

    when (activeSlider) {
        "Brightness" -> LabeledStudioSlider("Brightness", editState.brightness, -0.5f..0.5f, "${(editState.brightness * 200).toInt()}") { v ->
            onUpdateEditState(false) { it.copy(brightness = v) }
        }
        "Contrast" -> LabeledStudioSlider("Contrast", editState.contrast, -0.5f..0.5f, "${(editState.contrast * 200).toInt()}") { v ->
            onUpdateEditState(false) { it.copy(contrast = v) }
        }
        "Saturation" -> LabeledStudioSlider("Saturation", editState.saturation, -0.5f..0.5f, "${(editState.saturation * 200).toInt()}") { v ->
            onUpdateEditState(false) { it.copy(saturation = v) }
        }
        "Color Balance" -> {
            LabeledStudioSlider("Warmth (Cool – Amber)", editState.warmth, -0.5f..0.5f, "${(editState.warmth * 200).toInt()}") { v ->
                onUpdateEditState(false) { it.copy(warmth = v) }
            }
            LabeledStudioSlider("Tint (Green – Magenta)", editState.tint, -0.5f..0.5f, "${(editState.tint * 200).toInt()}") { v ->
                onUpdateEditState(false) { it.copy(tint = v) }
            }
        }
        "Sharpen" -> LabeledStudioSlider("Sharpen", editState.sharpen, 0f..1f, "${(editState.sharpen * 100).toInt()}%") { v ->
            onUpdateEditState(false) { it.copy(sharpen = v) }
        }
        "Vignette" -> LabeledStudioSlider("Vignette", editState.vignette, 0f..1f, "${(editState.vignette * 100).toInt()}%") { v ->
            onUpdateEditState(false) { it.copy(vignette = v) }
        }
    }
}

@Composable
private fun ClipEditPanel(
    isVideo: Boolean,
    playheadFraction: Float,
    editState: StudioEditState,
    onSelectTool: (StudioToolId) -> Unit,
    onRotate90: () -> Unit,
    onFlipHorizontal: () -> Unit,
    onSplitTimeline: (Float) -> Unit,
    onDeleteSelectedSegment: () -> Unit,
    onSelectAspectRatio: (AspectRatioOption) -> Unit,
    onResetFreeformCrop: () -> Unit,
    onSelectClipAnimation: (ClipTransitionAnimation) -> Unit,
    onOpenSpeedPopup: () -> Unit,
    onUpdateEditState: (Boolean, (StudioEditState) -> StudioEditState) -> Unit
) {
    Text("All Edit Suite Tools", style = MaterialTheme.typography.labelSmall, color = AccentTeal, fontWeight = FontWeight.Bold)
    val quickTools = remember {
        listOf(
            StudioToolId.OVERLAY,
            StudioToolId.ANIMATE,
            StudioToolId.CUTOUT,
            StudioToolId.CHROMA,
            StudioToolId.FREEZE,
            StudioToolId.REPLACE,
            StudioToolId.CAMERA_TRACKING,
            StudioToolId.VOCAL_SEPARATION,
            StudioToolId.NOISE_REDUCTION,
            StudioToolId.DETECT_SILENCE,
            StudioToolId.SEPARATE_AUDIO,
            StudioToolId.SOUND,
            StudioToolId.COPY,
            StudioToolId.TRIM,
            StudioToolId.MASK,
            StudioToolId.ADJUST,
            StudioToolId.STABILIZE,
            StudioToolId.REMOVAL,
            StudioToolId.QUALITY_ENHANCER,
            StudioToolId.CUT,
            StudioToolId.SPEED,
            StudioToolId.ANIMATION,
            StudioToolId.VOLUME,
            StudioToolId.DELETE
        )
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(quickTools, key = { it.name }) { quick ->
            Surface(
                onClick = { onSelectTool(quick) },
                shape = RoundedCornerShape(12.dp),
                color = SurfaceElevated,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.testTag("edit_quick_${quick.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(toolIconFor(quick), contentDescription = quick.label, tint = AccentPurple, modifier = Modifier.size(14.dp))
                    Text(quick.label, style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                }
            }
        }
    }

    // Primary Toolbar: Cut/Split, Rotate, Flip, Delete Segment
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Surface(
                onClick = { onSplitTimeline(playheadFraction) },
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF282046),
                border = BorderStroke(1.dp, Color(0xFF8B6EFF)),
                modifier = Modifier.testTag("editor_split_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Filled.ContentCut, contentDescription = "Split clip", tint = Color.White, modifier = Modifier.size(16.dp))
                    Text("Cut / Split", style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            Surface(
                onClick = onDeleteSelectedSegment,
                shape = RoundedCornerShape(14.dp),
                color = if (editState.timelineSegments.size > 1) ErrorRed.copy(alpha = 0.2f) else SurfaceElevated,
                border = BorderStroke(1.dp, if (editState.timelineSegments.size > 1) ErrorRed else BorderSubtle),
                modifier = Modifier.testTag("editor_delete_segment_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete segment", tint = if (editState.timelineSegments.size > 1) ErrorRed else TextMuted, modifier = Modifier.size(16.dp))
                    Text(
                        text = if (editState.timelineSegments.size > 1) "Delete Clip" else "Delete",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (editState.timelineSegments.size > 1) ErrorRed else TextMuted
                    )
                }
            }
        }
        item {
            Surface(
                onClick = onRotate90,
                shape = RoundedCornerShape(14.dp),
                color = SurfaceElevated,
                modifier = Modifier.testTag("editor_rotate_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Filled.RotateRight, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                    Text("Rotate", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
            }
        }
        item {
            Surface(
                onClick = onFlipHorizontal,
                shape = RoundedCornerShape(14.dp),
                color = SurfaceElevated,
                modifier = Modifier.testTag("editor_flip_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Filled.Flip, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                    Text("Mirror", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
            }
        }
    }

    // Crop aspect ratios (9:16, 16:9, 1:1, Freeform)
    Text("Crop & Aspect Ratio", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    CanvasRatioPanel(
        selected = editState.aspectRatio,
        hasCustomFreeform = !editState.freeformCrop.isFull,
        onSelect = onSelectAspectRatio,
        onResetFreeform = onResetFreeformCrop
    )

    // Speed + Popup trigger
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Playback Speed (${"%.2f".format(editState.playbackSpeed)}x)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(
            text = "Open Speed Bar →",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AccentTeal,
            modifier = Modifier.clickable { onOpenSpeedPopup() }
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(0.5f, 1f, 1.5f, 2f).forEach { speed ->
            val selected = editState.playbackSpeed == speed
            Surface(
                onClick = { onUpdateEditState(true) { it.copy(playbackSpeed = speed) } },
                shape = RoundedCornerShape(999.dp),
                color = if (selected) SurfaceBright else SurfacePill,
                modifier = Modifier.weight(1f).testTag("editor_speed_${speed}")
            ) {
                Box(modifier = Modifier.padding(vertical = 7.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "${speed}x",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) TextOnWhite else TextPrimary
                    )
                }
            }
        }
    }

    // Clip Animation
    Text("Clip Animation", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(ClipTransitionAnimation.entries) { anim ->
            val selected = anim == editState.clipAnimation
            Surface(
                onClick = { onSelectClipAnimation(anim) },
                shape = RoundedCornerShape(12.dp),
                color = if (selected) Color(0xFF6C47FF) else SurfaceElevated,
                modifier = Modifier.testTag("editor_clip_anim_${anim.name.lowercase()}")
            ) {
                Text(
                    text = anim.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) Color.White else TextPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }
    }

    if (isVideo) {
        Text(
            text = "Trim Range (${(editState.trimStartFraction * 100).toInt()}% – ${(editState.trimEndFraction * 100).toInt()}%)",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        StudioCircleRangeSlider(
            value = editState.trimStartFraction..editState.trimEndFraction,
            onValueChange = { range ->
                val start = range.start.coerceIn(0f, 0.9f)
                val end = range.endInclusive.coerceAtLeast(start + 0.08f).coerceAtMost(1f)
                onUpdateEditState(false) { it.copy(trimStartFraction = start, trimEndFraction = end) }
            }
        )
    }
}

@Composable
private fun EffectsPanel(
    editState: StudioEditState,
    onSelectEffect: (EffectMode) -> Unit,
    onSelectTransitionEffect: (String) -> Unit,
    onReplayTransition: () -> Unit,
    onUpdateIntensity: (Float) -> Unit
) {
    var showTransitions by remember { mutableStateOf(true) }

    // Sub-tabs: Transitions (100) vs Visual FX
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            onClick = { showTransitions = true },
            shape = RoundedCornerShape(999.dp),
            color = if (showTransitions) Color(0xFF6C47FF) else SurfacePill,
            modifier = Modifier.weight(1f).testTag("editor_fx_tab_transitions")
        ) {
            Box(modifier = Modifier.padding(vertical = 7.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "Transitions (100)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (showTransitions) FontWeight.Bold else FontWeight.Medium,
                    color = if (showTransitions) Color.White else TextSecondary
                )
            }
        }
        Surface(
            onClick = { showTransitions = false },
            shape = RoundedCornerShape(999.dp),
            color = if (!showTransitions) Color(0xFF6C47FF) else SurfacePill,
            modifier = Modifier.weight(1f).testTag("editor_fx_tab_visual")
        ) {
            Box(modifier = Modifier.padding(vertical = 7.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "Visual FX",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (!showTransitions) FontWeight.Bold else FontWeight.Medium,
                    color = if (!showTransitions) Color.White else TextSecondary
                )
            }
        }
    }

    if (showTransitions) {
        TransitionsBrowserSection(
            selectedTransitionId = editState.transitionEffectId,
            onSelectTransitionEffect = onSelectTransitionEffect,
            onReplayTransition = onReplayTransition
        )
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(EffectMode.entries) { mode ->
                val selected = mode == editState.effectMode
                Surface(
                    onClick = { onSelectEffect(mode) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (selected) SurfaceBright else SurfaceElevated
                ) {
                    Text(
                        text = mode.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) TextOnWhite else TextPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                    )
                }
            }
        }
        if (editState.effectMode != EffectMode.NONE) {
            LabeledStudioSlider(
                label = "Effect Strength",
                value = editState.effectIntensity,
                valueRange = 0.1f..1f,
                displayValue = "${(editState.effectIntensity * 100).toInt()}%",
                onValueChange = onUpdateIntensity
            )
        }
    }
}

@Composable
private fun TransitionsBrowserSection(
    selectedTransitionId: String,
    onSelectTransitionEffect: (String) -> Unit,
    onReplayTransition: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(TransitionCategory.ALL) }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        item {
            val isNone = selectedTransitionId == "none"
            Surface(
                onClick = { onSelectTransitionEffect("none") },
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
        if (selectedTransitionId != "none") {
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
                        Text(
                            text = "Preview",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentTeal
                        )
                    }
                }
            }
        }
        items(TransitionCategory.entries) { cat ->
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
            .height(152.dp)
    ) {
        items(visibleTransitions, key = { it.id }) { spec ->
            val isSel = selectedTransitionId == spec.id
            Surface(
                onClick = { onSelectTransitionEffect(spec.id) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSel) Color(0xFF282046) else SurfaceElevated,
                border = BorderStroke(1.dp, if (isSel) Color(0xFF8B6EFF) else BorderSubtle),
                modifier = Modifier.testTag("editor_transition_${spec.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
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
}

@Composable
private fun OverlaysPanel(
    editState: StudioEditState,
    onSelectOverlay: (OverlayMode) -> Unit,
    onUpdateIntensity: (Float) -> Unit,
    onSetOverlayImageUri: (Uri?) -> Unit,
    onUpdateOverlayImageOpacity: (Float) -> Unit
) {
    val overlayPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) onSetOverlayImageUri(uri)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = {
                overlayPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple, contentColor = Color.White),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
            modifier = Modifier.height(34.dp).testTag("overlay_pick_image_btn")
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Photo Overlay", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }

        if (editState.overlayImageUriString != null) {
            OutlinedButton(
                onClick = { onSetOverlayImageUri(null) },
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, ErrorRed),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(34.dp).testTag("overlay_clear_image_btn")
            ) {
                Text("Remove Photo", style = MaterialTheme.typography.labelSmall, color = ErrorRed)
            }
        }
    }

    if (editState.overlayImageUriString != null) {
        LabeledStudioSlider(
            label = "Picture Overlay Opacity",
            value = editState.overlayImageOpacity,
            valueRange = 0.1f..1f,
            displayValue = "${(editState.overlayImageOpacity * 100).toInt()}%",
            onValueChange = onUpdateOverlayImageOpacity
        )
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(OverlayMode.entries) { mode ->
            val selected = mode == editState.overlayMode
            Surface(
                onClick = { onSelectOverlay(mode) },
                shape = RoundedCornerShape(14.dp),
                color = if (selected) SurfaceBright else SurfaceElevated
            ) {
                Text(
                    text = mode.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) TextOnWhite else TextPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                )
            }
        }
    }
    if (editState.overlayMode != OverlayMode.NONE) {
        LabeledStudioSlider(
            label = "Blend Opacity",
            value = editState.overlayIntensity,
            valueRange = 0.1f..1f,
            displayValue = "${(editState.overlayIntensity * 100).toInt()}%",
            onValueChange = onUpdateIntensity
        )
    }
}

@Composable
private fun TextToolPanel(
    selectedTextOverlay: TextOverlayItem?,
    favoriteFontIds: Set<String>,
    onCommitText: (String, Long?) -> Unit,
    onAddNewTextBox: () -> Unit,
    onUpdateSelectedTextStyle: (
        colorArgb: Long?,
        fontId: String?,
        artPreset: TextArtPreset?,
        stylePreset: TextBoxStylePreset?,
        animationPreset: TextAnimationPreset?,
        scale: Float?,
        rotationDeg: Float?,
        recordUndo: Boolean
    ) -> Unit,
    onToggleFavoriteFont: (String) -> Unit
) {
    var activeTab by remember { mutableStateOf(TextToolTab.FONTS) }
    var fontFilter by remember { mutableStateOf(FontLanguageFilter.ALL) }
    var draft by remember(selectedTextOverlay?.id, selectedTextOverlay?.text) {
        mutableStateOf(
            if (selectedTextOverlay?.text == "Enter text") "" else selectedTextOverlay?.text.orEmpty()
        )
    }

    // Bottom Text Input Bar with Purple Checkmark Confirmation Button
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            placeholder = { Text("Enter text", color = TextMuted) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceElevated,
                unfocusedContainerColor = SurfaceElevated,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = AccentPurple,
                unfocusedBorderColor = BorderSubtle
            ),
            modifier = Modifier
                .weight(1f)
                .testTag("editor_text_input")
        )

        // Purple Checkmark Confirmation Button
        Button(
            onClick = {
                val finalTxt = draft.trim().ifEmpty { "Enter text" }
                onCommitText(finalTxt, null)
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6C47FF),
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .size(50.dp)
                .testTag("editor_text_confirm_btn")
        ) {
            Icon(Icons.Filled.Check, contentDescription = "Confirm text", modifier = Modifier.size(22.dp))
        }

        Surface(
            onClick = onAddNewTextBox,
            shape = RoundedCornerShape(14.dp),
            color = SurfaceElevated,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .size(50.dp)
                .testTag("editor_add_text_btn")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Add, contentDescription = "New text box", tint = TextPrimary)
            }
        }
    }

    // 5 Category Tabs: Basic | Art | Styles | Fonts | Animation
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(TextToolTab.entries) { tab ->
            val selected = tab == activeTab
            Surface(
                onClick = { activeTab = tab },
                shape = RoundedCornerShape(999.dp),
                color = if (selected) Color(0xFF6C47FF) else SurfacePill,
                modifier = Modifier.testTag("editor_text_tab_${tab.name.lowercase()}")
            ) {
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) Color.White else TextSecondary,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.dp)
                )
            }
        }
    }

    when (activeTab) {
        TextToolTab.BASIC -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Font Colors (${StudioCatalog.textColorChoices.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = "Swipe to browse",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextMuted
                )
            }
            LazyHorizontalGrid(
                rows = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                items(StudioCatalog.textColorChoices) { c ->
                    val argb = c.toArgb().toLong() and 0xFFFFFFFFL
                    val isSel = selectedTextOverlay?.colorArgb == argb
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(c)
                            .border(
                                width = if (isSel) 2.5.dp else 1.dp,
                                color = if (isSel) Color.White else BorderSubtle,
                                shape = CircleShape
                            )
                            .clickable {
                                onUpdateSelectedTextStyle(argb, null, TextArtPreset.NONE, null, null, null, null, true)
                            }
                            .testTag("editor_text_color_${argb.toString(16)}")
                    )
                }
            }
            LabeledStudioSlider(
                label = "Size",
                value = selectedTextOverlay?.scale ?: 1f,
                valueRange = 0.5f..2.6f,
                displayValue = "${((selectedTextOverlay?.scale ?: 1f) * 100).toInt()}%",
                onValueChange = { s ->
                    onUpdateSelectedTextStyle(null, null, null, null, null, s, null, false)
                }
            )
            LabeledStudioSlider(
                label = "Rotate",
                value = selectedTextOverlay?.rotationDeg ?: 0f,
                valueRange = -180f..180f,
                displayValue = "${(selectedTextOverlay?.rotationDeg ?: 0f).toInt()}°",
                onValueChange = { deg ->
                    onUpdateSelectedTextStyle(null, null, null, null, null, null, deg, false)
                }
            )
        }

        TextToolTab.ART -> {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(TextArtPreset.entries) { preset ->
                    val isSel = selectedTextOverlay?.artPreset == preset
                    Surface(
                        onClick = {
                            onUpdateSelectedTextStyle(null, null, preset, null, null, null, null, true)
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSel) Color(preset.accentArgb).copy(alpha = 0.22f) else SurfaceElevated,
                        border = BorderStroke(1.5.dp, if (isSel) Color(preset.accentArgb) else BorderSubtle),
                        modifier = Modifier.testTag("editor_text_art_${preset.name.lowercase()}")
                    ) {
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(preset.accentArgb),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                        )
                    }
                }
            }
        }

        TextToolTab.STYLES -> {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(TextBoxStylePreset.entries) { preset ->
                    val isSel = selectedTextOverlay?.stylePreset == preset
                    Surface(
                        onClick = {
                            onUpdateSelectedTextStyle(null, null, null, preset, null, null, null, true)
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSel) SurfaceBright else SurfaceElevated,
                        modifier = Modifier.testTag("editor_text_style_${preset.name.lowercase()}")
                    ) {
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSel) TextOnWhite else TextPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                        )
                    }
                }
            }
        }

        TextToolTab.FONTS -> {
            // Font Filter Pills: Favorites | All (100) | English | Urdu | Hindi | Chinese | Multi-Lang
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(FontLanguageFilter.entries) { filter ->
                    val isSel = fontFilter == filter
                    Surface(
                        onClick = { fontFilter = filter },
                        shape = RoundedCornerShape(999.dp),
                        color = if (isSel) SurfaceBright else SurfaceElevated,
                        border = BorderStroke(1.dp, if (isSel) SurfaceBright else BorderSubtle),
                        modifier = Modifier.testTag("editor_font_filter_${filter.name.lowercase()}")
                    ) {
                        Text(
                            text = filter.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSel) TextOnWhite else TextSecondary,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            val visibleFonts = remember(fontFilter, favoriteFontIds) {
                when (fontFilter) {
                    FontLanguageFilter.FAVORITES -> StudioCatalog.studioFonts.filter { it.id in favoriteFontIds }
                    FontLanguageFilter.ALL -> StudioCatalog.studioFonts
                    else -> StudioCatalog.studioFonts.filter { it.language == fontFilter }
                }
            }

            if (visibleFonts.isEmpty()) {
                Text(
                    text = "Tap the star on any font to save it to Favorites.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(126.dp)
                ) {
                    items(visibleFonts, key = { it.id }) { fontSpec ->
                        val isSel = selectedTextOverlay?.fontId == fontSpec.id
                        val isFav = fontSpec.id in favoriteFontIds
                        val composeFamily = remember(fontSpec.fontRes) {
                            FontFamily(Font(fontSpec.fontRes))
                        }
                        Surface(
                            onClick = {
                                onUpdateSelectedTextStyle(null, fontSpec.id, null, null, null, null, null, true)
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) Color(0xFF282046) else SurfaceElevated,
                            border = BorderStroke(1.dp, if (isSel) Color(0xFF8B6EFF) else BorderSubtle),
                            modifier = Modifier.testTag("editor_font_${fontSpec.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = fontSpec.sampleText,
                                        fontFamily = composeFamily,
                                        fontSize = 16.sp,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = fontSpec.displayName,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = TextMuted,
                                        maxLines = 1
                                    )
                                }
                                IconButton(
                                    onClick = { onToggleFavoriteFont(fontSpec.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Filled.Star else Icons.Filled.StarBorder,
                                        contentDescription = "Favorite font",
                                        tint = if (isFav) Color(0xFFFFD166) else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        TextToolTab.ANIMATION -> {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(TextAnimationPreset.entries) { anim ->
                    val isSel = selectedTextOverlay?.animationPreset == anim
                    Surface(
                        onClick = {
                            onUpdateSelectedTextStyle(null, null, null, null, anim, null, null, true)
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSel) Color(0xFF6C47FF) else SurfaceElevated,
                        modifier = Modifier.testTag("editor_text_anim_${anim.name.lowercase()}")
                    ) {
                        Text(
                            text = anim.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSel) Color.White else TextPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StickersToolPanel(onAddSticker: (label: String, isBadge: Boolean) -> Unit) {
    var selectedCategory by remember { mutableStateOf(StickerCategory.ALL) }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(StickerCategory.entries) { cat ->
            val isSel = cat == selectedCategory
            Surface(
                onClick = { selectedCategory = cat },
                shape = RoundedCornerShape(999.dp),
                color = if (isSel) Color(0xFF6C47FF) else SurfacePill,
                border = BorderStroke(1.dp, if (isSel) Color(0xFF8B6EFF) else BorderSubtle)
            ) {
                Text(
                    text = if (cat == StickerCategory.ALL) "All (100)" else cat.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSel) Color.White else TextSecondary,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                )
            }
        }
    }

    val visibleStickers = remember(selectedCategory) {
        if (selectedCategory == StickerCategory.ALL) {
            StudioCatalog.studioStickers
        } else {
            StudioCatalog.studioStickers.filter { it.category == selectedCategory }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(152.dp)
    ) {
        items(visibleStickers, key = { it.id }) { spec ->
            Surface(
                onClick = { onAddSticker(spec.content, spec.isBadge) },
                shape = RoundedCornerShape(12.dp),
                color = SurfaceElevated,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .height(44.dp)
                    .testTag("editor_sticker_${spec.id}")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = spec.content,
                        fontSize = if (spec.isBadge) 10.sp else 22.sp,
                        fontWeight = if (spec.isBadge) FontWeight.Bold else FontWeight.Normal,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    Text(
        text = "100 Offline Stickers • Tap to add, pinch/drag to resize & move",
        style = MaterialTheme.typography.bodySmall,
        color = TextMuted
    )
}

@Composable
private fun FramesToolPanel(
    selected: FrameStyle,
    onSelect: (FrameStyle) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(FrameStyle.entries) { frame ->
            val isSel = frame == selected
            Surface(
                onClick = { onSelect(frame) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSel) SurfaceBright else SurfaceElevated
            ) {
                Text(
                    text = frame.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSel) TextOnWhite else TextPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                )
            }
        }
    }
}

@Composable
private fun CanvasRatioPanel(
    selected: AspectRatioOption,
    hasCustomFreeform: Boolean,
    onSelect: (AspectRatioOption) -> Unit,
    onResetFreeform: () -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(AspectRatioOption.entries) { ratio ->
            val isSel = ratio == selected
            Surface(
                onClick = { onSelect(ratio) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSel) SurfaceBright else SurfaceElevated,
                modifier = Modifier.testTag("editor_crop_${ratio.name.lowercase()}")
            ) {
                Text(
                    text = ratio.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSel) TextOnWhite else TextPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
    if (selected == AspectRatioOption.FREEFORM || hasCustomFreeform) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Drag the corner handles on the preview to crop Freeform",
                style = MaterialTheme.typography.bodySmall,
                color = AccentTeal,
                modifier = Modifier.weight(1f)
            )
            if (hasCustomFreeform) {
                TextButton(onClick = onResetFreeform) {
                    Text("Reset Crop", color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun AudioToolPanel(
    isVideo: Boolean,
    editState: StudioEditState,
    onUpdateEditState: (Boolean, (StudioEditState) -> StudioEditState) -> Unit,
    onSelectBuiltInAudio: (StudioMusicPreset) -> Unit,
    onImportAudioUri: (Uri) -> Unit,
    onExtractAudioFromDeviceVideo: (Uri) -> Unit,
    onSeparateCurrentVideoAudio: () -> Unit,
    onAttachRecordedAudio: (File, Long) -> Unit,
    onApplyAudioDsp: (reverb: Float, vocalSep: Boolean, noiseRed: Boolean) -> Unit,
    onSaveProcessedAudio: () -> Unit,
    onRemoveAudioTrack: () -> Unit
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(AudioMenuTab.MUSIC_LIST) }

    // Audio & Video pickers (zero storage permission!)
    val audioFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) onImportAudioUri(uri)
    }

    val videoToAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) onExtractAudioFromDeviceVideo(uri)
    }

    // Mic recording state + runtime permission
    var isRecording by remember { mutableStateOf(false) }
    var recordStartMs by remember { mutableLongStateOf(0L) }
    var recordSeconds by remember { mutableIntStateOf(0) }
    var permissionDeniedNote by remember { mutableStateOf(false) }
    val micRecorder = remember(context) { AudioDSPProcessor.MicAudioRecorder(context) }

    DisposableEffect(micRecorder) {
        onDispose {
            if (isRecording) micRecorder.stopRecording()
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordSeconds = 0
            while (isRecording) {
                delay(500L)
                recordSeconds = ((System.currentTimeMillis() - recordStartMs) / 1000L).toInt()
            }
        }
    }

    val recordPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permissionDeniedNote = false
            val res = micRecorder.startRecording()
            if (res.isSuccess) {
                recordStartMs = System.currentTimeMillis()
                isRecording = true
            }
        } else {
            permissionDeniedNote = true
        }
    }

    // Audio Preview ExoPlayer for active track
    val track = editState.audioTrack
    var isPlayingTrack by remember { mutableStateOf(false) }
    val trackPlayer = remember(track?.processedFilePath) {
        val file = track?.processedFilePath?.let(::File)?.takeIf { it.exists() && it.length() > 44L }
        if (file != null) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
                prepare()
            }
        } else null
    }
    DisposableEffect(trackPlayer) {
        onDispose {
            isPlayingTrack = false
            trackPlayer?.release()
        }
    }

    // Audio Menu Tabs: Music List | Import | BGM | Record
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AudioMenuTab.entries.forEach { tab ->
            val selected = tab == activeTab
            Surface(
                onClick = { activeTab = tab },
                shape = RoundedCornerShape(999.dp),
                color = if (selected) Color(0xFF6C47FF) else SurfacePill,
                modifier = Modifier
                    .weight(1f)
                    .testTag("editor_audio_tab_${tab.name.lowercase()}")
            ) {
                Box(modifier = Modifier.padding(vertical = 7.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Color.White else TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }

    // Tab Body
    when (activeTab) {
        AudioMenuTab.MUSIC_LIST -> {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(StudioCatalog.musicListPresets, key = { it.id }) { preset ->
                    val isSel = track?.trackTitle == preset.title
                    Surface(
                        onClick = { onSelectBuiltInAudio(preset) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSel) Color(0xFF282046) else SurfaceElevated,
                        border = BorderStroke(1.dp, if (isSel) Color(0xFF8B6EFF) else BorderSubtle),
                        modifier = Modifier.width(142.dp).testTag("audio_preset_${preset.id}")
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Filled.MusicNote, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(16.dp))
                                Text(preset.title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
                            }
                            Text(preset.subtitle, style = MaterialTheme.typography.labelSmall, color = TextMuted, maxLines = 1)
                        }
                    }
                }
            }
        }

        AudioMenuTab.IMPORT -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = { audioFileLauncher.launch("audio/*") },
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceElevated,
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.weight(1f).testTag("audio_import_file_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.AudioFile, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import Audio", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                    }
                }

                Surface(
                    onClick = {
                        videoToAudioLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF282046),
                    border = BorderStroke(1.dp, Color(0xFF8B6EFF)),
                    modifier = Modifier.weight(1f).testTag("audio_video_to_audio_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.MovieFilter, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Video to Audio", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            if (isVideo) {
                Surface(
                    onClick = onSeparateCurrentVideoAudio,
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceElevated,
                    border = BorderStroke(1.dp, AccentTeal),
                    modifier = Modifier.fillMaxWidth().testTag("audio_separate_current_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.ContentCut, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Separate Audio from Current Video", style = MaterialTheme.typography.labelMedium, color = AccentTeal, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        AudioMenuTab.BGM -> {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(StudioCatalog.bgmPresets, key = { it.id }) { preset ->
                    val isSel = track?.trackTitle == preset.title
                    Surface(
                        onClick = { onSelectBuiltInAudio(preset) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSel) Color(0xFF282046) else SurfaceElevated,
                        border = BorderStroke(1.dp, if (isSel) Color(0xFF8B6EFF) else BorderSubtle),
                        modifier = Modifier.width(142.dp).testTag("audio_bgm_${preset.id}")
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = Color(0xFF8B6EFF), modifier = Modifier.size(16.dp))
                                Text(preset.title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
                            }
                            Text(preset.subtitle, style = MaterialTheme.typography.labelSmall, color = TextMuted, maxLines = 1)
                        }
                    }
                }
            }
        }

        AudioMenuTab.RECORD -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (isRecording) {
                            isRecording = false
                            val recFile = micRecorder.stopRecording()
                            if (recFile != null) {
                                val elapsed = (System.currentTimeMillis() - recordStartMs).coerceAtLeast(1_000L)
                                onAttachRecordedAudio(recFile, elapsed)
                            }
                        } else {
                            val hasPerm = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                val res = micRecorder.startRecording()
                                if (res.isSuccess) {
                                    recordStartMs = System.currentTimeMillis()
                                    isRecording = true
                                }
                            } else {
                                recordPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) ErrorRed else Color(0xFF6C47FF),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("audio_record_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = if (isRecording) "Stop recording" else "Record voiceover",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isRecording) "Stop (${recordSeconds}s)" else "Record Mic")
                }

                Text(
                    text = when {
                        permissionDeniedNote -> "Mic permission denied. Enable in Settings."
                        isRecording -> "Recording PCM voiceover..."
                        else -> "Tap to record live voiceover"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (permissionDeniedNote) ErrorRed else TextSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Active Audio Track + Sound FX Studio (Reverb, Vocal Separation, Noise Reduction, Export Audio)
    if (track != null) {
        HorizontalDivider(color = BorderSubtle)
        var reverbSlider by remember(track.rawFilePath, track.reverbAmount) {
            mutableFloatStateOf(track.reverbAmount)
        }
        var vocalSep by remember(track.rawFilePath, track.vocalSeparation) {
            mutableStateOf(track.vocalSeparation)
        }
        var noiseRed by remember(track.rawFilePath, track.noiseReduction) {
            mutableStateOf(track.noiseReduction)
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SurfaceElevated,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            trackPlayer?.let { p ->
                                if (p.isPlaying) {
                                    p.pause()
                                    isPlayingTrack = false
                                } else {
                                    p.seekTo(0L)
                                    p.play()
                                    isPlayingTrack = true
                                }
                            }
                        },
                        modifier = Modifier.size(34.dp).testTag("audio_track_play_btn")
                    ) {
                        Icon(
                            imageVector = if (isPlayingTrack) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Preview audio track",
                            tint = AccentTeal
                        )
                    }
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 6.dp)) {
                        Text(
                            text = track.trackTitle,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${track.sourceType.label} · ${(track.durationMs / 1000L)}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    IconButton(onClick = onSaveProcessedAudio, modifier = Modifier.size(32.dp).testTag("audio_save_wav_btn")) {
                        Icon(Icons.Filled.Download, contentDescription = "Save audio file", tint = TextPrimary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onRemoveAudioTrack, modifier = Modifier.size(32.dp).testTag("audio_remove_track_btn")) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove audio track", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }

                // Hall Reverb Slider
                LabeledStudioSlider(
                    label = "Hall Reverb (Echo)",
                    value = reverbSlider,
                    valueRange = 0f..1f,
                    displayValue = "${(reverbSlider * 100).toInt()}%",
                    onValueChange = { reverbSlider = it }
                )

                // Vocal Separation & Noise Reduction + Apply FX
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { vocalSep = !vocalSep },
                        shape = RoundedCornerShape(999.dp),
                        color = if (vocalSep) AccentTeal.copy(alpha = 0.22f) else SurfaceCard,
                        border = BorderStroke(1.dp, if (vocalSep) AccentTeal else BorderSubtle),
                        modifier = Modifier.weight(1f).testTag("audio_vocal_sep_toggle")
                    ) {
                        Box(modifier = Modifier.padding(vertical = 7.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Vocal Isolation",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (vocalSep) AccentTeal else TextSecondary
                            )
                        }
                    }

                    Surface(
                        onClick = { noiseRed = !noiseRed },
                        shape = RoundedCornerShape(999.dp),
                        color = if (noiseRed) AccentTeal.copy(alpha = 0.22f) else SurfaceCard,
                        border = BorderStroke(1.dp, if (noiseRed) AccentTeal else BorderSubtle),
                        modifier = Modifier.weight(1f).testTag("audio_noise_red_toggle")
                    ) {
                        Box(modifier = Modifier.padding(vertical = 7.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Noise Reduce",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (noiseRed) AccentTeal else TextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = { onApplyAudioDsp(reverbSlider, vocalSep, noiseRed) },
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C47FF), contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        modifier = Modifier.height(34.dp).testTag("audio_apply_dsp_btn")
                    ) {
                        Text("Apply FX", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (isVideo) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                onClick = { onUpdateEditState(true) { it.copy(muteAudio = !it.muteAudio) } },
                shape = RoundedCornerShape(14.dp),
                color = if (editState.muteAudio) SurfaceBright else SurfaceElevated,
                modifier = Modifier.testTag("editor_mute_toggle")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (editState.muteAudio) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                        contentDescription = "Mute audio",
                        tint = if (editState.muteAudio) TextOnWhite else TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (editState.muteAudio) "Muted" else "Video Sound",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (editState.muteAudio) TextOnWhite else TextPrimary
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                LabeledStudioSlider(
                    label = "Volume",
                    value = if (editState.muteAudio) 0f else editState.audioVolume,
                    valueRange = 0f..1f,
                    displayValue = "${((if (editState.muteAudio) 0f else editState.audioVolume) * 100).toInt()}%",
                    onValueChange = { v ->
                        onUpdateEditState(false) { it.copy(audioVolume = v, muteAudio = v < 0.02f) }
                    }
                )
            }
        }
    }
}

@Composable
private fun TouchBrushPanel(
    hint: String,
    count: Int,
    brushSize: Float?,
    onBrushSizeChange: (Float) -> Unit,
    onClear: () -> Unit
) {
    if (brushSize != null) {
        LabeledStudioSlider(
            label = "Heal Brush Radius",
            value = brushSize,
            valueRange = 0.025f..0.14f,
            displayValue = "${(brushSize * 100).toInt()}",
            onValueChange = onBrushSizeChange
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = hint,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        if (count > 0) {
            TextButton(onClick = onClear) {
                Text("Clear ($count)", color = AccentTeal)
            }
        }
    }
}

@Composable
private fun TemplateQuickPanel(onApplyTemplate: (StudioTemplate) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(StudioCatalog.templates, key = { it.id }) { tpl ->
            Surface(
                onClick = { onApplyTemplate(tpl) },
                shape = RoundedCornerShape(14.dp),
                color = SurfaceElevated,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.width(124.dp)
            ) {
                Column {
                    Image(
                        painter = painterResource(id = tpl.previewRes),
                        contentDescription = tpl.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                    )
                    Text(
                        text = tpl.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        maxLines = 1,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RetouchPanel(
    activeTool: StudioToolId,
    editState: StudioEditState,
    onUpdateEditState: (Boolean, (StudioEditState) -> StudioEditState) -> Unit
) {
    when (activeTool) {
        StudioToolId.SKIN_SMOOTH, StudioToolId.AI_RETOUCH -> LabeledStudioSlider(
            label = "Skin Smoothness",
            value = editState.skinSmooth,
            valueRange = 0f..1f,
            displayValue = "${(editState.skinSmooth * 100).toInt()}%",
            onValueChange = { v -> onUpdateEditState(false) { it.copy(skinSmooth = v) } }
        )
        StudioToolId.FACE, StudioToolId.BRIGHTEN -> LabeledStudioSlider(
            label = "Face Radiance & Glow",
            value = editState.faceGlow,
            valueRange = 0f..1f,
            displayValue = "${(editState.faceGlow * 100).toInt()}%",
            onValueChange = { v -> onUpdateEditState(false) { it.copy(faceGlow = v) } }
        )
        StudioToolId.BODY -> LabeledStudioSlider(
            label = "Body Proportion Balance",
            value = editState.bodyBalance,
            valueRange = -0.5f..0.5f,
            displayValue = "${(editState.bodyBalance * 100).toInt()}",
            onValueChange = { v -> onUpdateEditState(false) { it.copy(bodyBalance = v) } }
        )
        StudioToolId.HAIR -> LabeledStudioSlider(
            label = "Hair Shine & Contrast",
            value = editState.hairGloss,
            valueRange = 0f..1f,
            displayValue = "${(editState.hairGloss * 100).toInt()}%",
            onValueChange = { v -> onUpdateEditState(false) { it.copy(hairGloss = v) } }
        )
        StudioToolId.MAKEUP -> LabeledStudioSlider(
            label = "Warm Lip & Cheek Tint",
            value = editState.makeupTint,
            valueRange = 0f..1f,
            displayValue = "${(editState.makeupTint * 100).toInt()}%",
            onValueChange = { v -> onUpdateEditState(false) { it.copy(makeupTint = v) } }
        )
        else -> Unit
    }
}

@Composable
internal fun LabeledStudioSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Text(text = displayValue, style = MaterialTheme.typography.labelSmall, color = TextPrimary)
        }
        StudioCircleSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioCircleSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    modifier: Modifier = Modifier
) {
    val sliderColors = SliderDefaults.colors(
        thumbColor = Color.White,
        activeTrackColor = Color.White,
        inactiveTrackColor = Color(0xFF3A373A)
    )
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        colors = sliderColors,
        thumb = {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.5.dp, Color(0xFF8B6EFF), CircleShape)
            )
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                colors = sliderColors,
                drawStopIndicator = null,
                modifier = Modifier.height(5.dp)
            )
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioCircleRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    modifier: Modifier = Modifier
) {
    val sliderColors = SliderDefaults.colors(
        thumbColor = Color.White,
        activeTrackColor = Color.White,
        inactiveTrackColor = Color(0xFF3A373A)
    )
    RangeSlider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        colors = sliderColors,
        startThumb = {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.5.dp, Color(0xFF8B6EFF), CircleShape)
            )
        },
        endThumb = {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.5.dp, Color(0xFF8B6EFF), CircleShape)
            )
        },
        track = { rangeSliderState ->
            SliderDefaults.Track(
                rangeSliderState = rangeSliderState,
                colors = sliderColors,
                drawStopIndicator = null,
                modifier = Modifier.height(5.dp)
            )
        },
        modifier = modifier
    )
}
