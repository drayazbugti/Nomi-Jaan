package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.R
import com.example.media.VideoEnhancerEngine
import com.example.model.AspectRatioOption
import com.example.model.BackgroundReplaceMode
import com.example.model.CanvasFitMode
import com.example.model.ClipTransitionAnimation
import com.example.model.EditorMainMode
import com.example.model.ExportResolution
import com.example.model.FxCategory
import com.example.model.MaskShapeType
import com.example.model.MediaSource
import com.example.model.NormCropRect
import com.example.model.SpeedCurvePreset
import com.example.model.SpeedModeTab
import com.example.model.StickerOverlayItem
import com.example.model.StudioCatalog
import com.example.model.StudioPreferences
import com.example.model.StudioToolId
import com.example.model.TextAnimationPreset
import com.example.model.TextArtPreset
import com.example.model.TextBoxStylePreset
import com.example.model.TextOverlayItem
import com.example.model.TimelineSegment
import com.example.model.TransitionMotionStyle
import com.example.ui.components.EditorContextualToolPanel
import com.example.ui.components.SpeedCurveAndRegularPopup
import com.example.ui.components.StudioCircleSlider
import com.example.ui.components.toolIconFor
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.SurfaceBright
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextOnWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.StudioUiState
import com.example.viewmodel.StudioViewModel
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(UnstableApi::class)
@Composable
fun EditorScreen(
    uiState: StudioUiState,
    preferences: StudioPreferences,
    viewModel: StudioViewModel,
    onBackClick: () -> Unit,
    onAddClipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)

    val context = LocalContext.current
    val mediaSource = uiState.activeMediaSource
    val isVideo = mediaSource is MediaSource.ContentUri && mediaSource.isVideo

    var isFullscreen by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    // Video playback state
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosMs by remember { mutableLongStateOf(0L) }
    val totalDurationMs = uiState.videoDurationMs.coerceAtLeast(1000L)

    val exoPlayer = remember(mediaSource) {
        if (mediaSource is MediaSource.ContentUri && mediaSource.isVideo) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(mediaSource.uri))
                repeatMode = Player.REPEAT_MODE_ALL
                playWhenReady = true
                prepare()
            }
        } else null
    }

    // Separate synchronized audio track ExoPlayer (when custom/extracted/BGM/recorded audio is attached)
    val customAudioTrack = uiState.editState.audioTrack
    val customAudioPlayer = remember(customAudioTrack?.processedFilePath) {
        val file = customAudioTrack?.processedFilePath?.let(::File)?.takeIf { it.exists() && it.length() > 44L }
        if (file != null) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
                repeatMode = Player.REPEAT_MODE_ALL
                playWhenReady = false
                prepare()
            }
        } else null
    }

    var loudnessEnhancer by remember(exoPlayer) { mutableStateOf<LoudnessEnhancer?>(null) }
    DisposableEffect(exoPlayer) {
        onDispose {
            runCatching { loudnessEnhancer?.release() }
            loudnessEnhancer = null
            exoPlayer?.release()
        }
    }
    DisposableEffect(customAudioPlayer) {
        onDispose { customAudioPlayer?.release() }
    }

    // Apply live Media3 effects whenever edits change for video
    LaunchedEffect(
        exoPlayer,
        uiState.editState.enhanceMode,
        uiState.editState.enhanceStrength,
        uiState.editState.filterPreset,
        uiState.editState.filterSpecId,
        uiState.editState.filterIntensity,
        uiState.editState.brightness,
        uiState.editState.contrast,
        uiState.editState.saturation,
        uiState.editState.warmth,
        uiState.editState.tint,
        uiState.editState.rotationDegrees,
        uiState.editState.straightenDegrees,
        uiState.editState.flipHorizontal,
        uiState.editState.flipVertical,
        uiState.editState.clipScaleX,
        uiState.editState.clipScaleY,
        uiState.editState.freeformCrop,
        uiState.editState.faceGlow,
        uiState.editState.hairGloss,
        uiState.editState.makeupTint,
        uiState.editState.stabilizeLevel,
        uiState.editState.stabilizeHorizonLock,
        uiState.editState.cameraTrackingEnabled,
        uiState.editState.cameraTrackingStrength,
        uiState.editState.trackingAnchorX,
        uiState.editState.trackingAnchorY,
        uiState.isHoldingCompare
    ) {
        exoPlayer?.let { player ->
            val effects = if (uiState.isHoldingCompare) {
                emptyList()
            } else {
                VideoEnhancerEngine.buildMedia3Effects(uiState.editState, isExport = false)
            }
            player.setVideoEffects(effects)
        }
    }

    LaunchedEffect(
        exoPlayer,
        customAudioPlayer,
        uiState.editState.playbackSpeed,
        uiState.editState.muteAudio,
        uiState.editState.audioVolume,
        uiState.editState.audioTrack?.volume,
        isPlaying
    ) {
        exoPlayer?.let { player ->
            player.playbackParameters = PlaybackParameters(uiState.editState.playbackSpeed)
            val vol = if (uiState.editState.muteAudio || customAudioTrack != null) 0f else uiState.editState.audioVolume
            player.volume = vol.coerceIn(0f, 1f)
            val sessionId = player.audioSessionId
            if (sessionId != C.AUDIO_SESSION_ID_UNSET && sessionId != 0) {
                runCatching {
                    val enhancer = loudnessEnhancer ?: LoudnessEnhancer(sessionId).also { loudnessEnhancer = it }
                    if (vol > 1.02f) {
                        val gainMb = ((vol - 1f) * 900f).roundToInt().coerceIn(0, 1800)
                        enhancer.setTargetGain(gainMb)
                        enhancer.enabled = true
                    } else {
                        enhancer.setTargetGain(0)
                        enhancer.enabled = false
                    }
                }
            }
        }
        customAudioPlayer?.let { audioPlayer ->
            val aVol = if (uiState.editState.muteAudio) 0f else (uiState.editState.audioTrack?.volume ?: 0.9f)
            audioPlayer.volume = aVol.coerceIn(0f, 1f)
            audioPlayer.playbackParameters = PlaybackParameters(uiState.editState.playbackSpeed)
        }
    }

    // Poll playback position + dynamic Speed Curve modulation
    val latestEditState by rememberUpdatedState(uiState.editState)
    var lastCurveAppliedSpeed by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(exoPlayer, isPlaying) {
        while (exoPlayer != null) {
            currentPosMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            isPlaying = exoPlayer.isPlaying
            val st = latestEditState
            if (isPlaying && st.speedModeTab == SpeedModeTab.CURVE && st.speedCurvePreset != SpeedCurvePreset.ORIGINAL) {
                val frac = (currentPosMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
                val targetSpeed = evaluateCurveSpeed(st.customSpeedCurvePoints, frac)
                if (abs(targetSpeed - lastCurveAppliedSpeed) > 0.04f) {
                    lastCurveAppliedSpeed = targetSpeed
                    exoPlayer.playbackParameters = PlaybackParameters(targetSpeed)
                    customAudioPlayer?.playbackParameters = PlaybackParameters(targetSpeed)
                }
            }
            delay(100L)
        }
    }

    // Simulated scrubbing timeline position for photo mode
    var photoTimelineFraction by remember { mutableFloatStateOf(0.25f) }
    val activePlayheadFraction = if (isVideo) {
        (currentPosMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        photoTimelineFraction
    }

    // Pinch-to-zoom & pan state for interactive preview canvas
    var previewZoom by remember(mediaSource) { mutableFloatStateOf(1f) }
    var previewOffsetX by remember(mediaSource) { mutableFloatStateOf(0f) }
    var previewOffsetY by remember(mediaSource) { mutableFloatStateOf(0f) }
    var tapFeedbackNonce by remember { mutableIntStateOf(0) }
    var showTapPlayPauseBadge by remember { mutableStateOf(false) }

    LaunchedEffect(tapFeedbackNonce) {
        if (tapFeedbackNonce > 0) {
            delay(650L)
            showTapPlayPauseBadge = false
        }
    }

    fun toggleCenterPlayPause() {
        if (isVideo && exoPlayer != null) {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
                customAudioPlayer?.pause()
                isPlaying = false
            } else {
                exoPlayer.play()
                customAudioPlayer?.play()
                isPlaying = true
            }
        } else {
            isPlaying = !isPlaying
            customAudioPlayer?.let { p ->
                if (p.isPlaying) p.pause() else p.play()
            }
        }
        showTapPlayPauseBadge = true
        tapFeedbackNonce++
    }

    // One-shot replayable clip transition animation + automatic segment-junction trigger
    var transitionReplayNonce by remember { mutableIntStateOf(0) }
    var activeBoundaryTransitionId by remember { mutableStateOf<String?>(null) }
    var activeBoundaryDurationMs by remember { mutableIntStateOf(600) }
    var lastCrossedJunction by remember { mutableIntStateOf(-1) }

    LaunchedEffect(activePlayheadFraction, uiState.editState.timelineSegments, isPlaying) {
        val segs = uiState.editState.timelineSegments
        if (segs.size >= 2) {
            var hitIdx = -1
            for (idx in 0 until segs.lastIndex) {
                val boundary = segs[idx].endFraction
                if (abs(activePlayheadFraction - boundary) < 0.025f) {
                    hitIdx = idx
                    break
                }
            }
            if (hitIdx >= 0 && hitIdx != lastCrossedJunction) {
                lastCrossedJunction = hitIdx
                val transId = segs[hitIdx].transitionAfterId
                if (transId != "none") {
                    activeBoundaryTransitionId = transId
                    activeBoundaryDurationMs = segs[hitIdx].transitionAfterDurationMs
                    transitionReplayNonce++
                }
            } else if (hitIdx < 0) {
                lastCrossedJunction = -1
            }
        }
    }

    val transitionAnim = remember { Animatable(1f) }
    val effectiveTransitionId = activeBoundaryTransitionId ?: uiState.editState.transitionEffectId
    val effectiveTransitionDuration = if (activeBoundaryTransitionId != null) {
        activeBoundaryDurationMs
    } else {
        uiState.editState.animateDurationMs
    }
    LaunchedEffect(
        effectiveTransitionId,
        uiState.editState.clipAnimation,
        effectiveTransitionDuration,
        transitionReplayNonce
    ) {
        val hasTransition = effectiveTransitionId != "none" ||
            uiState.editState.clipAnimation != ClipTransitionAnimation.NONE
        if (hasTransition) {
            transitionAnim.snapTo(0f)
            transitionAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = effectiveTransitionDuration.coerceIn(200, 2000))
            )
            if (activeBoundaryTransitionId != null) {
                activeBoundaryTransitionId = null
            }
        } else {
            transitionAnim.snapTo(1f)
        }
    }
    val transitionProgress = transitionAnim.value

    // Live FX infinite phase ONLY runs when a non-none FX effect is actively selected
    val activeFxSpec = remember(uiState.editState.fxEffectId) {
        StudioCatalog.fxEffects.firstOrNull {
            it.id == uiState.editState.fxEffectId && it.id != "fx_none" && it.id != "fx_body_none"
        }
    }
    val fxPhase = if (activeFxSpec != null) {
        val fxInf = rememberInfiniteTransition(label = "fx_live_loop")
        val value by fxInf.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200),
                repeatMode = RepeatMode.Restart
            ),
            label = "fx_phase"
        )
        value
    } else {
        0f
    }

    // Text overlay animation loop runs only if a text box explicitly uses a text animation preset
    val hasAnimatedText = uiState.editState.textOverlays.any { it.animationPreset != TextAnimationPreset.NONE }
    val textPulseProgress = if (hasAnimatedText) {
        val infiniteAnim = rememberInfiniteTransition(label = "text_anim_loop")
        val value by infiniteAnim.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800),
                repeatMode = RepeatMode.Restart
            ),
            label = "text_pulse_progress"
        )
        value
    } else {
        1f
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ==================== TOP BAR (Back, Undo, Redo, Slider Compare, Compare, Save) ====================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("editor_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                IconButton(
                    onClick = { viewModel.undoLastEdit() },
                    enabled = uiState.canUndo,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("editor_undo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo edit",
                        tint = if (uiState.canUndo) TextPrimary else TextMuted.copy(alpha = 0.45f)
                    )
                }

                IconButton(
                    onClick = { viewModel.redoLastEdit() },
                    enabled = uiState.canRedo,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("editor_redo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo edit",
                        tint = if (uiState.canRedo) TextPrimary else TextMuted.copy(alpha = 0.45f)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Before/After Interactive Slider Toggle (Photo mode)
                if (!isVideo && uiState.originalBitmap != null) {
                    Surface(
                        onClick = { viewModel.toggleEditorCompareSlider() },
                        shape = RoundedCornerShape(999.dp),
                        color = if (uiState.isEditorCompareSliderActive) AccentPurple else SurfaceElevated,
                        border = BorderStroke(1.dp, if (uiState.isEditorCompareSliderActive) AccentPurple else BorderSubtle),
                        modifier = Modifier.testTag("editor_split_compare_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VerticalSplit,
                                contentDescription = "Toggle before and after slider",
                                tint = if (uiState.isEditorCompareSliderActive) Color.White else TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Slider",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.isEditorCompareSliderActive) Color.White else TextSecondary
                            )
                        }
                    }
                }

                // Hold-to-Compare pill
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = SurfaceElevated,
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    viewModel.setHoldingCompare(true)
                                    tryAwaitRelease()
                                    viewModel.setHoldingCompare(false)
                                }
                            )
                        }
                        .testTag("editor_compare_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Compare,
                            contentDescription = "Hold to compare original",
                            tint = if (uiState.isHoldingCompare) AccentTeal else TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (uiState.isHoldingCompare) "Orig" else "Compare",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.isHoldingCompare) AccentTeal else TextSecondary
                        )
                    }
                }

                // White "Save" Pill
                Button(
                    onClick = { viewModel.exportCurrentCreation() },
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceBright,
                        contentColor = TextOnWhite
                    ),
                    contentPadding = PaddingValues(horizontal = 15.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("editor_save_button")
                ) {
                    Text(
                        text = stringResource(R.string.action_save),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Overflow Menu (...)
                Box {
                    Surface(
                        onClick = { showOverflowMenu = true },
                        shape = RoundedCornerShape(999.dp),
                        color = SurfaceBright,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("editor_more_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More export options",
                                tint = TextOnWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false },
                        containerColor = SurfaceCard
                    ) {
                        ExportResolution.entries.forEach { res ->
                            val selected = res == uiState.editState.exportResolution
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${if (selected) "✓ " else ""}Export: ${res.label}",
                                        color = if (selected) AccentTeal else TextPrimary
                                    )
                                },
                                onClick = {
                                    viewModel.setExportResolution(res)
                                    showOverflowMenu = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Reset All Edits", color = TextPrimary) },
                            onClick = {
                                viewModel.resetAllEdits()
                                showOverflowMenu = false
                            }
                        )
                    }
                }
            }
        }

        // ==================== MAIN MEDIA CANVAS ====================
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = if (isFullscreen) 0.dp else 14.dp),
            contentAlignment = Alignment.Center
        ) {
            val canvasWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
            val canvasHeightPx = with(LocalDensity.current) { maxHeight.toPx() }

            val activeTransition = remember(effectiveTransitionId) {
                StudioCatalog.transitionEffects.firstOrNull { it.id == effectiveTransitionId }
            }

            // Compute fade envelope alpha for video/photo Fade In & Fade Out
            val posMs = (activePlayheadFraction * totalDurationMs).toLong()
            val remainingMs = (totalDurationMs - posMs).coerceAtLeast(0L)
            val fadeAlpha = run {
                var a = 1f
                if (uiState.editState.videoFadeInMs > 0 && posMs < uiState.editState.videoFadeInMs) {
                    a *= (posMs.toFloat() / uiState.editState.videoFadeInMs).coerceIn(0.05f, 1f)
                }
                if (uiState.editState.videoFadeOutMs > 0 && remainingMs < uiState.editState.videoFadeOutMs) {
                    a *= (remainingMs.toFloat() / uiState.editState.videoFadeOutMs).coerceIn(0.05f, 1f)
                }
                a
            }

            val clipAnimModifier = Modifier.graphicsLayer {
                var sx = previewZoom
                var sy = previewZoom
                if (uiState.isMagnifierEnabled) {
                    sx *= 2.0f
                    sy *= 2.0f
                }
                translationX = previewOffsetX + uiState.editState.clipOffsetX * canvasWidthPx
                translationY = previewOffsetY + uiState.editState.clipOffsetY * canvasHeightPx
                alpha = fadeAlpha

                // Live FX motion (when an FX effect card is selected)
                if (activeFxSpec != null && !uiState.isHoldingCompare) {
                    val fxStr = uiState.editState.fxIntensity
                    when {
                        activeFxSpec.id.startsWith("fx_spin_") || activeFxSpec.category == FxCategory.ROTATE -> {
                            rotationZ += sin(fxPhase * 2f * PI.toFloat()) * (14f * fxStr)
                        }
                        activeFxSpec.id.startsWith("fx_drop_") -> {
                            translationY += abs(sin(fxPhase * 2f * PI.toFloat())) * (18f * fxStr)
                        }
                        activeFxSpec.category == FxCategory.BEATS -> {
                            val pulse = 1f + sin(fxPhase * 4f * PI.toFloat()) * (0.07f * fxStr)
                            sx *= pulse
                            sy *= pulse
                        }
                        activeFxSpec.category == FxCategory.GLITCH -> {
                            if (fxPhase < 0.25f) {
                                translationX += sin(fxPhase * 80f) * (10f * fxStr)
                            }
                        }
                    }
                }

                if (transitionProgress < 0.999f) {
                    when (uiState.editState.clipAnimation) {
                        ClipTransitionAnimation.NONE -> Unit
                        ClipTransitionAnimation.FADE_IN -> alpha *= (0.25f + 0.75f * transitionProgress)
                        ClipTransitionAnimation.ZOOM_IN -> {
                            val s = 0.88f + 0.12f * transitionProgress
                            sx *= s
                            sy *= s
                        }
                        ClipTransitionAnimation.SLIDE_LEFT -> {
                            translationX += (1f - transitionProgress) * 44f
                        }
                        ClipTransitionAnimation.NEON_FLASH -> {
                            alpha *= if (transitionProgress < 0.18f) 0.45f else 1f
                        }
                    }

                    when (activeTransition?.motionStyle) {
                        null -> Unit
                        TransitionMotionStyle.FADE_PULSE -> {
                            alpha *= (0.25f + 0.75f * transitionProgress)
                        }
                        TransitionMotionStyle.ZOOM_BURST -> {
                            val z = 0.85f + 0.15f * transitionProgress
                            sx *= z
                            sy *= z
                        }
                        TransitionMotionStyle.SLIDE_LEFT -> {
                            translationX += (1f - transitionProgress) * 56f
                        }
                        TransitionMotionStyle.SLIDE_RIGHT -> {
                            translationX -= (1f - transitionProgress) * 56f
                        }
                        TransitionMotionStyle.SLIDE_UP -> {
                            translationY += (1f - transitionProgress) * 48f
                        }
                        TransitionMotionStyle.SLIDE_DOWN -> {
                            translationY -= (1f - transitionProgress) * 48f
                        }
                        TransitionMotionStyle.SPIN_TWIST -> {
                            rotationZ += (1f - transitionProgress) * 14f
                            val s = 0.92f + 0.08f * transitionProgress
                            sx *= s
                            sy *= s
                        }
                        TransitionMotionStyle.GLITCH_JITTER -> {
                            if (transitionProgress < 0.35f) {
                                translationX += sin(transitionProgress * 60f) * 12f
                                alpha *= 0.82f
                            }
                        }
                        TransitionMotionStyle.FLASH_GLOW -> {
                            alpha *= if (transitionProgress < 0.22f) 0.45f else 1f
                        }
                        TransitionMotionStyle.PAN_SWAY -> {
                            translationX += sin(transitionProgress * 6.283f) * 16f
                        }
                    }
                }

                scaleX = sx
                scaleY = sy
            }

            val isBrushActive = !isVideo && !uiState.isEditorCompareSliderActive &&
                (uiState.selectedEditorTool == StudioToolId.MOSAIC ||
                    uiState.selectedEditorTool == StudioToolId.REMOVAL ||
                    uiState.selectedEditorTool == StudioToolId.AI_REMOVAL)
            val isCameraTrackTapMode = uiState.selectedEditorTool == StudioToolId.CAMERA_TRACKING
            val isPositionDragMode = uiState.selectedEditorTool == StudioToolId.POSITION

            val canvasBackdropColor = when (uiState.editState.canvasFitMode) {
                CanvasFitMode.WHITE_CANVAS -> Color.White
                CanvasFitMode.BLUR_FIT -> Color(0xFF1F1930)
                else -> Color.Black
            }

            val latestCanvasWidthPx by rememberUpdatedState(canvasWidthPx)
            val latestCanvasHeightPx by rememberUpdatedState(canvasHeightPx)
            val latestClipOffsetX by rememberUpdatedState(uiState.editState.clipOffsetX)
            val latestClipOffsetY by rememberUpdatedState(uiState.editState.clipOffsetY)
            val interactiveViewportModifier = Modifier
                .pointerInput(isPositionDragMode) {
                    if (isPositionDragMode) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val nx = latestClipOffsetX + dragAmount.x / latestCanvasWidthPx.coerceAtLeast(1f)
                            val ny = latestClipOffsetY + dragAmount.y / latestCanvasHeightPx.coerceAtLeast(1f)
                            viewModel.setClipPosition(nx, ny, recordUndo = false)
                        }
                    }
                }
                .pointerInput(isBrushActive, isCameraTrackTapMode, isPositionDragMode, uiState.isEditorCompareSliderActive) {
                    if (!isBrushActive && !isCameraTrackTapMode && !isPositionDragMode && !uiState.isEditorCompareSliderActive) {
                        detectTransformGestures(panZoomLock = true) { _, pan, zoom, _ ->
                            val zoomed = abs(zoom - 1f) > 0.008f
                            val isAtDefault = abs(previewZoom - 1f) < 0.02f &&
                                abs(previewOffsetX) < 1f && abs(previewOffsetY) < 1f
                            if (isAtDefault && !zoomed && pan.getDistance() < 4f) {
                                return@detectTransformGestures
                            }
                            var nextZoom = (previewZoom * zoom).coerceIn(0.75f, 4.0f)
                            if (nextZoom in 0.97f..1.03f && !zoomed) {
                                nextZoom = 1f
                            }
                            previewZoom = nextZoom
                            if (nextZoom == 1f && isAtDefault && !zoomed) {
                                previewOffsetX = 0f
                                previewOffsetY = 0f
                            } else {
                                previewOffsetX = (previewOffsetX + pan.x)
                                    .coerceIn(-latestCanvasWidthPx * 0.75f, latestCanvasWidthPx * 0.75f)
                                previewOffsetY = (previewOffsetY + pan.y)
                                    .coerceIn(-latestCanvasHeightPx * 0.75f, latestCanvasHeightPx * 0.75f)
                            }
                        }
                    }
                }
                .pointerInput(isVideo, exoPlayer, customAudioPlayer, isBrushActive, isCameraTrackTapMode, uiState.isEditorCompareSliderActive) {
                    if (!uiState.isEditorCompareSliderActive && !isBrushActive) {
                        detectTapGestures(
                            onTap = { tapPos ->
                                if (isCameraTrackTapMode) {
                                    viewModel.setTrackingAnchor(
                                        tapPos.x / size.width.toFloat().coerceAtLeast(1f),
                                        tapPos.y / size.height.toFloat().coerceAtLeast(1f)
                                    )
                                } else if (isVideo) {
                                    toggleCenterPlayPause()
                                } else {
                                    viewModel.selectTextOverlay(null)
                                    viewModel.selectStickerOverlay(null)
                                }
                            },
                            onDoubleTap = {
                                previewZoom = 1f
                                previewOffsetX = 0f
                                previewOffsetY = 0f
                            }
                        )
                    }
                }

            val selectedSeg = uiState.editState.timelineSegments.firstOrNull {
                it.id == uiState.editState.selectedSegmentId
            }

            if (isVideo && exoPlayer != null) {
                val targetResizeMode = when (uiState.editState.canvasFitMode) {
                    CanvasFitMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    CanvasFitMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(if (isFullscreen) 0.dp else 14.dp))
                        .background(canvasBackdropColor)
                        .then(interactiveViewportModifier)
                        .testTag("editor_video_preview_surface")
                ) {
                    AndroidView(
                        factory = { ctx ->
                            (LayoutInflater.from(ctx).inflate(
                                R.layout.studio_texture_player_view,
                                null,
                                false
                            ) as PlayerView).apply {
                                player = exoPlayer
                                useController = false
                                resizeMode = targetResizeMode
                                isClickable = false
                                isFocusable = false
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        update = { view ->
                            if (view.player != exoPlayer) view.player = exoPlayer
                            if (view.resizeMode != targetResizeMode) view.resizeMode = targetResizeMode
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .then(clipAnimModifier)
                    )

                    if (selectedSeg?.isFrozen == true && !selectedSeg.frozenFramePath.isNullOrBlank()) {
                        val freezeBmp = remember(selectedSeg.frozenFramePath) {
                            runCatching { BitmapFactory.decodeFile(selectedSeg.frozenFramePath) }.getOrNull()
                        }
                        if (freezeBmp != null) {
                            Image(
                                bitmap = remember(freezeBmp) { freezeBmp.asImageBitmap() },
                                contentDescription = "Frozen still video frame",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black)
                                    .then(clipAnimModifier)
                            )
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color(0xCC121112),
                                border = BorderStroke(1.dp, AccentTeal),
                                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
                            ) {
                                Text(
                                    text = "FROZEN STILL · ${selectedSeg.freezeDurationMs / 1000}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentTeal,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                val processedBmp = uiState.processedBitmap ?: uiState.originalBitmap
                val origBmp = uiState.originalBitmap

                if (processedBmp != null) {
                    val activeBrushTool = uiState.selectedEditorTool
                    val photoContentScale = when (uiState.editState.canvasFitMode) {
                        CanvasFitMode.FILL -> ContentScale.Crop
                        CanvasFitMode.STRETCH -> ContentScale.FillBounds
                        else -> ContentScale.Fit
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(if (isFullscreen) 0.dp else 14.dp))
                            .background(canvasBackdropColor)
                            .then(interactiveViewportModifier)
                            .pointerInput(activeBrushTool, uiState.isEditorCompareSliderActive) {
                                if (!uiState.isEditorCompareSliderActive &&
                                    (activeBrushTool == StudioToolId.MOSAIC ||
                                        activeBrushTool == StudioToolId.REMOVAL ||
                                        activeBrushTool == StudioToolId.AI_REMOVAL)
                                ) {
                                    detectTapGestures { offset ->
                                        val nx = offset.x / size.width.toFloat()
                                        val ny = offset.y / size.height.toFloat()
                                        if (activeBrushTool == StudioToolId.MOSAIC) {
                                            viewModel.addMosaicPoint(nx, ny)
                                        } else {
                                            viewModel.addRemovalPoint(nx, ny)
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val needsTransparencyCheck =
                            ((uiState.editState.removeBackground || uiState.editState.chromaEnabled) &&
                                uiState.editState.bgReplaceMode == BackgroundReplaceMode.TRANSPARENT) ||
                                uiState.editState.maskShape != MaskShapeType.NONE
                        if (needsTransparencyCheck && !uiState.isHoldingCompare) {
                            TransparencyCheckerboard(modifier = Modifier.fillMaxSize())
                        }
                        if (uiState.isEditorCompareSliderActive && origBmp != null) {
                            InteractivePhotoCompareCanvas(
                                originalBitmap = origBmp,
                                enhancedBitmap = processedBmp,
                                splitFraction = uiState.editorCompareSplitFraction,
                                onSplitChange = { viewModel.updateEditorCompareSplit(it) },
                                modifier = Modifier.fillMaxSize().then(clipAnimModifier)
                            )
                        } else {
                            val showBitmap = if (uiState.isHoldingCompare) origBmp ?: processedBmp else processedBmp
                            val memoizedImageBitmap = remember(showBitmap) { showBitmap.asImageBitmap() }
                            Image(
                                bitmap = memoizedImageBitmap,
                                contentDescription = "Studio edited preview",
                                contentScale = photoContentScale,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(clipAnimModifier)
                            )
                        }
                    }
                } else if (uiState.isProcessingPreview) {
                    CircularProgressIndicator(color = SurfaceBright)
                }
            }

            // Interactive Shape / Linear Mask Guide when Mask tool is selected
            if (uiState.selectedEditorTool == StudioToolId.MASK && uiState.editState.maskShape != MaskShapeType.NONE) {
                MaskCanvasGuide(
                    shape = uiState.editState.maskShape,
                    centerX = uiState.editState.maskCenterX,
                    centerY = uiState.editState.maskCenterY,
                    scale = uiState.editState.maskScale,
                    rotation = uiState.editState.maskRotation,
                    canvasWidthPx = canvasWidthPx,
                    canvasHeightPx = canvasHeightPx,
                    onTransformMask = { nx, ny, nextScale, nextRot ->
                        viewModel.setMaskPosition(nx, ny, nextScale, nextRot)
                    }
                )
            }

            // Camera Tracking Lock Reticle when Camera Tracking is active
            if (uiState.selectedEditorTool == StudioToolId.CAMERA_TRACKING || uiState.editState.cameraTrackingEnabled) {
                CameraTrackingReticle(
                    anchorX = uiState.editState.trackingAnchorX,
                    anchorY = uiState.editState.trackingAnchorY,
                    canvasWidthPx = canvasWidthPx,
                    canvasHeightPx = canvasHeightPx
                )
            }

            // Freeform Crop Interactive Handles Overlay
            if (uiState.editState.aspectRatio == AspectRatioOption.FREEFORM) {
                FreeformCropOverlay(
                    crop = uiState.editState.freeformCrop,
                    canvasWidthPx = canvasWidthPx,
                    canvasHeightPx = canvasHeightPx,
                    onUpdateCrop = { nextCrop ->
                        viewModel.updateFreeformCrop(nextCrop, recordUndo = false)
                    }
                )
            }

            // Interactive Draggable / Pinch-Resize / Rotate / Styled / Animated Text Overlays
            uiState.editState.textOverlays.forEach { item ->
                InteractiveTextOverlayBox(
                    item = item,
                    isSelected = item.id == uiState.selectedTextOverlayId,
                    canvasWidthPx = canvasWidthPx,
                    canvasHeightPx = canvasHeightPx,
                    animPulse = textPulseProgress,
                    onSelect = {
                        viewModel.selectTextOverlay(item.id)
                        if (uiState.selectedEditorTool != StudioToolId.TEXT) {
                            viewModel.selectEditorTool(StudioToolId.TEXT)
                        }
                    },
                    onTransform = { nextX, nextY, nextScale, nextRot ->
                        viewModel.transformTextOverlay(item.id, nextX, nextY, nextScale, nextRot)
                    },
                    onRemove = { viewModel.removeTextOverlay(item.id) }
                )
            }

            // Interactive Pinch-Resize / Rotate / Draggable Offline Sticker Overlays
            uiState.editState.stickerOverlays.forEach { sticker ->
                InteractiveStickerOverlayBox(
                    sticker = sticker,
                    isSelected = sticker.id == uiState.selectedStickerId,
                    canvasWidthPx = canvasWidthPx,
                    canvasHeightPx = canvasHeightPx,
                    onSelect = { viewModel.selectStickerOverlay(sticker.id) },
                    onTransform = { nextX, nextY, nextScale, nextRot ->
                        viewModel.transformStickerOverlay(sticker.id, nextX, nextY, nextScale, nextRot)
                    },
                    onRemove = { viewModel.removeStickerOverlay(sticker.id) }
                )
            }

            // Center Play/Pause Tap HUD Feedback
            androidx.compose.animation.AnimatedVisibility(
                visible = showTapPlayPauseBadge && isVideo,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xB3121112),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.7f)),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = if (isPlaying) "Playing" else "Paused",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }

            // Reset Zoom/Pan pill when pinched or panned
            if (abs(previewZoom - 1f) > 0.04f || abs(previewOffsetX) > 4f || abs(previewOffsetY) > 4f) {
                Surface(
                    onClick = {
                        previewZoom = 1f
                        previewOffsetX = 0f
                        previewOffsetY = 0f
                    },
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xCC121112),
                    border = BorderStroke(1.dp, AccentPurple),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .testTag("editor_reset_zoom_btn")
                ) {
                    Text(
                        text = "Zoom ${(previewZoom * 100).toInt()}% · Reset",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            if (activeFxSpec != null && !uiState.isHoldingCompare &&
                (activeFxSpec.tab == com.example.model.FxTab.BODY || activeFxSpec.category == FxCategory.GLOW)
            ) {
                val fxAccent = Color(activeFxSpec.accentArgb)
                val ringAlpha = (0.35f + 0.35f * sin(fxPhase * 2f * PI.toFloat())) * uiState.editState.fxIntensity
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Transparent, fxAccent.copy(alpha = ringAlpha.coerceIn(0f, 0.7f)), Color.Transparent),
                            center = center,
                            radius = minOf(size.width, size.height) * 0.45f
                        ),
                        radius = minOf(size.width, size.height) * 0.45f
                    )
                }
            }

            if (uiState.isMagnifierEnabled) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp),
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xCC121112),
                    border = BorderStroke(1.dp, AccentTeal)
                ) {
                    Text(
                        text = "2.0x HD Loupe Active",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentTeal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            if (uiState.isProcessingPreview && uiState.processedBitmap != null) {
                CircularProgressIndicator(
                    color = SurfaceBright,
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(20.dp)
                )
            }
        }

        // ==================== PLAYBACK / SCRUB BAR ====================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { toggleCenterPlayPause() },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("editor_play_pause_btn")
            ) {
                Icon(
                    imageVector = if ((isVideo && isPlaying) || customAudioPlayer?.isPlaying == true) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play or pause",
                    tint = TextPrimary
                )
            }

            val currentSec = if (isVideo) currentPosMs / 1000 else (photoTimelineFraction * 19).roundToInt().toLong()
            val totalSec = if (isVideo) totalDurationMs / 1000 else 19L
            Text(
                text = "${formatSeconds(currentSec)}/${formatSeconds(totalSec)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                modifier = Modifier.padding(end = 10.dp)
            )

            StudioCircleSlider(
                value = activePlayheadFraction,
                onValueChange = { f ->
                    if (isVideo && exoPlayer != null) {
                        exoPlayer.seekTo((f * totalDurationMs).toLong())
                    } else {
                        photoTimelineFraction = f
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("editor_playback_slider")
            )

            IconButton(
                onClick = { isFullscreen = !isFullscreen },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("editor_fullscreen_btn")
            ) {
                Icon(
                    imageVector = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                    contentDescription = "Toggle fullscreen preview",
                    tint = TextPrimary
                )
            }
        }

        if (!isFullscreen) {
            // ==================== MULTI-SEGMENT TIMELINE FILMSTRIP + TRANSITION JUNCTIONS ====================
            EditorTimelineStrip(
                isVideo = isVideo,
                frames = uiState.videoFrames,
                previewBitmap = uiState.processedBitmap ?: uiState.originalBitmap,
                selectedCoverIndex = uiState.selectedCoverFrameIndex,
                segments = uiState.editState.timelineSegments,
                selectedSegmentId = uiState.editState.selectedSegmentId,
                playheadFraction = activePlayheadFraction,
                onSelectSegment = { viewModel.selectTimelineSegment(it) },
                onOpenTransitionJunction = { junctionIdx -> viewModel.openTransitionForJunction(junctionIdx) },
                onAddClipClick = onAddClipClick,
                onCoverClick = {
                    val nextIdx = if (uiState.videoFrames.isEmpty()) 0 else (uiState.selectedCoverFrameIndex + 1) % uiState.videoFrames.size
                    viewModel.selectCoverFrame(nextIdx)
                }
            )

            if (customAudioTrack != null) {
                Surface(
                    onClick = {
                        if (uiState.selectedEditorTool != StudioToolId.AUDIO) {
                            viewModel.selectEditorTool(StudioToolId.AUDIO)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF282046),
                    border = BorderStroke(1.dp, AccentPurple),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp)
                        .testTag("editor_timeline_audio_lane")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Text(
                                text = "${customAudioTrack.trackTitle} (${customAudioTrack.sourceType.label})",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (customAudioTrack.reverbAmount > 0.02f || customAudioTrack.vocalSeparation || customAudioTrack.noiseReduction) {
                            Text(
                                text = "FX On",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentTeal
                            )
                        }
                    }
                }
            }

            // ==================== CONTEXTUAL TOOL PANEL ====================
            AnimatedVisibility(
                visible = uiState.selectedEditorTool != null && uiState.selectedEditorTool != StudioToolId.MAGNIFIER,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                uiState.selectedEditorTool?.let { activeTool ->
                    EditorContextualToolPanel(
                        tool = activeTool,
                        isVideo = isVideo,
                        isPlaying = isPlaying,
                        playheadFraction = activePlayheadFraction,
                        totalDurationMs = totalDurationMs,
                        editState = uiState.editState,
                        selectedTextOverlayId = uiState.selectedTextOverlayId,
                        selectedStickerId = uiState.selectedStickerId,
                        selectedTransitionJunctionIndex = uiState.selectedTransitionJunctionIndex,
                        favoriteFontIds = preferences.favoriteFontIds,
                        silenceAnalysis = uiState.silenceAnalysis,
                        silenceThresholdDb = uiState.silenceThresholdDb,
                        silenceMinMs = uiState.silenceMinMs,
                        onTogglePlayPause = { toggleCenterPlayPause() },
                        onSelectTool = { toolId -> viewModel.selectEditorTool(toolId) },
                        onOpenSpeedPopup = { viewModel.openSpeedPopup() },
                        onUpdateEditState = { rec, fn -> viewModel.updateEditState(rec, fn) },
                        onSelectEnhanceMode = { viewModel.setEnhanceMode(it) },
                        onSelectFilter = { viewModel.setFilterPreset(it) },
                        onSelectFilterSpec = { viewModel.setFilterSpec(it) },
                        onSelectEffect = { viewModel.setEffectMode(it) },
                        onSelectFxEffect = { viewModel.setFxEffect(it) },
                        onSelectTransitionEffect = { viewModel.setTransitionEffect(it) },
                        onApplyTransitionToAllJunctions = { id, dur ->
                            viewModel.applyTransitionToAllJunctions(id, dur)
                        },
                        onReplayTransition = { transitionReplayNonce++ },
                        onSelectOverlay = { viewModel.setOverlayMode(it) },
                        onSetOverlayImageUri = { uri -> viewModel.setOverlayImageUri(uri) },
                        onSelectAspectRatio = { viewModel.setAspectRatio(it) },
                        onResetFreeformCrop = { viewModel.updateFreeformCrop(NormCropRect(), recordUndo = true) },
                        onSelectFrame = { viewModel.setFrameStyle(it) },
                        onRotateLeft90 = { viewModel.rotateLeft90() },
                        onRotate90 = { viewModel.rotate90() },
                        onRotate180 = { viewModel.rotate180() },
                        onSetStraighten = { deg, rec -> viewModel.setStraightenDegrees(deg, rec) },
                        onResetRotate = { viewModel.resetRotationAndFlip() },
                        onFlipHorizontal = { viewModel.toggleFlipHorizontal() },
                        onFlipVertical = { viewModel.toggleFlipVertical() },
                        onSetClipPosition = { x, y, rec -> viewModel.setClipPosition(x, y, rec) },
                        onSetClipSize = { w, h, rec -> viewModel.setClipSize(w, h, rec) },
                        onSelectFitMode = { mode -> viewModel.setCanvasFitMode(mode) },
                        onSetVideoFade = { fin, fout, rec -> viewModel.setVideoFade(fin, fout, rec) },
                        onSelectSpeedCurvePreset = { preset -> viewModel.setSpeedCurvePreset(preset) },
                        onApplyBgRemoval = { mode -> viewModel.applyBackgroundRemoval(mode) },
                        onClearBgRemoval = { viewModel.clearBackgroundRemoval() },
                        onSplitTimeline = { frac -> viewModel.splitTimelineAtPlayhead(frac) },
                        onSelectSegment = { id -> viewModel.selectTimelineSegment(id) },
                        onDuplicateSegment = { viewModel.duplicateSelectedTimelineSegment() },
                        onCaptureFreeze = { frac, dur -> viewModel.freezeCurrentFrameAtPlayhead(frac, dur) },
                        onUpdateFreezeDuration = { segId, dur -> viewModel.updateFreezeDuration(segId, dur) },
                        onReplaceClip = onAddClipClick,
                        onDeleteSelectedSegment = { viewModel.deleteSelectedTimelineSegment() },
                        onDeleteActiveTarget = { viewModel.deleteActiveTarget() },
                        onResetAllEdits = { viewModel.resetAllEdits() },
                        onSelectClipAnimation = { anim -> viewModel.setClipAnimation(anim) },
                        onCommitText = { txt, col -> viewModel.commitTextOverlayContent(txt, col) },
                        onAddNewTextBox = { viewModel.addInteractiveTextBox("Enter text") },
                        onUpdateSelectedTextStyle = { col, fontId, art, style, anim, scale, rot, rec ->
                            viewModel.updateSelectedTextStyle(col, fontId, art, style, anim, scale, rot, rec)
                        },
                        onToggleFavoriteFont = { fontId -> viewModel.toggleFavoriteFont(fontId) },
                        onAddSticker = { label, isBadge -> viewModel.addStickerOverlay(label, isBadge) },
                        onClearMosaic = { viewModel.clearMosaicPoints() },
                        onClearRemoval = { viewModel.clearRemovalPoints() },
                        onApplyTemplate = { viewModel.applyTemplateInEditor(it) },
                        onSelectBuiltInAudio = { preset -> viewModel.selectBuiltInAudioPreset(preset) },
                        onImportAudioUri = { uri -> viewModel.importAudioFileUri(uri) },
                        onExtractAudioFromDeviceVideo = { uri -> viewModel.extractAudioFromVideoUri(uri) },
                        onSeparateCurrentVideoAudio = { viewModel.separateAudioFromCurrentVideo() },
                        onAttachRecordedAudio = { file, dur -> viewModel.attachRecordedVoiceover(file, dur) },
                        onApplyAudioDsp = { reverb, vocalSep, noiseRed ->
                            viewModel.applyAudioDspEffects(reverb, vocalSep, noiseRed)
                        },
                        onRunOneTapAudioFeature = { vocal, noise, strength, fade ->
                            viewModel.runOneTapAudioFeature(vocal, noise, strength, fade)
                        },
                        onScanSilence = { thresh, minMs -> viewModel.scanAudioForSilence(thresh, minMs) },
                        onTrimSilences = { thresh, minMs -> viewModel.trimDetectedSilences(thresh, minMs) },
                        onSaveProcessedAudio = { viewModel.saveProcessedAudioToDevice() },
                        onRemoveAudioTrack = { viewModel.removeAudioTrack() },
                        onCancelToolChanges = { snap -> viewModel.restoreEditSnapshot(snap) },
                        onConfirmToolChanges = { snap -> viewModel.commitToolChanges(snap) },
                        onClosePanel = { viewModel.closeSelectedToolPanel() }
                    )
                }
            }

            // ==================== SCROLLABLE TOOL ICON ROW ====================
            val visibleTools = if (uiState.editorMainMode == EditorMainMode.EDIT) {
                StudioCatalog.editorEditTools
            } else {
                StudioCatalog.editorRetouchTools
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A090A))
                    .padding(top = 6.dp, bottom = 2.dp),
                contentPadding = PaddingValues(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(visibleTools, key = { it.name }) { tool ->
                    val isSelected = tool == uiState.selectedEditorTool ||
                        (tool == StudioToolId.MIRROR && uiState.editState.flipHorizontal) ||
                        (tool == StudioToolId.FLIP && uiState.editState.flipVertical)
                    Column(
                        modifier = Modifier
                            .width(66.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) SurfaceElevated else Color.Transparent)
                            .clickable { viewModel.selectEditorTool(tool) }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .testTag("editor_tool_${tool.name.lowercase()}"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = toolIconFor(tool),
                            contentDescription = tool.label,
                            tint = if (isSelected) SurfaceBright else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = tool.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = if (isSelected) TextPrimary else TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // ==================== RETOUCH | EDIT BOTTOM SWITCHER ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A090A))
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditorBottomModeTab(
                    label = stringResource(R.string.action_retouch),
                    selected = uiState.editorMainMode == EditorMainMode.RETOUCH,
                    testTag = "editor_mode_retouch",
                    onClick = { viewModel.switchEditorMainMode(EditorMainMode.RETOUCH) }
                )
                Spacer(modifier = Modifier.width(42.dp))
                EditorBottomModeTab(
                    label = stringResource(R.string.action_edit),
                    selected = uiState.editorMainMode == EditorMainMode.EDIT,
                    testTag = "editor_mode_edit",
                    onClick = { viewModel.switchEditorMainMode(EditorMainMode.EDIT) }
                )
            }
        }
    }

    if (uiState.showSpeedPopup) {
        val speedEntrySnapshot = remember(uiState.showSpeedPopup) { uiState.editState }
        SpeedCurveAndRegularPopup(
            editState = uiState.editState,
            totalDurationMs = totalDurationMs,
            isVideo = isVideo,
            onSelectTab = { tab -> viewModel.setSpeedModeTab(tab) },
            onSelectCurvePreset = { preset -> viewModel.setSpeedCurvePreset(preset) },
            onUpdateCurvePoint = { idx, v -> viewModel.updateCustomSpeedCurvePoint(idx, v) },
            onSpeedChange = { s -> viewModel.setPlaybackSpeed(s, recordUndo = false) },
            onReset = {
                viewModel.setSpeedCurvePreset(SpeedCurvePreset.ORIGINAL)
                viewModel.setPlaybackSpeed(1.0f, recordUndo = true)
            },
            onCancel = { viewModel.restoreEditSnapshot(speedEntrySnapshot) },
            onConfirm = { viewModel.commitToolChanges(speedEntrySnapshot) }
        )
    }
}

@Composable
private fun InteractivePhotoCompareCanvas(
    originalBitmap: Bitmap,
    enhancedBitmap: Bitmap,
    splitFraction: Float,
    onSplitChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    onSplitChange(change.position.x / size.width.toFloat())
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onSplitChange(offset.x / size.width.toFloat())
                }
            }
            .testTag("editor_compare_slider_canvas")
    ) {
        val fullWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val splitWidthDp = maxWidth * splitFraction

        val enhancedImgBmp = remember(enhancedBitmap) { enhancedBitmap.asImageBitmap() }
        val originalImgBmp = remember(originalBitmap) { originalBitmap.asImageBitmap() }

        // Base right layer: Enhanced photo
        Image(
            bitmap = enhancedImgBmp,
            contentDescription = "After enhancement",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // Left clipped layer: Original Before photo
        Box(
            modifier = Modifier
                .width(splitWidthDp)
                .fillMaxHeight()
                .clipToBounds()
        ) {
            Image(
                bitmap = originalImgBmp,
                contentDescription = "Before enhancement",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(this@BoxWithConstraints.maxWidth)
                    .fillMaxHeight()
            )
        }

        // Divider vertical line + circular handle
        Box(
            modifier = Modifier
                .offset { IntOffset((fullWidthPx * splitFraction).roundToInt() - 1, 0) }
                .width(2.5.dp)
                .fillMaxHeight()
                .background(Color.White)
        )

        Surface(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset { IntOffset((fullWidthPx * splitFraction).roundToInt() - 36, 0) }
                .size(34.dp),
            shape = CircleShape,
            color = AccentPurple,
            border = BorderStroke(2.dp, Color.White)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Compare,
                    contentDescription = "Drag comparison handle",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
            shape = RoundedCornerShape(999.dp),
            color = Color(0xAA121112)
        ) {
            Text("Before", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp))
        }
        Surface(
            modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
            shape = RoundedCornerShape(999.dp),
            color = AccentPurple
        ) {
            Text("After", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp))
        }
    }
}

@Composable
private fun FreeformCropOverlay(
    crop: NormCropRect,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    onUpdateCrop: (NormCropRect) -> Unit
) {
    val leftPx = (crop.left * canvasWidthPx).roundToInt()
    val topPx = (crop.top * canvasHeightPx).roundToInt()
    val widthPx = ((crop.right - crop.left) * canvasWidthPx).roundToInt().coerceAtLeast(60)
    val heightPx = ((crop.bottom - crop.top) * canvasHeightPx).roundToInt().coerceAtLeast(60)
    val density = LocalDensity.current

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset { IntOffset(leftPx, topPx) }
                .size(
                    width = with(density) { widthPx.toDp() },
                    height = with(density) { heightPx.toDp() }
                )
                .border(2.dp, Color.White, RoundedCornerShape(4.dp))
        ) {
            // Top-Left corner handle
            CropCornerHandle(
                modifier = Modifier.align(Alignment.TopStart),
                onDrag = { dx, dy ->
                    onUpdateCrop(
                        crop.copy(
                            left = crop.left + dx / canvasWidthPx.coerceAtLeast(1f),
                            top = crop.top + dy / canvasHeightPx.coerceAtLeast(1f)
                        )
                    )
                }
            )
            // Bottom-Right corner handle
            CropCornerHandle(
                modifier = Modifier.align(Alignment.BottomEnd),
                onDrag = { dx, dy ->
                    onUpdateCrop(
                        crop.copy(
                            right = crop.right + dx / canvasWidthPx.coerceAtLeast(1f),
                            bottom = crop.bottom + dy / canvasHeightPx.coerceAtLeast(1f)
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun CropCornerHandle(
    modifier: Modifier = Modifier,
    onDrag: (Float, Float) -> Unit
) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(AccentPurple)
            .border(2.dp, Color.White, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
    )
}

@Composable
private fun InteractiveTextOverlayBox(
    item: TextOverlayItem,
    isSelected: Boolean,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    animPulse: Float,
    onSelect: () -> Unit,
    onTransform: (normX: Float, normY: Float, scale: Float, rotationDeg: Float) -> Unit,
    onRemove: () -> Unit
) {
    val px = ((item.normX - 0.5f) * canvasWidthPx).roundToInt()
    val py = ((item.normY - 0.5f) * canvasHeightPx).roundToInt()

    val fontSpec = StudioCatalog.studioFonts.firstOrNull { it.id == item.fontId }
    val composeFontFamily = remember(fontSpec?.fontRes) {
        if (fontSpec != null) FontFamily(Font(fontSpec.fontRes)) else FontFamily.Default
    }

    // Live animation effect
    val animAlpha = when (item.animationPreset) {
        TextAnimationPreset.FADE_IN -> (0.25f + 0.75f * animPulse)
        TextAnimationPreset.NEON_PULSE -> if (animPulse < 0.5f) 0.55f + animPulse * 0.9f else 1.45f - animPulse * 0.9f
        else -> 1f
    }
    val animScale = when (item.animationPreset) {
        TextAnimationPreset.POP_ZOOM -> 0.85f + 0.25f * animPulse
        else -> 1f
    }
    val animTransY = when (item.animationPreset) {
        TextAnimationPreset.SLIDE_UP -> (1f - animPulse) * 24f
        else -> 0f
    }
    val shownText = if (item.animationPreset == TextAnimationPreset.TYPEWRITER && item.text.length > 1) {
        val count = ((animPulse * item.text.length).roundToInt()).coerceIn(1, item.text.length)
        item.text.take(count)
    } else {
        item.text
    }

    val effectiveColor = if (item.artPreset != TextArtPreset.NONE) {
        Color(item.artPreset.accentArgb)
    } else {
        Color(item.colorArgb)
    }

    val boxModifier = when (item.stylePreset) {
        TextBoxStylePreset.SOLID_PILL -> Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xE6121218))
            .padding(horizontal = 14.dp, vertical = 6.dp)
        TextBoxStylePreset.FROST_CARD -> Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x99FFFFFF))
            .padding(horizontal = 12.dp, vertical = 6.dp)
        TextBoxStylePreset.OUTLINE_BADGE -> Modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.5.dp, effectiveColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
        else -> Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(px, py) }
            .graphicsLayer {
                scaleX = item.scale * animScale
                scaleY = item.scale * animScale
                rotationZ = item.rotationDeg
                alpha = animAlpha
                translationY = animTransY
            }
            .pointerInput(item.id, item.normX, item.normY, item.scale, item.rotationDeg) {
                detectTransformGestures { _, pan, zoom, rotation ->
                    onTransform(
                        item.normX + pan.x / canvasWidthPx.coerceAtLeast(1f),
                        item.normY + pan.y / canvasHeightPx.coerceAtLeast(1f),
                        item.scale * zoom,
                        item.rotationDeg + rotation
                    )
                }
            }
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(6.dp)
            .testTag("canvas_text_overlay_${item.id}")
    ) {
        Box(modifier = boxModifier) {
            Text(
                text = shownText,
                color = effectiveColor,
                fontFamily = composeFontFamily,
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    shadow = if (item.stylePreset == TextBoxStylePreset.SHADOW_GLOW || item.artPreset != TextArtPreset.NONE) {
                        Shadow(
                            color = if (item.artPreset != TextArtPreset.NONE) effectiveColor else Color(0xCC000000),
                            offset = Offset(0f, 3f),
                            blurRadius = 10f
                        )
                    } else null
                )
            )
        }

        if (isSelected) {
            // Top-Right Delete (X) button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE63950))
                    .clickable(onClick = onRemove)
                    .testTag("canvas_text_delete_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Delete text overlay",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }

            // Bottom-Right Corner Resize & Rotate Drag Handle
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(AccentPurple)
                    .border(1.5.dp, Color.White, CircleShape)
                    .pointerInput(item.id, item.scale, item.rotationDeg) {
                        detectDragGestures { change, drag ->
                            change.consume()
                            val nextScale = (item.scale + drag.x * 0.005f).coerceIn(0.45f, 3.2f)
                            val nextRot = item.rotationDeg + drag.y * 0.45f
                            onTransform(item.normX, item.normY, nextScale, nextRot)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.OpenWith,
                    contentDescription = "Resize or rotate text",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun EditorTimelineStrip(
    isVideo: Boolean,
    frames: List<Bitmap>,
    previewBitmap: Bitmap?,
    selectedCoverIndex: Int,
    segments: List<TimelineSegment>,
    selectedSegmentId: String,
    playheadFraction: Float,
    onSelectSegment: (String) -> Unit,
    onOpenTransitionJunction: (Int) -> Unit,
    onAddClipClick: () -> Unit,
    onCoverClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // + Add Clip tile
        Surface(
            onClick = onAddClipClick,
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF2B292B),
            modifier = Modifier
                .size(44.dp)
                .testTag("editor_add_clip_btn")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Change or add media clip",
                    tint = TextPrimary
                )
            }
        }

        // Cover tile
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceElevated)
                .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                .clickable(onClick = onCoverClick)
                .testTag("editor_cover_btn")
        ) {
            val coverBmp = frames.getOrNull(selectedCoverIndex) ?: previewBitmap
            if (coverBmp != null) {
                val coverImgBmp = remember(coverBmp) { coverBmp.asImageBitmap() }
                Image(
                    bitmap = coverImgBmp,
                    contentDescription = "Cover frame",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000))
            )
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = stringResource(R.string.label_cover),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = TextPrimary
                )
            }
        }

        // Smooth Horizontally Scrollable Segmented Filmstrip + Transition Junction Badges
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceElevated)
        ) {
            val scrollState = rememberScrollState()
            val stripWidthDp = maxOf(maxWidth, 160.dp * segments.size.coerceAtLeast(1) + 120.dp)
            val stripWidthPx = with(LocalDensity.current) { stripWidthDp.toPx() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState)
                    .testTag("editor_timeline_scroll")
            ) {
                Box(
                    modifier = Modifier
                        .width(stripWidthDp)
                        .fillMaxHeight()
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(if (segments.size > 1) 4.dp else 0.dp)
                    ) {
                        segments.forEachIndexed { idx, seg ->
                            val segWeight = (seg.endFraction - seg.startFraction).coerceAtLeast(0.08f)
                            val isSel = seg.id == selectedSegmentId
                            Box(
                                modifier = Modifier
                                    .weight(segWeight)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (isSel) 2.dp else 1.dp,
                                        color = if (isSel) AccentPurple else SurfaceBright,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelectSegment(seg.id) }
                            ) {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    val frameCount = if (segments.size > 2) 3 else 6
                                    val frozenBmp = remember(seg.frozenFramePath) {
                                        if (seg.isFrozen && !seg.frozenFramePath.isNullOrBlank()) {
                                            runCatching { BitmapFactory.decodeFile(seg.frozenFramePath) }.getOrNull()
                                        } else null
                                    }
                                    for (i in 0 until frameCount) {
                                        val bmp = frozenBmp ?: if (isVideo && frames.isNotEmpty()) {
                                            frames[(i * frames.size) / frameCount]
                                        } else {
                                            previewBitmap
                                        }
                                        if (bmp != null) {
                                            val tileImgBmp = remember(bmp) { bmp.asImageBitmap() }
                                            Image(
                                                bitmap = tileImgBmp,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.weight(1f).fillMaxHeight()
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .background(if (i % 2 == 0) SurfaceCard else SurfaceElevated)
                                            )
                                        }
                                    }
                                }
                                if (seg.isFrozen) {
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = Color(0xCC121112),
                                        modifier = Modifier.align(Alignment.TopStart).padding(3.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.AcUnit,
                                                contentDescription = "Frozen segment",
                                                tint = AccentTeal,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Text(
                                                text = "${seg.freezeDurationMs / 1000}s",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                                color = AccentTeal
                                            )
                                        }
                                    }
                                }
                            }

                            // Interactive Transition Junction Button between adjacent clips
                            if (idx < segments.lastIndex) {
                                val hasTransition = seg.transitionAfterId != "none"
                                Surface(
                                    onClick = { onOpenTransitionJunction(idx) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (hasTransition) AccentPurple else SurfaceCard,
                                    border = BorderStroke(
                                        width = 1.5.dp,
                                        color = if (hasTransition) Color.White else AccentTeal
                                    ),
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("timeline_transition_btn_$idx")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.MovieFilter,
                                            contentDescription = "Set transition between Clip ${idx + 1} and Clip ${idx + 2}",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // White Playhead bar
                    Box(
                        modifier = Modifier
                            .offset { IntOffset((stripWidthPx * playheadFraction).roundToInt() - 2, 0) }
                            .width(3.dp)
                            .fillMaxHeight()
                            .background(SurfaceBright)
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorBottomModeTab(
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) TextPrimary else TextMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(2.5.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(if (selected) SurfaceBright else Color.Transparent)
        )
    }
}

@Composable
private fun InteractiveStickerOverlayBox(
    sticker: StickerOverlayItem,
    isSelected: Boolean,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    onSelect: () -> Unit,
    onTransform: (normX: Float, normY: Float, scale: Float, rotationDeg: Float) -> Unit,
    onRemove: () -> Unit
) {
    val px = ((sticker.normX - 0.5f) * canvasWidthPx).roundToInt()
    val py = ((sticker.normY - 0.5f) * canvasHeightPx).roundToInt()
    Box(
        modifier = Modifier
            .offset { IntOffset(px, py) }
            .graphicsLayer {
                scaleX = sticker.scale
                scaleY = sticker.scale
                rotationZ = sticker.rotationDeg
            }
            .pointerInput(sticker.id, sticker.normX, sticker.normY, sticker.scale, sticker.rotationDeg) {
                detectTransformGestures { _, pan, zoom, rotation ->
                    onTransform(
                        sticker.normX + pan.x / canvasWidthPx.coerceAtLeast(1f),
                        sticker.normY + pan.y / canvasHeightPx.coerceAtLeast(1f),
                        (sticker.scale * zoom).coerceIn(0.45f, 3.5f),
                        sticker.rotationDeg + rotation
                    )
                }
            }
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(6.dp)
            .testTag("canvas_sticker_overlay_${sticker.id}")
    ) {
        if (sticker.isBadge) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color(0xCC121112),
                border = BorderStroke(1.dp, Color(0x88FFFFFF))
            ) {
                Text(
                    text = sticker.label,
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        } else {
            Text(
                text = sticker.label,
                fontSize = 36.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color(0xAA000000),
                        offset = Offset(0f, 3f),
                        blurRadius = 8f
                    )
                )
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE63950))
                    .clickable(onClick = onRemove)
                    .testTag("canvas_sticker_delete_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove sticker",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(AccentPurple)
                    .border(1.5.dp, Color.White, CircleShape)
                    .pointerInput(sticker.id, sticker.scale, sticker.rotationDeg) {
                        detectDragGestures { change, drag ->
                            change.consume()
                            val nextScale = (sticker.scale + drag.x * 0.006f).coerceIn(0.45f, 3.5f)
                            val nextRot = sticker.rotationDeg + drag.y * 0.45f
                            onTransform(sticker.normX, sticker.normY, nextScale, nextRot)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.OpenWith,
                    contentDescription = "Resize or rotate sticker",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

private fun evaluateCurveSpeed(points: List<Float>, fraction: Float): Float {
    if (points.isEmpty()) return 1f
    if (points.size == 1) return points[0].coerceIn(0.25f, 4.0f)
    val clamped = fraction.coerceIn(0f, 1f)
    val scaled = clamped * (points.size - 1)
    val idx = scaled.toInt().coerceIn(0, points.lastIndex - 1)
    val t = (scaled - idx).coerceIn(0f, 1f)
    return (points[idx] + (points[idx + 1] - points[idx]) * t).coerceIn(0.25f, 4.0f)
}

private fun formatSeconds(sec: Long): String {
    val s = sec.coerceAtLeast(0L)
    val m = s / 60
    val rem = s % 60
    return "%02d:%02d".format(m, rem)
}

@Composable
private fun MaskCanvasGuide(
    shape: MaskShapeType,
    centerX: Float,
    centerY: Float,
    scale: Float,
    rotation: Float,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    onTransformMask: (normX: Float, normY: Float, scale: Float, rotationDeg: Float) -> Unit
) {
    val dashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(shape, centerX, centerY, scale, rotation) {
                detectTransformGestures { _, pan, zoom, rot ->
                    onTransformMask(
                        centerX + pan.x / canvasWidthPx.coerceAtLeast(1f),
                        centerY + pan.y / canvasHeightPx.coerceAtLeast(1f),
                        scale * zoom,
                        rotation + rot
                    )
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotation }
        ) {
            val cx = centerX * size.width
            val cy = centerY * size.height
            val halfSpan = minOf(size.width, size.height) * 0.45f * scale
            val stroke = Stroke(width = 2.dp.toPx(), pathEffect = dashEffect)
            when (shape) {
                MaskShapeType.CIRCLE, MaskShapeType.HEART, MaskShapeType.STAR -> {
                    drawCircle(color = AccentTeal, radius = halfSpan, center = Offset(cx, cy), style = stroke)
                }
                MaskShapeType.RECTANGLE -> {
                    drawRect(
                        color = AccentTeal,
                        topLeft = Offset(cx - halfSpan, cy - halfSpan * 0.72f),
                        size = androidx.compose.ui.geometry.Size(halfSpan * 2f, halfSpan * 1.44f),
                        style = stroke
                    )
                }
                MaskShapeType.LINEAR -> {
                    drawLine(
                        color = AccentTeal,
                        start = Offset(0f, cy),
                        end = Offset(size.width, cy),
                        strokeWidth = 2.5.dp.toPx()
                    )
                }
                MaskShapeType.MIRROR -> {
                    drawRect(
                        color = AccentTeal,
                        topLeft = Offset(0f, cy - halfSpan * 0.55f),
                        size = androidx.compose.ui.geometry.Size(size.width, halfSpan * 1.1f),
                        style = stroke
                    )
                }
                MaskShapeType.NONE -> Unit
            }
            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(cx, cy))
        }
    }
}

@Composable
private fun CameraTrackingReticle(
    anchorX: Float,
    anchorY: Float,
    canvasWidthPx: Float,
    canvasHeightPx: Float
) {
    val px = ((anchorX - 0.5f) * canvasWidthPx).roundToInt()
    val py = ((anchorY - 0.5f) * canvasHeightPx).roundToInt()
    Box(
        modifier = Modifier
            .offset { IntOffset(px, py) }
            .size(56.dp)
            .border(2.dp, AccentTeal, RoundedCornerShape(14.dp))
            .background(AccentTeal.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(AccentPurple)
                .border(1.5.dp, Color.White, CircleShape)
        )
    }
}

@Composable
private fun TransparencyCheckerboard(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(Color(0xFF1A1A1E))) {
        val cellPx = 16.dp.toPx()
        val cols = (size.width / cellPx).toInt() + 1
        val rows = (size.height / cellPx).toInt() + 1
        val lightCell = Color(0xFF282830)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if ((r + c) % 2 == 0) {
                    drawRect(
                        color = lightCell,
                        topLeft = Offset(c * cellPx, r * cellPx),
                        size = androidx.compose.ui.geometry.Size(cellPx, cellPx)
                    )
                }
            }
        }
    }
}
