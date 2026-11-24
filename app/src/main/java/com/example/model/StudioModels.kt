package com.example.model

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.R

enum class HomeTab {
    HOME, TEMPLATES, CLOUD, ME
}

enum class AppThemeMode {
    WHITE, BLACK
}

enum class StudioBadge(val label: String) {
    NONE(""),
    HOT("Hot"),
    PRO("PRO"),
    NEW("New")
}

enum class StudioToolId(val label: String, val category: String, val badge: StudioBadge = StudioBadge.NONE) {
    QUALITY_ENHANCER("Quality Enhancer", "Enhance", StudioBadge.HOT),
    SMART_ENHANCE("Smart Enhance", "Enhance", StudioBadge.PRO),
    SUPER_RESOLUTION("Super Resolution", "Enhance", StudioBadge.NONE),
    BATCH_ENHANCE("Batch Enhance", "Enhance", StudioBadge.NEW),
    AI_REMOVAL("AI Removal", "Retouch", StudioBadge.NONE),
    FACE("Face", "Retouch", StudioBadge.NONE),
    BODY("Body", "Retouch", StudioBadge.NONE),
    AI_RETOUCH("AI Retouch", "Retouch", StudioBadge.NONE),
    COLOR_MATCH("Color Match", "Color", StudioBadge.NEW),
    HAIR("Hair", "Retouch", StudioBadge.NONE),
    MAKEUP("Makeup", "Retouch", StudioBadge.NONE),
    ALL_TOOLS("All Tools(56)", "Catalog", StudioBadge.HOT),

    // Editor bottom bar tools
    EDIT("Edit", "Edit"),
    FILTERS("Filter", "Color"),
    ADJUST("Adjust", "Color"),
    ENHANCE("AI Edit", "Enhance", StudioBadge.HOT),
    AI_EDIT("AI Edit", "Enhance", StudioBadge.HOT),
    EFFECTS("FX", "Effects"),
    TEXT("Text", "Creative"),
    MOSAIC("Mosaic", "Privacy"),
    REMOVAL("Removal", "Retouch"),
    OVERLAY("Overlay", "Effects"),
    ANIMATE("Animate", "Motion"),
    CUTOUT("Cutout", "AI Studio", StudioBadge.HOT),
    CHROMA("Chroma", "AI Studio"),
    FREEZE("Freeze", "Timeline"),
    REPLACE("Replace", "Timeline"),
    CAMERA_TRACKING("Camera Tracking", "Motion", StudioBadge.NEW),
    VOCAL_SEPARATION("Vocal Separation", "Audio"),
    NOISE_REDUCTION("Noise reduction", "Audio"),
    DETECT_SILENCE("Detect Silence", "Audio"),
    SEPARATE_AUDIO("Separate Audio", "Audio"),
    SOUND("Sound", "Audio"),
    COPY("Copy", "Timeline"),
    TRIM("Trim", "Timeline"),
    SPLIT("Split", "Timeline"),
    MASK("Mask", "Edit"),
    STABILIZE("Stabilize", "Motion"),
    CUT("Cut", "Timeline"),
    SPEED("Speed", "Timeline"),
    ANIMATION("Animation", "Motion"),
    VOLUME("Volume", "Audio"),
    DELETE("Delete", "Timeline"),
    VIDEO_QUALITY("Video Quality", "Enhance", StudioBadge.PRO),
    FADE("Fade", "Edit"),
    POSITION("Position", "Transform"),
    SIZE("Size", "Transform"),
    MIRROR("Mirror", "Transform"),
    FLIP("Flip", "Transform"),
    FIT("Fit", "Transform"),
    AUDIO("Audio", "Edit"),
    STICKERS("Stickers", "Creative"),
    FRAMES("Frames", "Creative"),
    MAGNIFIER("Magnifier", "Inspect"),
    CANVAS("Canvas", "Edit"),
    CROP("Crop", "Edit"),
    ROTATE("Rotate", "Edit"),
    BG_REMOVER("BG Remover", "AI Studio", StudioBadge.HOT),
    TRANSITIONS("Transitions", "Effects", StudioBadge.NEW),
    TEMPLATE("Template", "Templates"),
    SKIN_SMOOTH("Skin Smooth", "Retouch"),
    BRIGHTEN("Brighten", "Retouch")
}

enum class EnhanceSegment {
    GENERAL, SCENARIO
}

enum class EnhanceMode(
    val title: String,
    val shortBadge: String,
    val description: String,
    val segment: EnhanceSegment,
    val badge: StudioBadge = StudioBadge.NONE,
    val defaultStrength: Float = 0.75f
) {
    NONE("Original", "RAW", "Original unenhanced source media", EnhanceSegment.GENERAL, StudioBadge.NONE, 0f),
    HD("HD", "HD", "Balanced clarity boost and edge sharpening", EnhanceSegment.GENERAL, StudioBadge.NONE, 0.55f),
    ULTRA_HD("Ultra HD", "HD+", "Advanced clarity — less noise, more detail", EnhanceSegment.GENERAL, StudioBadge.NONE, 0.80f),
    PORTRAIT("Portrait", "FACE", "Face lock clarity with natural skin smoothness", EnhanceSegment.GENERAL, StudioBadge.NONE, 0.72f),
    AI_UHD("AI UHD", "AI\nUHD", "Deep detail reconstruction & dynamic contrast", EnhanceSegment.GENERAL, StudioBadge.HOT, 0.92f),

    NIGHT_DENOISE("Night Denoise", "NIGHT", "Low-light grain suppression & shadow lift", EnhanceSegment.SCENARIO, StudioBadge.NONE, 0.85f),
    ANIME_4K("4K Line", "4K", "Crisp vector-like contours & vibrant color pop", EnhanceSegment.SCENARIO, StudioBadge.HOT, 0.88f),
    OLD_PHOTO("Restore", "REST", "Contrast recovery for faded & soft captures", EnhanceSegment.SCENARIO, StudioBadge.NONE, 0.78f),
    MOTION_FIX("Deblur", "FIX", "Micro-sharpening for handheld motion softness", EnhanceSegment.SCENARIO, StudioBadge.NEW, 0.82f)
}

enum class FilterPreset(val label: String, val accentHex: Long) {
    NATURAL("Natural", 0xFF8C888C),
    TOKYO_NIGHT("Tokyo Night", 0xFF4ADBC8),
    GOLDEN_HOUR("Golden Hour", 0xFFFFB25C),
    TEAL_ORANGE("Teal & Orange", 0xFFFF6B4A),
    CRISP_BW("Crisp B&W", 0xFFDADADA),
    VELVET_MATTE("Velvet Matte", 0xFFB892B8),
    CYBER_NEON("Cyber Neon", 0xFFFF5CA8),
    FUJI_WARM("Fuji Warm", 0xFFE2C275)
}

enum class EffectMode(val label: String) {
    NONE("None"),
    FILM_GRAIN("Film Grain"),
    GLOW_BLOOM("Glow Bloom"),
    SOFT_FOCUS("Soft Focus"),
    CINEMA_HAZE("Cinema Haze")
}

enum class OverlayMode(val label: String) {
    NONE("None"),
    SUNSET_LEAK("Sunset Leak"),
    PRISM_FLARE("Prism Flare"),
    STUDIO_DUO("Studio Duo")
}

enum class AspectRatioOption(val label: String, val ratio: Float?) {
    ORIGINAL("Original", null),
    REEL_9_16("9:16", 9f / 16f),
    CINEMA_16_9("16:9", 16f / 9f),
    SQUARE_1_1("1:1", 1f),
    PORTRAIT_4_5("4:5", 4f / 5f),
    FREEFORM("Freeform", null)
}

@Immutable
data class NormCropRect(
    val left: Float = 0f,
    val top: Float = 0f,
    val right: Float = 1f,
    val bottom: Float = 1f
) {
    val isFull: Boolean
        get() = left <= 0.01f && top <= 0.01f && right >= 0.99f && bottom >= 0.99f
}

enum class FrameStyle(val label: String) {
    NONE("Clean"),
    CINEMA_BARS("Cinema 2.39"),
    POLAROID("Polaroid"),
    DARK_FILM("Film Border")
}

enum class ExportResolution(val label: String, val maxDim: Int) {
    HD_1080("1080p HD", 1920),
    UHD_4K("4K Ultra HD", 3840)
}

enum class EditorMainMode {
    RETOUCH, EDIT
}

// ==================== TEXT & STYLISH FONTS ====================

enum class TextToolTab(val label: String) {
    BASIC("Basic"),
    ART("Art"),
    STYLES("Styles"),
    FONTS("Fonts"),
    ANIMATION("Animation")
}

enum class FontLanguageFilter(val label: String) {
    FAVORITES("Favorites"),
    ALL("All (100)"),
    ENGLISH("English"),
    URDU("Urdu"),
    HINDI("Hindi"),
    SIMPLIFIED_CHINESE("Chinese"),
    MULTI_LANGUAGE("Multi-Lang")
}

enum class BackgroundReplaceMode(val label: String) {
    TRANSPARENT("Transparent"),
    BLUR_BG("Blur BG"),
    STUDIO_WHITE("White"),
    SOLID_DARK("Studio Dark"),
    GRADIENT_PURPLE("Purple Glow"),
    GRADIENT_SUNSET("Sunset")
}

enum class MaskShapeType(val label: String) {
    NONE("None"),
    CIRCLE("Circle"),
    RECTANGLE("Rectangle"),
    LINEAR("Linear"),
    MIRROR("Mirror"),
    HEART("Heart"),
    STAR("Star")
}

enum class TrackingTarget(val label: String) {
    FACE("Face Lock"),
    SUBJECT("Center Subject"),
    MOTION_POINT("Custom Point")
}

enum class StabilizeLevel(val label: String, val cropMargin: Float, val detail: String) {
    OFF("Off", 0f, "Original uncropped framing"),
    MINIMAL("Minimal Crop", 0.04f, "4% margin · Preserves wide view"),
    RECOMMENDED("Recommended", 0.08f, "8% margin · Balanced smoothness"),
    MOST_STABLE("Most Stable", 0.14f, "14% margin · Gimbal lock")
}

enum class FxTab(val label: String) {
    BASIC("Basic Effect"),
    BODY("Body Effect")
}

enum class FxCategory(val label: String, val tab: FxTab) {
    ORIGINAL("Original", FxTab.BASIC),
    BASIC("Basic", FxTab.BASIC),
    BEATS("Beats", FxTab.BASIC),
    GLITCH("Glitch", FxTab.BASIC),
    RGB("RGB", FxTab.BASIC),
    BLUR("Blur", FxTab.BASIC),
    ROTATE("Rotate", FxTab.BASIC),
    RETRO("Retro", FxTab.BASIC),
    GLOW("Glow", FxTab.BASIC),
    BODY_ORIGINAL("Original", FxTab.BODY),
    BODY_AURA("Aura", FxTab.BODY),
    BODY_CLONE("Clone", FxTab.BODY),
    BODY_OUTLINE("Neon Outline", FxTab.BODY),
    BODY_HALO("Beat Halo", FxTab.BODY),
    BODY_LASER("Laser", FxTab.BODY)
}

@Immutable
data class FxEffectSpec(
    val id: String,
    val name: String,
    val tab: FxTab,
    val category: FxCategory,
    val accentArgb: Long
)

enum class SpeedModeTab(val label: String) {
    CURVE("Curve"),
    REGULAR("Regular")
}

enum class SpeedCurvePreset(val label: String, val points: List<Float>) {
    ORIGINAL("Original", listOf(1.0f, 1.0f, 1.0f, 1.0f, 1.0f)),
    CUSTOM("Custom", listOf(1.0f, 1.5f, 0.7f, 1.9f, 1.0f)),
    MONTAGE("Montage", listOf(0.8f, 2.0f, 2.3f, 0.7f, 1.3f)),
    HERO_TIME("Hero Time", listOf(1.7f, 1.4f, 0.45f, 0.45f, 1.7f)),
    BULLET_TIME("Bullet Time", listOf(1.9f, 0.45f, 0.35f, 0.45f, 1.9f)),
    JUMP_CUT("Jump Cut", listOf(2.5f, 0.6f, 2.5f, 0.6f, 2.5f)),
    FAST_IN("Fast In", listOf(2.8f, 1.8f, 1.1f, 0.9f, 0.85f)),
    FAST_OUT("Fast Out", listOf(0.85f, 0.9f, 1.1f, 1.8f, 2.8f))
}

enum class CanvasFitMode(val label: String) {
    FIT("Fit"),
    FILL("Fill"),
    STRETCH("Stretch"),
    BLUR_FIT("Blur Fit"),
    DARK_CANVAS("Dark"),
    WHITE_CANVAS("White")
}

enum class FilterCategory(val label: String) {
    AESTHETIC("Aesthetic"),
    CREATE("Create"),
    VIVID("Vivid"),
    ESSENCE("Essence"),
    FOG("Fog"),
    LUNE("Lune"),
    MOOD("Mood"),
    POLAROID("Polaroid"),
    SEASON("Season"),
    FILM("Film"),
    FOOD("Food"),
    LIFE("Life"),
    LIGHT("Light"),
    PERSON("Person"),
    RETRO("Retro"),
    SCENERY("Scenery"),
    TEAL_ORANGE("Teal Orange"),
    TRAVEL("Travel")
}

@Immutable
data class StudioFilterSpec(
    val id: String,
    val name: String,
    val category: FilterCategory,
    val swatchHex: Long,
    val secondaryHex: Long,
    val mappedPreset: FilterPreset,
    val warmthBoost: Float = 0f,
    val tintBoost: Float = 0f,
    val contrastBoost: Float = 0f,
    val satBoost: Float = 0f,
    val brightBoost: Float = 0f
)

enum class TextArtPreset(val label: String, val accentArgb: Long) {
    NONE("Clean", 0xFFFFFFFF),
    NEON_PURPLE("Neon Glow", 0xFF9C7CFF),
    SUNSET_GOLD("Cinema Gold", 0xFFFFC857),
    CYBER_TEAL("Cyber Cyan", 0xFF4ADBC8),
    CRIMSON_POP("Candy Pop", 0xFFFF5CA8),
    CHROME_INK("Chrome", 0xFFE5E9F0)
}

enum class TextBoxStylePreset(val label: String) {
    PLAIN("Plain"),
    SHADOW_GLOW("Shadow"),
    SOLID_PILL("Solid Pill"),
    FROST_CARD("Frosted"),
    OUTLINE_BADGE("Border")
}

enum class TextAnimationPreset(val label: String) {
    NONE("None"),
    FADE_IN("Fade In"),
    POP_ZOOM("Pop Zoom"),
    SLIDE_UP("Slide Up"),
    TYPEWRITER("Typewriter"),
    NEON_PULSE("Pulse")
}

@Immutable
data class StudioFontSpec(
    val id: String,
    val displayName: String,
    val sampleText: String,
    @FontRes val fontRes: Int,
    val language: FontLanguageFilter = FontLanguageFilter.ENGLISH
) {
    val isSimplifiedChinese: Boolean
        get() = language == FontLanguageFilter.SIMPLIFIED_CHINESE
}

@Immutable
data class TextOverlayItem(
    val id: String,
    val text: String = "Enter text",
    val colorArgb: Long = 0xFFFFFFFF,
    val normX: Float = 0.5f,
    val normY: Float = 0.35f,
    val scale: Float = 1f,
    val rotationDeg: Float = 0f,
    val fontId: String = "sacramento",
    val artPreset: TextArtPreset = TextArtPreset.NONE,
    val stylePreset: TextBoxStylePreset = TextBoxStylePreset.SHADOW_GLOW,
    val animationPreset: TextAnimationPreset = TextAnimationPreset.NONE
)

@Immutable
data class StickerOverlayItem(
    val id: String,
    val label: String,
    val normX: Float = 0.5f,
    val normY: Float = 0.5f,
    val scale: Float = 1f,
    val rotationDeg: Float = 0f,
    val isBadge: Boolean = true
)

@Immutable
data class NormPoint(
    val x: Float,
    val y: Float
)

// ==================== TIMELINE SEGMENTS & AUDIO STUDIO ====================

enum class ClipTransitionAnimation(val label: String) {
    NONE("None"),
    FADE_IN("Fade"),
    ZOOM_IN("Zoom"),
    SLIDE_LEFT("Slide"),
    NEON_FLASH("Flash")
}

@Immutable
data class TimelineSegment(
    val id: String,
    val startFraction: Float,
    val endFraction: Float,
    val label: String = "Clip",
    val isFrozen: Boolean = false,
    val frozenFramePath: String? = null,
    val freezeDurationMs: Long = 3_000L,
    val transitionAfterId: String = "none",
    val transitionAfterDurationMs: Int = 600
)

enum class AudioMenuTab(val label: String) {
    MUSIC_LIST("Music List"),
    IMPORT("Import"),
    BGM("BGM"),
    RECORD("Record")
}

enum class AudioSourceType(val label: String) {
    MUSIC_LIST("Music List"),
    IMPORTED("Imported Audio"),
    BGM("Studio BGM"),
    RECORDED("Mic Voiceover"),
    EXTRACTED_VIDEO_AUDIO("Video Audio")
}

@Immutable
data class StudioMusicPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val baseFreqHz: Float,
    val bpm: Int,
    val isBgm: Boolean
)

@Immutable
data class StudioAudioTrackState(
    val trackTitle: String,
    val sourceType: AudioSourceType,
    val rawFilePath: String,
    val processedFilePath: String = rawFilePath,
    val durationMs: Long = 8_000L,
    val volume: Float = 0.9f,
    val reverbAmount: Float = 0f,           // 0f .. 1f Schroeder hall reverb
    val vocalSeparation: Boolean = false,   // Mid-side vocal isolation
    val noiseReduction: Boolean = false,    // High-pass + spectral noise gate
    val noiseReductionStrength: Float = 0.75f,
    val fadeInOut: Boolean = false
)

@Immutable
data class StudioEditState(
    val enhanceMode: EnhanceMode = EnhanceMode.NONE,
    val enhanceStrength: Float = 0.75f,
    val filterPreset: FilterPreset = FilterPreset.NATURAL,
    val filterSpecId: String = "natural",
    val filterIntensity: Float = 0.75f,
    val brightness: Float = 0f,     // -0.5 .. +0.5
    val contrast: Float = 0f,       // -0.5 .. +0.5
    val saturation: Float = 0f,     // -0.5 .. +0.5
    val sharpen: Float = 0f,        // 0 .. 1
    val warmth: Float = 0f,         // -0.5 .. +0.5 (Color Balance Blue–Amber)
    val tint: Float = 0f,           // -0.5 .. +0.5 (Color Balance Green–Magenta)
    val vignette: Float = 0f,       // 0 .. 1
    // Retouch parameters
    val skinSmooth: Float = 0f,     // 0 .. 1
    val faceGlow: Float = 0f,       // 0 .. 1
    val bodyBalance: Float = 0f,    // -0.2 .. +0.2 subtle aspect slimming
    val hairGloss: Float = 0f,      // 0 .. 1
    val makeupTint: Float = 0f,     // 0 .. 1
    // Background Remover / Cutout (Keep Person, Remove Background)
    val removeBackground: Boolean = false,
    val bgReplaceMode: BackgroundReplaceMode = BackgroundReplaceMode.TRANSPARENT,
    val bgEdgeFeather: Float = 0.25f,
    // Chroma Key
    val chromaEnabled: Boolean = false,
    val chromaKeyColorHex: Long = 0xFF00C853,
    val chromaSimilarity: Float = 0.28f,
    val chromaSmoothness: Float = 0.14f,
    // Shape / Linear Masking
    val maskShape: MaskShapeType = MaskShapeType.NONE,
    val maskCenterX: Float = 0.5f,
    val maskCenterY: Float = 0.5f,
    val maskScale: Float = 0.62f,
    val maskFeather: Float = 0.12f,
    val maskRotation: Float = 0f,
    val maskInvert: Boolean = false,
    // Motion Stabilization & Camera Tracking
    val cameraTrackingEnabled: Boolean = false,
    val cameraTrackingTarget: TrackingTarget = TrackingTarget.FACE,
    val cameraTrackingStrength: Float = 0.65f,
    val trackingAnchorX: Float = 0.5f,
    val trackingAnchorY: Float = 0.42f,
    val stabilizeLevel: StabilizeLevel = StabilizeLevel.OFF,
    val stabilizeHorizonLock: Boolean = true,
    // Effects & Overlays
    val effectMode: EffectMode = EffectMode.NONE,
    val effectIntensity: Float = 0.5f,
    val fxEffectId: String = "fx_none",
    val fxIntensity: Float = 0.65f,
    val transitionEffectId: String = "none",
    val overlayMode: OverlayMode = OverlayMode.NONE,
    val overlayIntensity: Float = 0.45f,
    val overlayImageUriString: String? = null,
    val overlayImageOpacity: Float = 0.75f,
    // Geometry / Crop / Rotate / Position / Size / Mirror / Flip / Fit / Timeline Video Edit
    val rotationDegrees: Int = 0,
    val straightenDegrees: Float = 0f, // -180 .. +180 smooth degree ruler rotation
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val clipOffsetX: Float = 0f,       // -0.6 .. +0.6 Position X
    val clipOffsetY: Float = 0f,       // -0.6 .. +0.6 Position Y
    val clipScaleX: Float = 1f,        // 0.35 .. 2.5 Width W scale
    val clipScaleY: Float = 1f,        // 0.35 .. 2.5 Height H scale
    val canvasFitMode: CanvasFitMode = CanvasFitMode.FIT,
    val aspectRatio: AspectRatioOption = AspectRatioOption.ORIGINAL,
    val freeformCrop: NormCropRect = NormCropRect(),
    val frameStyle: FrameStyle = FrameStyle.NONE,
    val playbackSpeed: Float = 1f,
    val speedModeTab: SpeedModeTab = SpeedModeTab.REGULAR,
    val speedCurvePreset: SpeedCurvePreset = SpeedCurvePreset.ORIGINAL,
    val customSpeedCurvePoints: List<Float> = listOf(1f, 1f, 1f, 1f, 1f),
    val videoFadeInMs: Int = 0,
    val videoFadeOutMs: Int = 0,
    val animateDurationMs: Int = 720,
    val clipAnimation: ClipTransitionAnimation = ClipTransitionAnimation.NONE,
    val muteAudio: Boolean = false,
    val audioVolume: Float = 1f,       // 0f .. 3.0f (0% .. 300%)
    val trimStartFraction: Float = 0f,
    val trimEndFraction: Float = 1f,
    val timelineSegments: List<TimelineSegment> = listOf(
        TimelineSegment(id = "seg_0", startFraction = 0f, endFraction = 1f, label = "Main")
    ),
    val selectedSegmentId: String = "seg_0",
    val audioTrack: StudioAudioTrackState? = null,
    // Creative / Privacy / Removal
    val textOverlays: List<TextOverlayItem> = emptyList(),
    val stickerOverlays: List<StickerOverlayItem> = emptyList(),
    val mosaicPoints: List<NormPoint> = emptyList(),
    val removalPoints: List<NormPoint> = emptyList(),
    val removalBrushSize: Float = 0.055f,
    val exportResolution: ExportResolution = ExportResolution.HD_1080
) {
    val hasAnyEdits: Boolean
        get() = enhanceMode != EnhanceMode.NONE ||
            filterPreset != FilterPreset.NATURAL ||
            filterSpecId != "natural" ||
            brightness != 0f || contrast != 0f || saturation != 0f ||
            sharpen != 0f || warmth != 0f || tint != 0f || vignette != 0f ||
            skinSmooth != 0f || faceGlow != 0f || bodyBalance != 0f ||
            hairGloss != 0f || makeupTint != 0f ||
            removeBackground || chromaEnabled ||
            maskShape != MaskShapeType.NONE ||
            cameraTrackingEnabled || stabilizeLevel != StabilizeLevel.OFF ||
            effectMode != EffectMode.NONE || fxEffectId != "fx_none" ||
            transitionEffectId != "none" ||
            overlayMode != OverlayMode.NONE || overlayImageUriString != null ||
            rotationDegrees != 0 || straightenDegrees != 0f || flipHorizontal || flipVertical ||
            clipOffsetX != 0f || clipOffsetY != 0f || clipScaleX != 1f || clipScaleY != 1f ||
            canvasFitMode != CanvasFitMode.FIT ||
            aspectRatio != AspectRatioOption.ORIGINAL || !freeformCrop.isFull ||
            frameStyle != FrameStyle.NONE ||
            playbackSpeed != 1f || speedCurvePreset != SpeedCurvePreset.ORIGINAL ||
            videoFadeInMs > 0 || videoFadeOutMs > 0 ||
            clipAnimation != ClipTransitionAnimation.NONE ||
            muteAudio || audioVolume != 1f || audioTrack != null ||
            trimStartFraction > 0f || trimEndFraction < 1f ||
            timelineSegments.size > 1 || timelineSegments.any { it.isFrozen || it.transitionAfterId != "none" } ||
            textOverlays.isNotEmpty() || stickerOverlays.isNotEmpty() ||
            mosaicPoints.isNotEmpty() || removalPoints.isNotEmpty()
}

sealed interface MediaSource {
    data class ContentUri(val uri: Uri, val isVideo: Boolean) : MediaSource
    data class SampleDrawable(@DrawableRes val resId: Int, val title: String) : MediaSource
}

@Immutable
data class StudioTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val tag: String,
    val category: String = "General",
    val durationSec: Int = 15,
    @DrawableRes val previewRes: Int,
    val editState: StudioEditState
)

@Immutable
data class SavedCreation(
    val id: String,
    val title: String,
    val uriString: String,
    val isVideo: Boolean,
    val resolutionLabel: String,
    val modeLabel: String,
    val createdAtMillis: Long,
    val isUploadedToCloud: Boolean = false,
    @DrawableRes val sampleResId: Int? = null
)

@Immutable
data class StudioPreferences(
    val themeMode: AppThemeMode = AppThemeMode.WHITE,
    val autoWhiteOnClose: Boolean = false,
    val autoSaveDrafts: Boolean = true,
    val cloudAutoBackup: Boolean = false,
    val credits: Int = 25,
    val defaultResolution: ExportResolution = ExportResolution.UHD_4K,
    val autoFaceLock: Boolean = true,
    val showBeforeAfterHint: Boolean = true,
    val isLoggedIn: Boolean = false,
    val loggedInEmail: String = "",
    val profileName: String = "",
    val profilePhotoPath: String = "",
    val favoriteFontIds: Set<String> = setOf("sacramento", "cinzel_bold", "playfair_display")
)

sealed interface ExportStatus {
    data object Idle : ExportStatus
    data class Processing(val progress: Float, val message: String, val title: String = "Processing") : ExportStatus
    data class Success(val outputUri: Uri, val isVideo: Boolean, val summary: String, val isAudio: Boolean = false) : ExportStatus
    data class Error(val message: String, val title: String = "Operation Failed") : ExportStatus
}

object StudioCatalog {
    val homeGridTools = listOf(
        StudioToolId.QUALITY_ENHANCER,
        StudioToolId.BG_REMOVER,
        StudioToolId.BATCH_ENHANCE,
        StudioToolId.CROP,
        StudioToolId.ROTATE,
        StudioToolId.TRANSITIONS,
        StudioToolId.SMART_ENHANCE,
        StudioToolId.AI_REMOVAL,
        StudioToolId.FACE,
        StudioToolId.BODY,
        StudioToolId.COLOR_MATCH,
        StudioToolId.ALL_TOOLS
    )

    val editorEditTools = listOf(
        StudioToolId.FILTERS,
        StudioToolId.TRIM,
        StudioToolId.EFFECTS,
        StudioToolId.SPLIT,
        StudioToolId.AI_EDIT,
        StudioToolId.CUTOUT,
        StudioToolId.VIDEO_QUALITY,
        StudioToolId.SPEED,
        StudioToolId.VOLUME,
        StudioToolId.FADE,
        StudioToolId.CROP,
        StudioToolId.ROTATE,
        StudioToolId.POSITION,
        StudioToolId.SIZE,
        StudioToolId.MIRROR,
        StudioToolId.FLIP,
        StudioToolId.FIT,
        StudioToolId.TRANSITIONS,
        StudioToolId.ADJUST,
        StudioToolId.TEXT,
        StudioToolId.AUDIO,
        StudioToolId.EDIT,
        StudioToolId.CUT,
        StudioToolId.ANIMATION,
        StudioToolId.ANIMATE,
        StudioToolId.DELETE,
        StudioToolId.COPY,
        StudioToolId.FREEZE,
        StudioToolId.REPLACE,
        StudioToolId.OVERLAY,
        StudioToolId.CHROMA,
        StudioToolId.MASK,
        StudioToolId.STABILIZE,
        StudioToolId.CAMERA_TRACKING,
        StudioToolId.REMOVAL,
        StudioToolId.QUALITY_ENHANCER,
        StudioToolId.SEPARATE_AUDIO,
        StudioToolId.SOUND,
        StudioToolId.VOCAL_SEPARATION,
        StudioToolId.NOISE_REDUCTION,
        StudioToolId.DETECT_SILENCE,
        StudioToolId.BG_REMOVER,
        StudioToolId.CANVAS,
        StudioToolId.MOSAIC,
        StudioToolId.STICKERS,
        StudioToolId.FRAMES,
        StudioToolId.MAGNIFIER,
        StudioToolId.TEMPLATE
    )

    val editorRetouchTools = listOf(
        StudioToolId.FACE,
        StudioToolId.SKIN_SMOOTH,
        StudioToolId.BRIGHTEN,
        StudioToolId.AI_RETOUCH,
        StudioToolId.BODY,
        StudioToolId.HAIR,
        StudioToolId.MAKEUP,
        StudioToolId.COLOR_MATCH
    )

    val allSearchableTools = listOf(
        StudioToolId.FILTERS,
        StudioToolId.TRIM,
        StudioToolId.EFFECTS,
        StudioToolId.SPLIT,
        StudioToolId.AI_EDIT,
        StudioToolId.CUTOUT,
        StudioToolId.VIDEO_QUALITY,
        StudioToolId.SPEED,
        StudioToolId.VOLUME,
        StudioToolId.FADE,
        StudioToolId.CROP,
        StudioToolId.ROTATE,
        StudioToolId.POSITION,
        StudioToolId.SIZE,
        StudioToolId.MIRROR,
        StudioToolId.FLIP,
        StudioToolId.FIT,
        StudioToolId.QUALITY_ENHANCER,
        StudioToolId.CUT,
        StudioToolId.ANIMATION,
        StudioToolId.ANIMATE,
        StudioToolId.DELETE,
        StudioToolId.COPY,
        StudioToolId.FREEZE,
        StudioToolId.REPLACE,
        StudioToolId.OVERLAY,
        StudioToolId.CHROMA,
        StudioToolId.MASK,
        StudioToolId.ADJUST,
        StudioToolId.STABILIZE,
        StudioToolId.CAMERA_TRACKING,
        StudioToolId.REMOVAL,
        StudioToolId.SEPARATE_AUDIO,
        StudioToolId.SOUND,
        StudioToolId.VOCAL_SEPARATION,
        StudioToolId.NOISE_REDUCTION,
        StudioToolId.DETECT_SILENCE,
        StudioToolId.BG_REMOVER,
        StudioToolId.TRANSITIONS,
        StudioToolId.SMART_ENHANCE,
        StudioToolId.SUPER_RESOLUTION,
        StudioToolId.BATCH_ENHANCE,
        StudioToolId.AI_REMOVAL,
        StudioToolId.FACE,
        StudioToolId.BODY,
        StudioToolId.AI_RETOUCH,
        StudioToolId.COLOR_MATCH,
        StudioToolId.HAIR,
        StudioToolId.MAKEUP,
        StudioToolId.TEXT,
        StudioToolId.MOSAIC,
        StudioToolId.AUDIO,
        StudioToolId.STICKERS,
        StudioToolId.FRAMES,
        StudioToolId.MAGNIFIER,
        StudioToolId.CANVAS
    )

    val studioFonts: List<StudioFontSpec>
        get() = StudioOfflineCatalogs.studioFonts

    val musicListPresets = listOf(
        StudioMusicPreset("m_midnight_lofi", "Midnight Lo-Fi", "Warm chords · 84 BPM", 220f, 84, isBgm = false),
        StudioMusicPreset("m_sunset_synth", "Sunset Synthwave", "Dream pulse · 108 BPM", 261.63f, 108, isBgm = false),
        StudioMusicPreset("m_urban_groove", "Urban Beat", "Crisp rhythm · 96 BPM", 196f, 96, isBgm = false),
        StudioMusicPreset("m_acoustic_gold", "Acoustic Gold", "Gentle pluck · 90 BPM", 293.66f, 90, isBgm = false)
    )

    val bgmPresets = listOf(
        StudioMusicPreset("bgm_velvet_pad", "Velvet Ambient", "Soft studio pad", 174.61f, 72, isBgm = true),
        StudioMusicPreset("bgm_vlog_pop", "Vlog Breeze", "Bright melody loop", 329.63f, 118, isBgm = true),
        StudioMusicPreset("bgm_cinema_rise", "Cinema Horizon", "Wide atmospheric tone", 146.83f, 68, isBgm = true),
        StudioMusicPreset("bgm_neon_chill", "Neon Chill", "Deep late-night loop", 246.94f, 92, isBgm = true)
    )

    val templates: List<StudioTemplate>
        get() = StudioOfflineCatalogs.templates

    val studioStickers: List<StudioStickerSpec>
        get() = StudioOfflineCatalogs.studioStickers

    val stickerChoices: List<String>
        get() = StudioOfflineCatalogs.studioStickers.map { it.content }

    val textColorChoices: List<Color>
        get() = StudioOfflineCatalogs.textColorChoices

    val transitionEffects: List<TransitionEffectSpec>
        get() = StudioOfflineCatalogs.transitionEffects

    val fxEffects: List<FxEffectSpec>
        get() = StudioFilterAndFxCatalog.fxEffects

    val filterPresetsCatalog: List<StudioFilterSpec>
        get() = StudioFilterAndFxCatalog.filterPresets
}
