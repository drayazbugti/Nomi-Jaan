package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.transformer.Transformer
import com.example.R
import com.example.data.StudioRepository
import com.example.media.AudioDSPProcessor
import com.example.media.BackgroundRemoverEngine
import com.example.media.ImageEnhancerEngine
import com.example.media.SilenceAnalysisResult
import com.example.media.VideoEnhancerEngine
import com.example.model.AppThemeMode
import com.example.model.AspectRatioOption
import com.example.model.AudioSourceType
import com.example.model.BackgroundReplaceMode
import com.example.model.CanvasFitMode
import com.example.model.ClipTransitionAnimation
import com.example.model.EditorMainMode
import com.example.model.EffectMode
import com.example.model.EnhanceMode
import com.example.model.EnhanceSegment
import com.example.model.ExportResolution
import com.example.model.ExportStatus
import com.example.model.FilterPreset
import com.example.model.FrameStyle
import com.example.model.HomeTab
import com.example.model.MaskShapeType
import com.example.model.MediaSource
import com.example.model.NormCropRect
import com.example.model.NormPoint
import com.example.model.OverlayMode
import com.example.model.SavedCreation
import com.example.model.SpeedCurvePreset
import com.example.model.SpeedModeTab
import com.example.model.StabilizeLevel
import com.example.model.StickerOverlayItem
import com.example.model.StudioAudioTrackState
import com.example.model.StudioCatalog
import com.example.model.StudioEditState
import com.example.model.StudioFilterSpec
import com.example.model.StudioMusicPreset
import com.example.model.StudioPreferences
import com.example.model.StudioTemplate
import com.example.model.StudioToolId
import com.example.model.TextAnimationPreset
import com.example.model.TextArtPreset
import com.example.model.TextBoxStylePreset
import com.example.model.TextOverlayItem
import com.example.model.TimelineSegment
import com.example.model.TrackingTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Immutable
data class StudioUiState(
    val currentHomeTab: HomeTab = HomeTab.HOME,
    val searchQuery: String = "",
    val qualitySegment: EnhanceSegment = EnhanceSegment.GENERAL,
    val selectedQualityMode: EnhanceMode = EnhanceMode.ULTRA_HD,
    val demoSplitPosition: Float = 0.52f,
    val activeMediaSource: MediaSource? = null,
    val originalBitmap: Bitmap? = null,
    val processedBitmap: Bitmap? = null,
    val personMaskBitmap: Bitmap? = null,
    val isProcessingPreview: Boolean = false,
    val videoDurationMs: Long = 19_000L,
    val videoFrames: List<Bitmap> = emptyList(),
    val editState: StudioEditState = StudioEditState(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val editorMainMode: EditorMainMode = EditorMainMode.EDIT,
    val selectedEditorTool: StudioToolId? = StudioToolId.ENHANCE,
    val selectedTextOverlayId: String? = null,
    val selectedStickerId: String? = null,
    val isHoldingCompare: Boolean = false,
    val isEditorCompareSliderActive: Boolean = false,
    val editorCompareSplitFraction: Float = 0.5f,
    val isMagnifierEnabled: Boolean = false,
    val selectedCoverFrameIndex: Int = 0,
    val showSpeedPopup: Boolean = false,
    val selectedTransitionJunctionIndex: Int? = null,
    val silenceAnalysis: SilenceAnalysisResult? = null,
    val silenceThresholdDb: Float = -35f,
    val silenceMinMs: Long = 350L,
    val exportStatus: ExportStatus = ExportStatus.Idle,
    val pendingToolAfterPick: StudioToolId? = null,
    val pendingModeAfterPick: EditorMainMode = EditorMainMode.EDIT,
    val pendingPresetEditState: StudioEditState? = null
)

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StudioRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    val savedCreations: StateFlow<List<SavedCreation>> = repository.savedCreationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val preferences: StateFlow<StudioPreferences> = repository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StudioPreferences())

    private val undoStack = ArrayDeque<StudioEditState>()
    private val redoStack = ArrayDeque<StudioEditState>()
    private var previewJob: Job? = null
    private var activeTransformer: Transformer? = null

    init {
        viewModelScope.launch {
            repository.resetToWhiteThemeIfConfigured()
        }
    }

    fun selectHomeTab(tab: HomeTab) {
        _uiState.update { it.copy(currentHomeTab = tab) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectQualitySegment(segment: EnhanceSegment) {
        val defaultMode = if (segment == EnhanceSegment.GENERAL) {
            EnhanceMode.ULTRA_HD
        } else {
            EnhanceMode.NIGHT_DENOISE
        }
        _uiState.update {
            it.copy(qualitySegment = segment, selectedQualityMode = defaultMode)
        }
    }

    fun selectQualityMode(mode: EnhanceMode) {
        _uiState.update { it.copy(selectedQualityMode = mode) }
    }

    fun updateDemoSplitPosition(fraction: Float) {
        _uiState.update { it.copy(demoSplitPosition = fraction.coerceIn(0.08f, 0.92f)) }
    }

    fun toggleEditorCompareSlider() {
        _uiState.update {
            it.copy(isEditorCompareSliderActive = !it.isEditorCompareSliderActive)
        }
    }

    fun updateEditorCompareSplit(fraction: Float) {
        _uiState.update {
            it.copy(editorCompareSplitFraction = fraction.coerceIn(0.05f, 0.95f))
        }
    }

    fun preparePickerContext(
        mainMode: EditorMainMode = EditorMainMode.EDIT,
        initialTool: StudioToolId? = StudioToolId.ENHANCE,
        presetState: StudioEditState? = null
    ) {
        _uiState.update {
            it.copy(
                pendingModeAfterPick = mainMode,
                pendingToolAfterPick = initialTool,
                pendingPresetEditState = presetState
            )
        }
    }

    fun openMediaUri(uri: Uri, isVideo: Boolean) {
        val source = MediaSource.ContentUri(uri, isVideo)
        openMediaSource(source)
    }

    fun openSampleDrawable(@DrawableRes resId: Int, title: String) {
        openMediaSource(MediaSource.SampleDrawable(resId, title))
    }

    fun applyTemplateAndOpenSample(template: StudioTemplate) {
        preparePickerContext(
            mainMode = EditorMainMode.EDIT,
            initialTool = StudioToolId.ENHANCE,
            presetState = template.editState
        )
        openSampleDrawable(template.previewRes, template.title)
    }

    fun applyTemplateInEditor(template: StudioTemplate) {
        pushUndoSnapshot()
        _uiState.update { state ->
            state.copy(
                editState = template.editState.copy(
                    rotationDegrees = state.editState.rotationDegrees,
                    flipHorizontal = state.editState.flipHorizontal,
                    exportResolution = state.editState.exportResolution
                ),
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        schedulePreviewReprocess()
    }

    private fun openMediaSource(source: MediaSource) {
        val current = _uiState.value
        val defaultRes = preferences.value.defaultResolution
        val initialEditState = (current.pendingPresetEditState ?: StudioEditState()).copy(
            exportResolution = defaultRes
        )
        val mainMode = current.pendingModeAfterPick
        val initialTool = current.pendingToolAfterPick ?: if (mainMode == EditorMainMode.RETOUCH) {
            StudioToolId.FACE
        } else {
            StudioToolId.ENHANCE
        }

        undoStack.clear()
        redoStack.clear()
        val initialTextId = if (initialTool == StudioToolId.TEXT && initialEditState.textOverlays.isEmpty()) {
            UUID.randomUUID().toString()
        } else null
        val seededEditState = if (initialTextId != null) {
            initialEditState.copy(
                textOverlays = listOf(TextOverlayItem(id = initialTextId, text = "Enter text"))
            )
        } else {
            initialEditState
        }

        _uiState.update {
            it.copy(
                activeMediaSource = source,
                originalBitmap = null,
                processedBitmap = null,
                personMaskBitmap = null,
                videoFrames = emptyList(),
                videoDurationMs = 19_000L,
                editState = seededEditState,
                canUndo = false,
                canRedo = false,
                editorMainMode = mainMode,
                selectedEditorTool = initialTool,
                selectedTextOverlayId = initialTextId,
                isProcessingPreview = true,
                isEditorCompareSliderActive = false,
                editorCompareSplitFraction = 0.5f,
                isMagnifierEnabled = false,
                selectedCoverFrameIndex = 0,
                exportStatus = ExportStatus.Idle,
                pendingPresetEditState = null
            )
        }

        val ctx = getApplication<Application>().applicationContext
        viewModelScope.launch {
            if (source is MediaSource.ContentUri && source.isVideo) {
                val (durationMs, frames) = VideoEnhancerEngine.extractVideoInfoAndThumbnails(ctx, source.uri)
                _uiState.update {
                    it.copy(
                        videoDurationMs = durationMs,
                        videoFrames = frames,
                        isProcessingPreview = false
                    )
                }
            } else {
                val loaded = ImageEnhancerEngine.loadSourceBitmap(ctx, source, maxDimension = 1280)
                if (loaded != null) {
                    val processed = ImageEnhancerEngine.processBitmap(loaded, seededEditState, isExport = false, context = ctx)
                    _uiState.update {
                        it.copy(
                            originalBitmap = loaded,
                            processedBitmap = processed,
                            isProcessingPreview = false
                        )
                    }
                    if (initialTool == StudioToolId.BG_REMOVER || initialTool == StudioToolId.CUTOUT) {
                        applyBackgroundRemoval()
                    }
                } else {
                    _uiState.update { it.copy(isProcessingPreview = false) }
                }
            }
        }
    }

    fun switchEditorMainMode(mode: EditorMainMode) {
        val defaultTool = if (mode == EditorMainMode.RETOUCH) {
            StudioToolId.FACE
        } else {
            StudioToolId.ENHANCE
        }
        _uiState.update {
            it.copy(
                editorMainMode = mode,
                selectedEditorTool = defaultTool,
                isMagnifierEnabled = false,
                showSpeedPopup = false
            )
        }
    }

    fun selectEditorTool(tool: StudioToolId) {
        if (tool == StudioToolId.MAGNIFIER) {
            _uiState.update {
                val nextMag = !it.isMagnifierEnabled
                it.copy(
                    selectedEditorTool = if (nextMag) StudioToolId.MAGNIFIER else null,
                    isMagnifierEnabled = nextMag,
                    showSpeedPopup = false
                )
            }
            return
        }
        if (tool == StudioToolId.MIRROR) {
            toggleFlipHorizontal()
            return
        }
        if (tool == StudioToolId.FLIP) {
            toggleFlipVertical()
            return
        }
        if (tool == StudioToolId.SPEED) {
            _uiState.update {
                val nextShow = !it.showSpeedPopup || it.selectedEditorTool != StudioToolId.SPEED
                it.copy(
                    selectedEditorTool = if (nextShow) StudioToolId.SPEED else null,
                    showSpeedPopup = nextShow,
                    isMagnifierEnabled = false
                )
            }
            return
        }
        val currentTool = _uiState.value.selectedEditorTool
        val nextTool = if (currentTool == tool) null else tool
        _uiState.update {
            it.copy(
                selectedEditorTool = nextTool,
                isMagnifierEnabled = false,
                showSpeedPopup = false,
                selectedTransitionJunctionIndex = if (nextTool == StudioToolId.TRANSITIONS) it.selectedTransitionJunctionIndex else null
            )
        }
        // Automatically ensure an interactive "Enter text" overlay box appears when opening Text tool
        if (nextTool == StudioToolId.TEXT && _uiState.value.editState.textOverlays.isEmpty()) {
            addInteractiveTextBox("Enter text")
        }
    }

    fun closeSelectedToolPanel() {
        _uiState.update {
            it.copy(
                selectedEditorTool = null,
                showSpeedPopup = false,
                isMagnifierEnabled = false,
                selectedTransitionJunctionIndex = null
            )
        }
    }

    fun restoreEditSnapshot(snapshot: StudioEditState) {
        _uiState.update {
            it.copy(
                editState = snapshot,
                selectedEditorTool = null,
                showSpeedPopup = false,
                selectedTransitionJunctionIndex = null
            )
        }
        schedulePreviewReprocess()
    }

    fun commitToolChanges(entrySnapshot: StudioEditState? = null) {
        if (entrySnapshot != null && entrySnapshot != _uiState.value.editState) {
            if (undoStack.size >= 30) undoStack.removeFirst()
            undoStack.addLast(entrySnapshot)
            redoStack.clear()
        }
        _uiState.update {
            it.copy(
                selectedEditorTool = null,
                showSpeedPopup = false,
                selectedTransitionJunctionIndex = null,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun openTransitionForJunction(junctionIndex: Int) {
        _uiState.update {
            it.copy(
                selectedEditorTool = StudioToolId.TRANSITIONS,
                selectedTransitionJunctionIndex = junctionIndex,
                showSpeedPopup = false,
                isMagnifierEnabled = false
            )
        }
    }

    fun setJunctionTransition(junctionIndex: Int, transitionId: String, durationMs: Int? = null) {
        val segs = _uiState.value.editState.timelineSegments
        if (junctionIndex !in 0 until segs.lastIndex) {
            setTransitionEffect(transitionId)
            return
        }
        val spec = StudioCatalog.transitionEffects.firstOrNull { it.id == transitionId }
        val resolvedDur = (durationMs ?: spec?.durationMs ?: 600).coerceIn(200, 2000)
        updateEditState { state ->
            state.copy(
                transitionEffectId = transitionId,
                animateDurationMs = resolvedDur,
                timelineSegments = state.timelineSegments.mapIndexed { idx, seg ->
                    if (idx == junctionIndex) {
                        seg.copy(transitionAfterId = transitionId, transitionAfterDurationMs = resolvedDur)
                    } else seg
                }
            )
        }
    }

    fun applyTransitionToAllJunctions(transitionId: String, durationMs: Int) {
        val dur = durationMs.coerceIn(200, 2000)
        updateEditState { state ->
            state.copy(
                transitionEffectId = transitionId,
                animateDurationMs = dur,
                timelineSegments = state.timelineSegments.mapIndexed { idx, seg ->
                    if (idx < state.timelineSegments.lastIndex) {
                        seg.copy(transitionAfterId = transitionId, transitionAfterDurationMs = dur)
                    } else seg
                }
            )
        }
    }

    fun openSpeedPopup() {
        _uiState.update {
            it.copy(
                selectedEditorTool = StudioToolId.SPEED,
                showSpeedPopup = true,
                isMagnifierEnabled = false
            )
        }
    }

    fun dismissSpeedPopup() {
        _uiState.update {
            it.copy(
                showSpeedPopup = false,
                selectedEditorTool = if (it.selectedEditorTool == StudioToolId.SPEED) null else it.selectedEditorTool
            )
        }
    }

    fun setHoldingCompare(holding: Boolean) {
        _uiState.update { it.copy(isHoldingCompare = holding) }
    }

    fun selectCoverFrame(index: Int) {
        _uiState.update { it.copy(selectedCoverFrameIndex = index) }
    }

    // ==================== UNDO / REDO SYSTEM ====================

    private fun pushUndoSnapshot() {
        val current = _uiState.value.editState
        if (undoStack.lastOrNull() == current) return
        if (undoStack.size >= 30) undoStack.removeFirst()
        undoStack.addLast(current)
        redoStack.clear()
    }

    fun undoLastEdit() {
        if (undoStack.isEmpty()) return
        val current = _uiState.value.editState
        if (redoStack.size >= 30) redoStack.removeFirst()
        redoStack.addLast(current)
        val prev = undoStack.removeLast()
        _uiState.update { state ->
            val validSelectedText = prev.textOverlays.firstOrNull { it.id == state.selectedTextOverlayId }?.id
                ?: prev.textOverlays.lastOrNull()?.id
            state.copy(
                editState = prev,
                selectedTextOverlayId = validSelectedText,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        schedulePreviewReprocess()
    }

    fun redoLastEdit() {
        if (redoStack.isEmpty()) return
        val current = _uiState.value.editState
        if (undoStack.size >= 30) undoStack.removeFirst()
        undoStack.addLast(current)
        val next = redoStack.removeLast()
        _uiState.update { state ->
            val validSelectedText = next.textOverlays.firstOrNull { it.id == state.selectedTextOverlayId }?.id
                ?: next.textOverlays.lastOrNull()?.id
            state.copy(
                editState = next,
                selectedTextOverlayId = validSelectedText,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        schedulePreviewReprocess()
    }

    fun resetAllEdits() {
        pushUndoSnapshot()
        _uiState.update {
            it.copy(
                editState = StudioEditState(exportResolution = it.editState.exportResolution),
                selectedTextOverlayId = null,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        schedulePreviewReprocess()
    }

    fun updateEditState(recordUndo: Boolean = true, transform: (StudioEditState) -> StudioEditState) {
        if (recordUndo) pushUndoSnapshot()
        _uiState.update {
            it.copy(
                editState = transform(it.editState),
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        schedulePreviewReprocess()
    }

    fun setEnhanceMode(mode: EnhanceMode) {
        updateEditState {
            it.copy(
                enhanceMode = mode,
                enhanceStrength = if (mode == EnhanceMode.NONE) 0f else mode.defaultStrength
            )
        }
    }

    /**
     * On-device Background Remover: keeps person/subject intact and removes background.
     */
    fun applyBackgroundRemoval(bgMode: BackgroundReplaceMode = _uiState.value.editState.bgReplaceMode) {
        val original = _uiState.value.originalBitmap ?: return
        val ctx = getApplication<Application>().applicationContext
        viewModelScope.launch {
            val cachedMask = _uiState.value.personMaskBitmap
            if (cachedMask == null) {
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Processing(
                            progress = 0.25f,
                            title = "Removing Background",
                            message = "Detecting person & isolating subject..."
                        )
                    )
                }
            }
            val mask = cachedMask ?: BackgroundRemoverEngine.extractPersonMask(original)
            pushUndoSnapshot()
            val nextState = _uiState.value.editState.copy(
                removeBackground = true,
                bgReplaceMode = bgMode
            )
            if (cachedMask == null) {
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Processing(
                            progress = 0.78f,
                            title = "Removing Background",
                            message = "Refining cutout edges..."
                        )
                    )
                }
            }
            val result = ImageEnhancerEngine.processBitmap(
                source = original,
                editState = nextState,
                isExport = false,
                context = ctx,
                personMask = mask
            )
            _uiState.update {
                it.copy(
                    personMaskBitmap = mask,
                    editState = nextState,
                    processedBitmap = result,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = redoStack.isNotEmpty(),
                    exportStatus = ExportStatus.Idle
                )
            }
        }
    }

    fun clearBackgroundRemoval() {
        updateEditState { it.copy(removeBackground = false) }
    }

    /**
     * Batch AI Enhancement: applies the chosen EnhanceMode & settings to multiple selected
     * photos simultaneously with smooth 0%-100% progress bar, saves to Gallery & records Drafts.
     */
    fun runBatchEnhance(
        uris: List<Uri>,
        mode: EnhanceMode,
        resolution: ExportResolution = preferences.value.defaultResolution
    ) {
        if (uris.isEmpty()) return
        val ctx = getApplication<Application>().applicationContext
        val batchEditState = StudioEditState(
            enhanceMode = if (mode == EnhanceMode.NONE) EnhanceMode.ULTRA_HD else mode,
            enhanceStrength = mode.defaultStrength.coerceAtLeast(0.75f),
            exportResolution = resolution
        )

        viewModelScope.launch {
            var successCount = 0
            var lastUri: Uri? = null
            val total = uris.size

            uris.forEachIndexed { index, uri ->
                val baseFrac = index.toFloat() / total.toFloat()
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Processing(
                            progress = (baseFrac + 0.05f).coerceIn(0.04f, 0.96f),
                            title = "Batch AI Processing ($total photos)",
                            message = "Enhancing photo ${index + 1} of $total (${batchEditState.enhanceMode.title})..."
                        )
                    )
                }
                val source = MediaSource.ContentUri(uri, isVideo = false)
                val loaded = ImageEnhancerEngine.loadSourceBitmap(ctx, source, maxDimension = resolution.maxDim)
                if (loaded != null) {
                    val enhanced = ImageEnhancerEngine.processBitmap(loaded, batchEditState, isExport = true, context = ctx)
                    val saved = ImageEnhancerEngine.saveBitmapToGallery(ctx, enhanced, "BugtiBatch_${index + 1}")
                    saved.onSuccess { outUri ->
                        successCount++
                        lastUri = outUri
                        recordExportCreation(
                            title = "Batch #${index + 1} (${batchEditState.enhanceMode.title})",
                            uri = outUri,
                            isVideo = false,
                            editState = batchEditState
                        )
                    }
                }
                val doneFrac = (index + 1).toFloat() / total.toFloat()
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Processing(
                            progress = doneFrac.coerceIn(0.05f, 0.99f),
                            title = "Batch AI Processing ($total photos)",
                            message = "Saved ${index + 1} of $total to Pictures/Bugti Editz"
                        )
                    )
                }
            }

            if (successCount > 0 && lastUri != null) {
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Success(
                            outputUri = lastUri!!,
                            isVideo = false,
                            summary = "Batch enhanced & saved $successCount of $total photos to Pictures/Bugti Editz"
                        )
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Error(
                            title = "Batch Processing Failed",
                            message = "Could not load or save the selected photos."
                        )
                    )
                }
            }
        }
    }

    fun setFilterPreset(preset: FilterPreset) {
        updateEditState { it.copy(filterPreset = preset, filterSpecId = preset.name.lowercase()) }
    }

    fun setFilterSpec(spec: StudioFilterSpec) {
        updateEditState {
            it.copy(
                filterSpecId = spec.id,
                filterPreset = spec.mappedPreset
            )
        }
    }

    fun setEffectMode(mode: EffectMode) {
        updateEditState { it.copy(effectMode = mode) }
    }

    fun setFxEffect(fxId: String) {
        updateEditState { it.copy(fxEffectId = fxId) }
    }

    fun setTransitionEffect(transitionId: String) {
        val spec = StudioCatalog.transitionEffects.firstOrNull { it.id == transitionId }
        val junctionIdx = _uiState.value.selectedTransitionJunctionIndex
        if (junctionIdx != null) {
            setJunctionTransition(junctionIdx, transitionId, spec?.durationMs)
        } else {
            updateEditState {
                it.copy(
                    transitionEffectId = transitionId,
                    animateDurationMs = spec?.durationMs ?: it.animateDurationMs
                )
            }
        }
    }

    fun setOverlayMode(mode: OverlayMode) {
        updateEditState { it.copy(overlayMode = mode) }
    }

    fun setAspectRatio(option: AspectRatioOption) {
        updateEditState {
            it.copy(
                aspectRatio = option,
                freeformCrop = if (option != AspectRatioOption.FREEFORM) NormCropRect() else it.freeformCrop
            )
        }
    }

    fun updateFreeformCrop(crop: NormCropRect, recordUndo: Boolean = false) {
        updateEditState(recordUndo = recordUndo) {
            it.copy(
                aspectRatio = AspectRatioOption.FREEFORM,
                freeformCrop = NormCropRect(
                    left = crop.left.coerceIn(0f, 0.78f),
                    top = crop.top.coerceIn(0f, 0.78f),
                    right = crop.right.coerceIn((crop.left + 0.18f).coerceAtMost(1f), 1f),
                    bottom = crop.bottom.coerceIn((crop.top + 0.18f).coerceAtMost(1f), 1f)
                )
            )
        }
    }

    fun setFrameStyle(style: FrameStyle) {
        updateEditState { it.copy(frameStyle = style) }
    }

    fun setExportResolution(resolution: ExportResolution) {
        updateEditState(recordUndo = false) { it.copy(exportResolution = resolution) }
    }

    fun rotate90() {
        updateEditState { it.copy(rotationDegrees = (it.rotationDegrees + 90) % 360) }
    }

    fun rotateLeft90() {
        updateEditState { it.copy(rotationDegrees = (it.rotationDegrees + 270) % 360) }
    }

    fun rotate180() {
        updateEditState { it.copy(rotationDegrees = (it.rotationDegrees + 180) % 360) }
    }

    fun setStraightenDegrees(degrees: Float, recordUndo: Boolean = false) {
        updateEditState(recordUndo = recordUndo) {
            it.copy(straightenDegrees = degrees.coerceIn(-180f, 180f))
        }
    }

    fun resetRotationAndFlip() {
        updateEditState {
            it.copy(
                rotationDegrees = 0,
                straightenDegrees = 0f,
                flipHorizontal = false,
                flipVertical = false
            )
        }
    }

    fun toggleFlipHorizontal() {
        updateEditState { it.copy(flipHorizontal = !it.flipHorizontal) }
    }

    fun toggleFlipVertical() {
        updateEditState { it.copy(flipVertical = !it.flipVertical) }
    }

    fun setClipPosition(offsetX: Float, offsetY: Float, recordUndo: Boolean = false) {
        updateEditState(recordUndo = recordUndo) {
            it.copy(
                clipOffsetX = offsetX.coerceIn(-0.6f, 0.6f),
                clipOffsetY = offsetY.coerceIn(-0.6f, 0.6f)
            )
        }
    }

    fun setClipSize(scaleX: Float, scaleY: Float, recordUndo: Boolean = false) {
        updateEditState(recordUndo = recordUndo) {
            it.copy(
                clipScaleX = scaleX.coerceIn(0.35f, 2.5f),
                clipScaleY = scaleY.coerceIn(0.35f, 2.5f)
            )
        }
    }

    fun setCanvasFitMode(mode: CanvasFitMode) {
        updateEditState { it.copy(canvasFitMode = mode) }
    }

    fun setVideoFade(fadeInMs: Int? = null, fadeOutMs: Int? = null, recordUndo: Boolean = false) {
        updateEditState(recordUndo = recordUndo) { state ->
            state.copy(
                videoFadeInMs = (fadeInMs ?: state.videoFadeInMs).coerceIn(0, 3000),
                videoFadeOutMs = (fadeOutMs ?: state.videoFadeOutMs).coerceIn(0, 3000)
            )
        }
    }

    fun setSpeedModeTab(tab: SpeedModeTab) {
        updateEditState(recordUndo = false) { it.copy(speedModeTab = tab) }
    }

    fun setSpeedCurvePreset(preset: SpeedCurvePreset) {
        val avg = preset.points.average().toFloat().coerceIn(0.25f, 4.0f)
        updateEditState {
            it.copy(
                speedModeTab = SpeedModeTab.CURVE,
                speedCurvePreset = preset,
                customSpeedCurvePoints = preset.points,
                playbackSpeed = if (preset == SpeedCurvePreset.ORIGINAL) 1f else avg
            )
        }
    }

    fun updateCustomSpeedCurvePoint(index: Int, speedVal: Float) {
        updateEditState(recordUndo = false) { state ->
            val updated = state.customSpeedCurvePoints.toMutableList()
            if (index in updated.indices) {
                updated[index] = speedVal.coerceIn(0.25f, 4.0f)
            }
            state.copy(
                speedModeTab = SpeedModeTab.CURVE,
                speedCurvePreset = SpeedCurvePreset.CUSTOM,
                customSpeedCurvePoints = updated,
                playbackSpeed = updated.average().toFloat().coerceIn(0.25f, 4.0f)
            )
        }
    }

    // ==================== TIMELINE CUT / COPY / FREEZE / SPEED / DELETE / ANIMATION ====================

    fun splitTimelineAtPlayhead(playheadFraction: Float) {
        val currentSegs = _uiState.value.editState.timelineSegments
        val clamped = playheadFraction.coerceIn(0.05f, 0.95f)
        val target = currentSegs.firstOrNull {
            clamped > it.startFraction + 0.03f && clamped < it.endFraction - 0.03f
        } ?: currentSegs.firstOrNull { it.id == _uiState.value.editState.selectedSegmentId }?.let { sel ->
            val mid = (sel.startFraction + sel.endFraction) * 0.5f
            if (sel.endFraction - sel.startFraction > 0.06f) {
                return splitSegmentAtPoint(sel, mid)
            } else null
        } ?: return

        splitSegmentAtPoint(target, clamped)
    }

    private fun splitSegmentAtPoint(target: TimelineSegment, splitPoint: Float) {
        val currentSegs = _uiState.value.editState.timelineSegments
        val nextSegs = buildList {
            currentSegs.forEach { seg ->
                if (seg.id == target.id) {
                    add(seg.copy(endFraction = splitPoint))
                    add(
                        TimelineSegment(
                            id = "seg_${UUID.randomUUID().toString().take(6)}",
                            startFraction = splitPoint,
                            endFraction = seg.endFraction,
                            label = "Clip ${currentSegs.size + 1}"
                        )
                    )
                } else {
                    add(seg)
                }
            }
        }

        val newSelectedId = nextSegs.lastOrNull { it.startFraction == splitPoint }?.id
            ?: _uiState.value.editState.selectedSegmentId

        updateEditState {
            it.copy(
                timelineSegments = nextSegs,
                selectedSegmentId = newSelectedId
            )
        }
    }

    fun duplicateSelectedTimelineSegment() {
        val currentSegs = _uiState.value.editState.timelineSegments
        if (currentSegs.isEmpty()) return
        val selectedId = _uiState.value.editState.selectedSegmentId
        val targetIdx = currentSegs.indexOfFirst { it.id == selectedId }.takeIf { it >= 0 } ?: 0
        val sourceSeg = currentSegs[targetIdx]
        val cloneSeg = sourceSeg.copy(
            id = "copy_${UUID.randomUUID().toString().take(6)}",
            label = if (sourceSeg.isFrozen) "Freeze Copy" else "${sourceSeg.label} Copy"
        )
        val rawCombined = buildList {
            currentSegs.forEachIndexed { i, seg ->
                add(seg)
                if (i == targetIdx) add(cloneSeg)
            }
        }
        val normalized = normalizeTimelineSegments(rawCombined)
        updateEditState {
            it.copy(
                timelineSegments = normalized,
                selectedSegmentId = cloneSeg.id
            )
        }
    }

    fun freezeCurrentFrameAtPlayhead(playheadFraction: Float, durationMs: Long = 3_000L) {
        val state = _uiState.value
        val source = state.activeMediaSource
        val ctx = getApplication<Application>().applicationContext
        val durClamped = durationMs.coerceIn(1_000L, 10_000L)

        viewModelScope.launch {
            val capturedBmp: Bitmap? = withContext(Dispatchers.IO) {
                var bmp: Bitmap? = null
                if (source is MediaSource.ContentUri && source.isVideo) {
                    val retriever = MediaMetadataRetriever()
                    runCatching {
                        retriever.setDataSource(ctx, source.uri)
                        val timeUs = (state.videoDurationMs * playheadFraction.coerceIn(0f, 1f) * 1000L).toLong()
                        bmp = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                            ?: retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)
                    }
                    runCatching { retriever.release() }
                }
                if (bmp == null && state.videoFrames.isNotEmpty()) {
                    val idx = (playheadFraction * (state.videoFrames.size - 1)).toInt()
                        .coerceIn(0, state.videoFrames.lastIndex)
                    bmp = state.videoFrames[idx]
                }
                if (bmp == null) {
                    bmp = state.processedBitmap ?: state.originalBitmap
                }
                bmp
            }

            val freezePath = if (capturedBmp != null) {
                withContext(Dispatchers.IO) {
                    val dir = File(ctx.cacheDir, "frozen_frames").apply { mkdirs() }
                    val file = File(dir, "freeze_${System.currentTimeMillis()}.png")
                    FileOutputStream(file).use { out ->
                        capturedBmp.compress(Bitmap.CompressFormat.PNG, 95, out)
                    }
                    file.absolutePath
                }
            } else null

            val freezeId = "freeze_${UUID.randomUUID().toString().take(6)}"
            val freezeSeg = TimelineSegment(
                id = freezeId,
                startFraction = playheadFraction,
                endFraction = (playheadFraction + 0.14f).coerceAtMost(1f),
                label = "Freeze ${(durClamped / 1000L)}s",
                isFrozen = true,
                frozenFramePath = freezePath,
                freezeDurationMs = durClamped
            )

            val currentSegs = _uiState.value.editState.timelineSegments
            val selectedIdx = currentSegs.indexOfFirst { it.id == _uiState.value.editState.selectedSegmentId }
                .takeIf { it >= 0 } ?: 0
            val splitFrac = playheadFraction.coerceIn(0.05f, 0.95f)
            val targetSeg = currentSegs.firstOrNull {
                splitFrac > it.startFraction + 0.03f && splitFrac < it.endFraction - 0.03f
            }

            val rawSegs = if (targetSeg != null) {
                buildList {
                    currentSegs.forEach { seg ->
                        if (seg.id == targetSeg.id) {
                            add(seg.copy(endFraction = splitFrac))
                            add(freezeSeg)
                            add(
                                TimelineSegment(
                                    id = "seg_${UUID.randomUUID().toString().take(6)}",
                                    startFraction = splitFrac,
                                    endFraction = seg.endFraction,
                                    label = "Clip ${currentSegs.size + 1}"
                                )
                            )
                        } else {
                            add(seg)
                        }
                    }
                }
            } else {
                buildList {
                    currentSegs.forEachIndexed { idx, seg ->
                        add(seg)
                        if (idx == selectedIdx) add(freezeSeg)
                    }
                }
            }

            val normalized = normalizeTimelineSegments(rawSegs)
            updateEditState {
                it.copy(
                    timelineSegments = normalized,
                    selectedSegmentId = freezeId
                )
            }
        }
    }

    fun updateFreezeDuration(segmentId: String, durationMs: Long) {
        val durClamped = durationMs.coerceIn(1_000L, 10_000L)
        updateEditState(recordUndo = false) { state ->
            state.copy(
                timelineSegments = state.timelineSegments.map { seg ->
                    if (seg.id == segmentId && seg.isFrozen) {
                        seg.copy(freezeDurationMs = durClamped, label = "Freeze ${(durClamped / 1000L)}s")
                    } else seg
                }
            )
        }
    }

    private fun normalizeTimelineSegments(rawSegs: List<TimelineSegment>): List<TimelineSegment> {
        if (rawSegs.isEmpty()) return listOf(TimelineSegment("seg_0", 0f, 1f, "Main"))
        val weights = rawSegs.map { seg ->
            if (seg.isFrozen) 0.16f else (seg.endFraction - seg.startFraction).coerceAtLeast(0.08f)
        }
        val totalW = weights.sum().coerceAtLeast(0.1f)
        var cursor = 0f
        return rawSegs.mapIndexed { idx, seg ->
            val w = weights[idx] / totalW
            val nextCursor = if (idx == rawSegs.lastIndex) 1f else (cursor + w).coerceAtMost(1f)
            val updated = seg.copy(startFraction = cursor, endFraction = nextCursor)
            cursor = nextCursor
            updated
        }
    }

    fun selectTimelineSegment(id: String) {
        _uiState.update { state ->
            state.copy(editState = state.editState.copy(selectedSegmentId = id))
        }
    }

    fun deleteSelectedTimelineSegment() {
        val segs = _uiState.value.editState.timelineSegments
        if (segs.size <= 1) return
        val selectedId = _uiState.value.editState.selectedSegmentId
        val remaining = segs.filterNot { it.id == selectedId }
        if (remaining.isEmpty()) return
        val normalized = normalizeTimelineSegments(remaining)
        updateEditState {
            it.copy(
                timelineSegments = normalized,
                selectedSegmentId = normalized.first().id,
                trimStartFraction = normalized.first().startFraction,
                trimEndFraction = normalized.last().endFraction
            )
        }
    }

    fun deleteActiveTarget() {
        val state = _uiState.value
        when {
            state.editState.timelineSegments.size > 1 -> deleteSelectedTimelineSegment()
            state.selectedTextOverlayId != null -> removeTextOverlay(state.selectedTextOverlayId)
            state.selectedStickerId != null -> removeStickerOverlay(state.selectedStickerId)
            state.editState.audioTrack != null -> removeAudioTrack()
            state.editState.overlayImageUriString != null -> setOverlayImageUri(null)
            state.editState.chromaEnabled -> updateEditState { it.copy(chromaEnabled = false) }
            state.editState.maskShape != MaskShapeType.NONE -> updateEditState { it.copy(maskShape = MaskShapeType.NONE) }
        }
    }

    fun setPlaybackSpeed(speed: Float, recordUndo: Boolean = false) {
        updateEditState(recordUndo = recordUndo) {
            it.copy(playbackSpeed = speed.coerceIn(0.25f, 4.0f))
        }
    }

    fun setMaskPosition(normX: Float, normY: Float, scale: Float? = null, rotation: Float? = null) {
        updateEditState(recordUndo = false) { state ->
            state.copy(
                maskCenterX = normX.coerceIn(0.05f, 0.95f),
                maskCenterY = normY.coerceIn(0.05f, 0.95f),
                maskScale = (scale ?: state.maskScale).coerceIn(0.15f, 1.35f),
                maskRotation = rotation ?: state.maskRotation
            )
        }
    }

    fun setTrackingAnchor(normX: Float, normY: Float) {
        updateEditState(recordUndo = true) { state ->
            state.copy(
                cameraTrackingEnabled = true,
                cameraTrackingTarget = TrackingTarget.MOTION_POINT,
                trackingAnchorX = normX.coerceIn(0.08f, 0.92f),
                trackingAnchorY = normY.coerceIn(0.08f, 0.92f)
            )
        }
    }

    fun setOverlayImageUri(uri: Uri?) {
        updateEditState { it.copy(overlayImageUriString = uri?.toString()) }
    }

    fun setClipAnimation(animation: ClipTransitionAnimation) {
        updateEditState { it.copy(clipAnimation = animation) }
    }

    // ==================== TEXT OVERLAYS & STYLISH FONTS ====================

    fun addInteractiveTextBox(initialText: String = "Enter text", colorArgb: Long = 0xFFFFFFFF) {
        val newId = UUID.randomUUID().toString()
        val countOffset = (_uiState.value.editState.textOverlays.size % 4) * 0.08f
        updateEditState { state ->
            val currentLast = state.textOverlays.lastOrNull()
            state.copy(
                textOverlays = state.textOverlays + TextOverlayItem(
                    id = newId,
                    text = initialText,
                    colorArgb = colorArgb,
                    normX = 0.5f,
                    normY = (0.34f + countOffset).coerceAtMost(0.76f),
                    fontId = currentLast?.fontId ?: "sacramento",
                    artPreset = currentLast?.artPreset ?: TextArtPreset.NONE,
                    stylePreset = currentLast?.stylePreset ?: TextBoxStylePreset.SHADOW_GLOW,
                    animationPreset = currentLast?.animationPreset ?: TextAnimationPreset.NONE
                )
            )
        }
        _uiState.update { it.copy(selectedTextOverlayId = newId) }
    }

    fun selectTextOverlay(id: String?) {
        _uiState.update { it.copy(selectedTextOverlayId = id) }
    }

    fun commitTextOverlayContent(text: String, colorArgb: Long? = null) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        val targetId = _uiState.value.selectedTextOverlayId
            ?: _uiState.value.editState.textOverlays.lastOrNull()?.id

        if (targetId == null) {
            addInteractiveTextBox(clean, colorArgb ?: 0xFFFFFFFF)
        } else {
            updateEditState { state ->
                state.copy(
                    textOverlays = state.textOverlays.map { item ->
                        if (item.id == targetId) {
                            item.copy(
                                text = clean,
                                colorArgb = colorArgb ?: item.colorArgb
                            )
                        } else item
                    }
                )
            }
        }
    }

    fun updateSelectedTextStyle(
        colorArgb: Long? = null,
        fontId: String? = null,
        artPreset: TextArtPreset? = null,
        stylePreset: TextBoxStylePreset? = null,
        animationPreset: TextAnimationPreset? = null,
        scale: Float? = null,
        rotationDeg: Float? = null,
        recordUndo: Boolean = true
    ) {
        val targetId = _uiState.value.selectedTextOverlayId
            ?: _uiState.value.editState.textOverlays.lastOrNull()?.id
        if (targetId == null) {
            addInteractiveTextBox("Enter text", colorArgb ?: 0xFFFFFFFF)
            return
        }
        updateEditState(recordUndo = recordUndo) { state ->
            state.copy(
                textOverlays = state.textOverlays.map { item ->
                    if (item.id == targetId) {
                        item.copy(
                            colorArgb = colorArgb ?: item.colorArgb,
                            fontId = fontId ?: item.fontId,
                            artPreset = artPreset ?: item.artPreset,
                            stylePreset = stylePreset ?: item.stylePreset,
                            animationPreset = animationPreset ?: item.animationPreset,
                            scale = (scale ?: item.scale).coerceIn(0.45f, 3.2f),
                            rotationDeg = rotationDeg ?: item.rotationDeg
                        )
                    } else item
                }
            )
        }
    }

    fun transformTextOverlay(
        id: String,
        normX: Float,
        normY: Float,
        scale: Float,
        rotationDeg: Float
    ) {
        _uiState.update { state ->
            state.copy(
                selectedTextOverlayId = id,
                editState = state.editState.copy(
                    textOverlays = state.editState.textOverlays.map { item ->
                        if (item.id == id) {
                            item.copy(
                                normX = normX.coerceIn(0.08f, 0.92f),
                                normY = normY.coerceIn(0.08f, 0.92f),
                                scale = scale.coerceIn(0.45f, 3.2f),
                                rotationDeg = rotationDeg
                            )
                        } else item
                    }
                )
            )
        }
    }

    fun removeTextOverlay(id: String) {
        updateEditState { state ->
            state.copy(textOverlays = state.textOverlays.filterNot { it.id == id })
        }
        _uiState.update { state ->
            if (state.selectedTextOverlayId == id) {
                state.copy(selectedTextOverlayId = state.editState.textOverlays.lastOrNull()?.id)
            } else state
        }
    }

    fun toggleFavoriteFont(fontId: String) {
        viewModelScope.launch {
            repository.toggleFavoriteFont(fontId)
        }
    }

    // ==================== STICKERS / BRUSHES ====================

    fun addStickerOverlay(label: String, isBadge: Boolean = true) {
        val newId = UUID.randomUUID().toString()
        val countOffset = (_uiState.value.editState.stickerOverlays.size % 4) * 0.06f
        updateEditState {
            it.copy(
                stickerOverlays = it.stickerOverlays + StickerOverlayItem(
                    id = newId,
                    label = label,
                    normX = (0.5f + countOffset * 0.4f).coerceIn(0.15f, 0.85f),
                    normY = (0.48f + countOffset).coerceIn(0.15f, 0.85f),
                    scale = if (isBadge) 1f else 1.15f,
                    rotationDeg = 0f,
                    isBadge = isBadge
                )
            )
        }
        _uiState.update { it.copy(selectedStickerId = newId) }
    }

    fun selectStickerOverlay(id: String?) {
        _uiState.update { it.copy(selectedStickerId = id) }
    }

    fun moveStickerOverlay(id: String, normX: Float, normY: Float) {
        transformStickerOverlay(id = id, normX = normX, normY = normY)
    }

    fun transformStickerOverlay(
        id: String,
        normX: Float,
        normY: Float,
        scale: Float? = null,
        rotationDeg: Float? = null
    ) {
        _uiState.update { state ->
            state.copy(
                selectedStickerId = id,
                editState = state.editState.copy(
                    stickerOverlays = state.editState.stickerOverlays.map { item ->
                        if (item.id == id) {
                            item.copy(
                                normX = normX.coerceIn(0.06f, 0.94f),
                                normY = normY.coerceIn(0.06f, 0.94f),
                                scale = (scale ?: item.scale).coerceIn(0.45f, 3.5f),
                                rotationDeg = rotationDeg ?: item.rotationDeg
                            )
                        } else item
                    }
                )
            )
        }
    }

    fun removeStickerOverlay(id: String) {
        updateEditState { state ->
            state.copy(stickerOverlays = state.stickerOverlays.filterNot { it.id == id })
        }
        _uiState.update { state ->
            if (state.selectedStickerId == id) {
                state.copy(selectedStickerId = state.editState.stickerOverlays.lastOrNull()?.id)
            } else state
        }
    }

    fun addMosaicPoint(normX: Float, normY: Float) {
        updateEditState {
            it.copy(mosaicPoints = it.mosaicPoints + NormPoint(normX.coerceIn(0f, 1f), normY.coerceIn(0f, 1f)))
        }
    }

    fun clearMosaicPoints() {
        updateEditState { it.copy(mosaicPoints = emptyList()) }
    }

    fun addRemovalPoint(normX: Float, normY: Float) {
        updateEditState {
            it.copy(removalPoints = it.removalPoints + NormPoint(normX.coerceIn(0f, 1f), normY.coerceIn(0f, 1f)))
        }
    }

    fun clearRemovalPoints() {
        updateEditState { it.copy(removalPoints = emptyList()) }
    }

    // ==================== AUDIO MENU, VIDEO-TO-AUDIO & REVERB DSP ====================

    fun selectBuiltInAudioPreset(preset: StudioMusicPreset) {
        val ctx = getApplication<Application>().applicationContext
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    exportStatus = ExportStatus.Processing(
                        progress = 0.08f,
                        title = "Loading Studio Audio",
                        message = "Preparing ${preset.title}..."
                    )
                )
            }
            val result = AudioDSPProcessor.generateStudioTrackWav(ctx, preset) { p ->
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Processing(
                            progress = p.coerceIn(0.08f, 0.98f),
                            title = "Loading Studio Audio",
                            message = "Rendering ${preset.title} (${(p * 100).toInt()}%)..."
                        )
                    )
                }
            }
            result.fold(
                onSuccess = { wavFile ->
                    val sourceType = if (preset.isBgm) AudioSourceType.BGM else AudioSourceType.MUSIC_LIST
                    val track = StudioAudioTrackState(
                        trackTitle = preset.title,
                        sourceType = sourceType,
                        rawFilePath = wavFile.absolutePath,
                        processedFilePath = wavFile.absolutePath,
                        durationMs = 8_000L
                    )
                    updateEditState { it.copy(audioTrack = track, muteAudio = false) }
                    _uiState.update { it.copy(exportStatus = ExportStatus.Idle) }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            exportStatus = ExportStatus.Error(
                                title = "Audio Load Failed",
                                message = err.localizedMessage ?: "Unable to prepare track"
                            )
                        )
                    }
                }
            )
        }
    }

    fun importAudioFileUri(uri: Uri, title: String = "Imported Audio") {
        extractOrImportAudioUri(
            uri = uri,
            title = title,
            sourceType = AudioSourceType.IMPORTED,
            dialogTitle = "Importing Audio",
            prefix = "imported_audio"
        )
    }

    fun extractAudioFromVideoUri(uri: Uri, title: String = "Extracted Video Audio") {
        extractOrImportAudioUri(
            uri = uri,
            title = title,
            sourceType = AudioSourceType.EXTRACTED_VIDEO_AUDIO,
            dialogTitle = "Video to Audio Converter",
            prefix = "video_to_audio"
        )
    }

    fun separateAudioFromCurrentVideo() {
        val source = _uiState.value.activeMediaSource
        if (source is MediaSource.ContentUri && source.isVideo) {
            extractAudioFromVideoUri(source.uri, "Separated Video Audio")
        } else {
            _uiState.update {
                it.copy(
                    exportStatus = ExportStatus.Error(
                        title = "No Video Loaded",
                        message = "Open a video clip or use 'Video to Audio' to pick any video from your device."
                    )
                )
            }
        }
    }

    private fun extractOrImportAudioUri(
        uri: Uri,
        title: String,
        sourceType: AudioSourceType,
        dialogTitle: String,
        prefix: String
    ) {
        val ctx = getApplication<Application>().applicationContext
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    exportStatus = ExportStatus.Processing(
                        progress = 0.06f,
                        title = dialogTitle,
                        message = "Decoding audio stream..."
                    )
                )
            }
            val result = AudioDSPProcessor.extractAudioFromVideo(ctx, uri, prefix) { p ->
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Processing(
                            progress = p.coerceIn(0.06f, 0.98f),
                            title = dialogTitle,
                            message = "Converting to standalone PCM track (${(p * 100).toInt()}%)..."
                        )
                    )
                }
            }
            result.fold(
                onSuccess = { (wavFile, durationMs) ->
                    val track = StudioAudioTrackState(
                        trackTitle = title,
                        sourceType = sourceType,
                        rawFilePath = wavFile.absolutePath,
                        processedFilePath = wavFile.absolutePath,
                        durationMs = durationMs
                    )
                    updateEditState { it.copy(audioTrack = track, muteAudio = false) }
                    _uiState.update { it.copy(exportStatus = ExportStatus.Idle) }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            exportStatus = ExportStatus.Error(
                                title = "$dialogTitle Failed",
                                message = err.localizedMessage ?: "Could not extract audio from this file."
                            )
                        )
                    }
                }
            )
        }
    }

    fun attachRecordedVoiceover(file: File, durationMs: Long) {
        val track = StudioAudioTrackState(
            trackTitle = "Voiceover Recording",
            sourceType = AudioSourceType.RECORDED,
            rawFilePath = file.absolutePath,
            processedFilePath = file.absolutePath,
            durationMs = durationMs.coerceAtLeast(1_000L)
        )
        updateEditState { it.copy(audioTrack = track, muteAudio = false) }
    }

    fun applyAudioDspEffects(
        reverbAmount: Float,
        vocalSeparation: Boolean,
        noiseReduction: Boolean,
        noiseReductionStrength: Float = if (noiseReduction) 0.75f else 0f,
        fadeInOut: Boolean = false
    ) {
        val currentTrack = _uiState.value.editState.audioTrack ?: return
        val rawFile = File(currentTrack.rawFilePath)
        if (!rawFile.exists()) return
        val ctx = getApplication<Application>().applicationContext

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    exportStatus = ExportStatus.Processing(
                        progress = 0.08f,
                        title = "Applying Audio Studio FX",
                        message = "Processing Reverb, Vocal Isolation & Denoise..."
                    )
                )
            }
            val result = AudioDSPProcessor.applyAudioEffectsToWav(
                context = ctx,
                inputFile = rawFile,
                reverbAmount = reverbAmount,
                vocalSeparation = vocalSeparation,
                noiseReduction = noiseReduction,
                noiseReductionStrength = noiseReductionStrength,
                fadeInOut = fadeInOut
            ) { p ->
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Processing(
                            progress = p.coerceIn(0.08f, 0.98f),
                            title = "Applying Audio Studio FX",
                            message = "Rendering DSP filters (${(p * 100).toInt()}%)..."
                        )
                    )
                }
            }
            result.fold(
                onSuccess = { outFile ->
                    updateEditState {
                        it.copy(
                            audioTrack = currentTrack.copy(
                                processedFilePath = outFile.absolutePath,
                                reverbAmount = reverbAmount,
                                vocalSeparation = vocalSeparation,
                                noiseReduction = noiseReduction,
                                noiseReductionStrength = noiseReductionStrength,
                                fadeInOut = fadeInOut
                            )
                        )
                    }
                    _uiState.update { it.copy(exportStatus = ExportStatus.Idle) }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            exportStatus = ExportStatus.Error(
                                title = "Audio DSP Failed",
                                message = err.localizedMessage ?: "Failed to process audio effects."
                            )
                        )
                    }
                }
            )
        }
    }

    private suspend fun ensureAudioTrackAvailable(): StudioAudioTrackState? {
        _uiState.value.editState.audioTrack?.let { existing ->
            if (File(existing.rawFilePath).exists()) return existing
        }
        val source = _uiState.value.activeMediaSource
        if (source is MediaSource.ContentUri && source.isVideo) {
            val ctx = getApplication<Application>().applicationContext
            val extracted = AudioDSPProcessor.extractAudioFromVideo(ctx, source.uri, "studio_video_audio") { p ->
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Processing(
                            progress = (p * 0.45f).coerceIn(0.05f, 0.45f),
                            title = "Preparing Video Audio",
                            message = "Extracting audio stream (${(p * 100).toInt()}%)..."
                        )
                    )
                }
            }
            val pair = extracted.getOrNull() ?: return null
            val track = StudioAudioTrackState(
                trackTitle = "Video Audio",
                sourceType = AudioSourceType.EXTRACTED_VIDEO_AUDIO,
                rawFilePath = pair.first.absolutePath,
                processedFilePath = pair.first.absolutePath,
                durationMs = pair.second
            )
            updateEditState { it.copy(audioTrack = track, muteAudio = false) }
            return track
        }
        return null
    }

    fun runOneTapAudioFeature(
        vocalSeparation: Boolean? = null,
        noiseReduction: Boolean? = null,
        noiseReductionStrength: Float? = null,
        fadeInOut: Boolean? = null
    ) {
        val ctx = getApplication<Application>().applicationContext
        viewModelScope.launch {
            val track = ensureAudioTrackAvailable()
            if (track == null) {
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Error(
                            title = "No Audio Track",
                            message = "Open a video with audio or import/record audio in Sound first."
                        )
                    )
                }
                return@launch
            }
            val nextVocal = vocalSeparation ?: track.vocalSeparation
            val nextNoise = noiseReduction ?: track.noiseReduction
            val nextNoiseStrength = noiseReductionStrength ?: track.noiseReductionStrength
            val nextFade = fadeInOut ?: track.fadeInOut
            _uiState.update {
                it.copy(
                    exportStatus = ExportStatus.Processing(
                        progress = 0.50f,
                        title = if (noiseReduction == true) "Noise Reduction" else "Vocal Separation",
                        message = "Applying clean PCM DSP filter..."
                    )
                )
            }
            val result = AudioDSPProcessor.applyAudioEffectsToWav(
                context = ctx,
                inputFile = File(track.rawFilePath),
                reverbAmount = track.reverbAmount,
                vocalSeparation = nextVocal,
                noiseReduction = nextNoise,
                noiseReductionStrength = nextNoiseStrength,
                fadeInOut = nextFade
            ) { p ->
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Processing(
                            progress = (0.50f + p * 0.48f).coerceIn(0.50f, 0.99f),
                            title = if (noiseReduction == true) "Noise Reduction" else "Audio Studio FX",
                            message = "Filtering noise & enhancing clarity (${(p * 100).toInt()}%)..."
                        )
                    )
                }
            }
            result.fold(
                onSuccess = { outFile ->
                    updateEditState {
                        it.copy(
                            audioTrack = track.copy(
                                processedFilePath = outFile.absolutePath,
                                vocalSeparation = nextVocal,
                                noiseReduction = nextNoise,
                                noiseReductionStrength = nextNoiseStrength,
                                fadeInOut = nextFade
                            )
                        )
                    }
                    _uiState.update { it.copy(exportStatus = ExportStatus.Idle) }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            exportStatus = ExportStatus.Error(
                                title = "Audio DSP Failed",
                                message = err.localizedMessage ?: "Failed to process audio."
                            )
                        )
                    }
                }
            )
        }
    }

    fun scanAudioForSilence(thresholdDb: Float = _uiState.value.silenceThresholdDb, minSilenceMs: Long = _uiState.value.silenceMinMs) {
        viewModelScope.launch {
            _uiState.update { it.copy(silenceThresholdDb = thresholdDb, silenceMinMs = minSilenceMs) }
            val track = ensureAudioTrackAvailable()
            if (track == null) {
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Error(
                            title = "No Audio to Scan",
                            message = "Open a video with audio or attach an audio track in Sound."
                        )
                    )
                }
                return@launch
            }
            _uiState.update {
                it.copy(
                    exportStatus = ExportStatus.Processing(
                        progress = 0.55f,
                        title = "Detect Silence",
                        message = "Scanning PCM waveform for silent pauses..."
                    )
                )
            }
            val analysisRes = AudioDSPProcessor.analyzeSilenceInWavFile(
                inputFile = File(track.processedFilePath),
                thresholdDb = thresholdDb,
                minSilenceMs = minSilenceMs
            )
            analysisRes.fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            silenceAnalysis = result,
                            exportStatus = ExportStatus.Idle
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            exportStatus = ExportStatus.Error(
                                title = "Silence Scan Failed",
                                message = err.localizedMessage ?: "Unable to analyze audio."
                            )
                        )
                    }
                }
            )
        }
    }

    fun trimDetectedSilences(thresholdDb: Float = _uiState.value.silenceThresholdDb, minSilenceMs: Long = _uiState.value.silenceMinMs) {
        val ctx = getApplication<Application>().applicationContext
        viewModelScope.launch {
            val track = ensureAudioTrackAvailable() ?: return@launch
            _uiState.update {
                it.copy(
                    exportStatus = ExportStatus.Processing(
                        progress = 0.25f,
                        title = "Removing Silent Gaps",
                        message = "Cutting silent intervals from audio..."
                    )
                )
            }
            val trimRes = AudioDSPProcessor.trimSilentRegionsWav(
                context = ctx,
                inputFile = File(track.processedFilePath),
                thresholdDb = thresholdDb,
                minSilenceMs = minSilenceMs
            ) { p ->
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Processing(
                            progress = p.coerceIn(0.25f, 0.98f),
                            title = "Removing Silent Gaps",
                            message = "Rebuilding clean timeline audio (${(p * 100).toInt()}%)..."
                        )
                    )
                }
            }
            trimRes.fold(
                onSuccess = { (trimmedFile, analysis) ->
                    val newDurMs = (analysis.totalDurationMs - analysis.silentDurationMs).coerceAtLeast(1_000L)
                    updateEditState {
                        it.copy(
                            audioTrack = track.copy(
                                rawFilePath = trimmedFile.absolutePath,
                                processedFilePath = trimmedFile.absolutePath,
                                durationMs = newDurMs
                            )
                        )
                    }
                    _uiState.update {
                        it.copy(
                            silenceAnalysis = analysis.copy(silentCount = 0, silentDurationMs = 0L),
                            exportStatus = ExportStatus.Idle
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            exportStatus = ExportStatus.Error(
                                title = "Silence Removal Failed",
                                message = err.localizedMessage ?: "Could not trim silent intervals."
                            )
                        )
                    }
                }
            )
        }
    }

    fun saveProcessedAudioToDevice() {
        val currentTrack = _uiState.value.editState.audioTrack ?: return
        val file = File(currentTrack.processedFilePath)
        if (!file.exists()) return
        val ctx = getApplication<Application>().applicationContext

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    exportStatus = ExportStatus.Processing(
                        progress = 0.55f,
                        title = "Saving Audio Track",
                        message = "Writing WAV audio to Music/Bugti Editz..."
                    )
                )
            }
            val saveRes = AudioDSPProcessor.saveAudioToMusicCollection(ctx, file, currentTrack.trackTitle)
            saveRes.fold(
                onSuccess = { outUri ->
                    _uiState.update {
                        it.copy(
                            exportStatus = ExportStatus.Success(
                                outputUri = outUri,
                                isVideo = false,
                                isAudio = true,
                                summary = "Saved '${currentTrack.trackTitle}' to Music/Bugti Editz"
                            )
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            exportStatus = ExportStatus.Error(
                                title = "Audio Save Failed",
                                message = err.localizedMessage ?: "Could not save audio file."
                            )
                        )
                    }
                }
            )
        }
    }

    fun removeAudioTrack() {
        updateEditState { it.copy(audioTrack = null) }
        _uiState.update { it.copy(silenceAnalysis = null) }
    }

    // ==================== PREVIEW & EXPORT ====================

    private fun schedulePreviewReprocess() {
        val original = _uiState.value.originalBitmap ?: return
        val ctx = getApplication<Application>().applicationContext
        previewJob?.cancel()
        previewJob = viewModelScope.launch {
            delay(45L)
            _uiState.update { it.copy(isProcessingPreview = true) }
            val currentSnapshot = _uiState.value
            val stateSnapshot = currentSnapshot.editState
            val cachedMask = currentSnapshot.personMaskBitmap
            val maskToPass = if (stateSnapshot.removeBackground) {
                cachedMask ?: BackgroundRemoverEngine.extractPersonMask(original)
            } else {
                cachedMask
            }
            val result = ImageEnhancerEngine.processBitmap(
                source = original,
                editState = stateSnapshot,
                isExport = false,
                context = ctx,
                personMask = maskToPass
            )
            _uiState.update {
                it.copy(
                    personMaskBitmap = maskToPass,
                    processedBitmap = result,
                    isProcessingPreview = false
                )
            }
        }
    }

    fun exportCurrentCreation() {
        val state = _uiState.value
        val source = state.activeMediaSource ?: return
        val ctx = getApplication<Application>().applicationContext

        if (source is MediaSource.ContentUri && source.isVideo) {
            _uiState.update {
                it.copy(
                    exportStatus = ExportStatus.Processing(
                        progress = 0.05f,
                        title = "Exporting Video",
                        message = "Rendering enhanced video..."
                    )
                )
            }
            activeTransformer = VideoEnhancerEngine.exportEditedVideo(
                context = ctx,
                sourceUri = source.uri,
                durationMs = state.videoDurationMs,
                editState = state.editState,
                onProgress = { progress ->
                    _uiState.update {
                        it.copy(
                            exportStatus = ExportStatus.Processing(
                                progress = progress.coerceAtLeast(0.08f),
                                title = "Exporting Video",
                                message = "Encoding ${state.editState.exportResolution.label}..."
                            )
                        )
                    }
                },
                onComplete = { result ->
                    activeTransformer = null
                    result.fold(
                        onSuccess = { outUri ->
                            recordExportCreation(
                                title = "Video ${state.editState.enhanceMode.title}",
                                uri = outUri,
                                isVideo = true,
                                editState = state.editState
                            )
                            _uiState.update {
                                it.copy(
                                    exportStatus = ExportStatus.Success(
                                        outputUri = outUri,
                                        isVideo = true,
                                        summary = "Saved ${state.editState.exportResolution.label} MP4 to Movies/Bugti Editz"
                                    )
                                )
                            }
                        },
                        onFailure = { err ->
                            _uiState.update {
                                it.copy(
                                    exportStatus = ExportStatus.Error(
                                        title = "Export Failed",
                                        message = err.localizedMessage ?: "Video export failed"
                                    )
                                )
                            }
                        }
                    )
                }
            )
        } else {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Processing(
                            progress = 0.25f,
                            title = "Exporting Creation",
                            message = "Upscaling & sharpening..."
                        )
                    )
                }
                val highRes = ImageEnhancerEngine.loadSourceBitmap(
                    ctx,
                    source,
                    maxDimension = state.editState.exportResolution.maxDim
                )
                if (highRes == null) {
                    _uiState.update {
                        it.copy(exportStatus = ExportStatus.Error("Could not read source photo", "Export Failed"))
                    }
                    return@launch
                }
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Processing(
                            progress = 0.65f,
                            title = "Exporting Creation",
                            message = "Applying color, fonts & retouch pass..."
                        )
                    )
                }
                val rendered = ImageEnhancerEngine.processBitmap(
                    source = highRes,
                    editState = state.editState,
                    isExport = true,
                    context = ctx,
                    personMask = state.personMaskBitmap
                )
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Processing(
                            progress = 0.90f,
                            title = "Exporting Creation",
                            message = "Saving to Gallery..."
                        )
                    )
                }
                val labelTitle = when (source) {
                    is MediaSource.SampleDrawable -> source.title
                    is MediaSource.ContentUri -> "Bugti Photo"
                }
                val preserveAlpha = ((state.editState.removeBackground || state.editState.chromaEnabled) &&
                    state.editState.bgReplaceMode == BackgroundReplaceMode.TRANSPARENT) ||
                    state.editState.maskShape != MaskShapeType.NONE
                val saveRes = ImageEnhancerEngine.saveBitmapToGallery(
                    context = ctx,
                    bitmap = rendered,
                    displayName = "BugtiEditz",
                    preserveAlpha = preserveAlpha
                )
                saveRes.fold(
                    onSuccess = { outUri ->
                        recordExportCreation(
                            title = "$labelTitle (${state.editState.enhanceMode.shortBadge.replace("\n", " ")})",
                            uri = outUri,
                            isVideo = false,
                            editState = state.editState
                        )
                        _uiState.update {
                            it.copy(
                                exportStatus = ExportStatus.Success(
                                    outputUri = outUri,
                                    isVideo = false,
                                    summary = "Saved ${rendered.width}x${rendered.height} to Pictures/Bugti Editz"
                                )
                            )
                        }
                    },
                    onFailure = { err ->
                        _uiState.update {
                            it.copy(
                                exportStatus = ExportStatus.Error(
                                    title = "Export Failed",
                                    message = err.localizedMessage ?: "Photo export failed"
                                )
                            )
                        }
                    }
                )
            }
        }
    }

    fun cancelExport() {
        runCatching { activeTransformer?.cancel() }
        activeTransformer = null
        _uiState.update { it.copy(exportStatus = ExportStatus.Idle) }
    }

    fun dismissExportStatus() {
        _uiState.update { it.copy(exportStatus = ExportStatus.Idle) }
    }

    private fun recordExportCreation(
        title: String,
        uri: Uri,
        isVideo: Boolean,
        editState: StudioEditState
    ) {
        viewModelScope.launch {
            val modeText = if (editState.enhanceMode != EnhanceMode.NONE) {
                editState.enhanceMode.title
            } else if (editState.filterPreset != FilterPreset.NATURAL) {
                editState.filterPreset.label
            } else {
                "Studio Edit"
            }
            repository.addSavedCreation(
                SavedCreation(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    uriString = uri.toString(),
                    isVideo = isVideo,
                    resolutionLabel = editState.exportResolution.label,
                    modeLabel = modeText,
                    createdAtMillis = System.currentTimeMillis()
                )
            )
        }
    }

    fun removeSavedCreation(id: String) {
        viewModelScope.launch { repository.removeCreation(id) }
    }

    fun removeSelectedCreations(ids: Set<String>) {
        viewModelScope.launch { repository.removeCreations(ids) }
    }

    fun renameCreation(id: String, newTitle: String) {
        viewModelScope.launch { repository.renameCreation(id, newTitle) }
    }

    fun duplicateCreation(id: String) {
        viewModelScope.launch {
            repository.duplicateCreation(id, UUID.randomUUID().toString())
        }
    }

    fun markCreationsUploadedToCloud(ids: Set<String>) {
        viewModelScope.launch { repository.markUploadedToCloud(ids) }
    }

    fun createQuickDraft() {
        viewModelScope.launch {
            val count = savedCreations.value.size + 1
            val samples = listOf(
                R.drawable.img_demo_skyline,
                R.drawable.img_showcase_cinema,
                R.drawable.img_demo_portrait,
                R.drawable.img_template_film
            )
            val chosenRes = samples[count % samples.size]
            val currentPrefs = preferences.value
            repository.addSavedCreation(
                SavedCreation(
                    id = UUID.randomUUID().toString(),
                    title = "Studio Draft #$count",
                    uriString = "",
                    isVideo = false,
                    resolutionLabel = currentPrefs.defaultResolution.label,
                    modeLabel = "Ultra HD",
                    createdAtMillis = System.currentTimeMillis(),
                    isUploadedToCloud = false,
                    sampleResId = chosenRes
                )
            )
        }
    }

    fun clearSavedHistory() {
        viewModelScope.launch { repository.clearHistory() }
    }

    fun updatePreferences(transform: (StudioPreferences) -> StudioPreferences) {
        viewModelScope.launch {
            repository.updatePreferences(transform(preferences.value))
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun onAuthUserObserved(user: com.google.firebase.auth.FirebaseUser?) {
        if (user != null) {
            viewModelScope.launch {
                repository.setAuthSession(
                    isLoggedIn = true,
                    email = user.email,
                    defaultDisplayName = user.displayName
                )
            }
        }
    }

    fun onUserSignedOut() {
        viewModelScope.launch {
            repository.setAuthSession(
                isLoggedIn = false,
                email = null,
                defaultDisplayName = null
            )
        }
    }

    fun saveProfileName(name: String) {
        viewModelScope.launch {
            repository.saveProfileName(name)
        }
    }

    fun saveProfilePhoto(uri: Uri, onCompleted: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val savedPath = repository.saveProfilePhotoFromUri(uri)
            onCompleted(savedPath != null)
        }
    }

    fun clearProfilePhoto() {
        viewModelScope.launch {
            repository.clearProfilePhoto()
        }
    }

    fun claimBonusCredits(amount: Int = 10) {
        updatePreferences { it.copy(credits = it.credits + amount) }
    }

    fun onAppStopped() {
        if (preferences.value.autoWhiteOnClose && preferences.value.themeMode != AppThemeMode.WHITE) {
            viewModelScope.launch(kotlinx.coroutines.NonCancellable) {
                repository.resetToWhiteThemeIfConfigured()
            }
        }
    }
}
