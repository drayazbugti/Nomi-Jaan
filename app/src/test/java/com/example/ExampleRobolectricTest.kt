package com.example

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.test.core.app.ApplicationProvider
import com.example.media.AudioDSPProcessor
import com.example.media.ImageEnhancerEngine
import com.example.media.WavData
import com.example.model.AspectRatioOption
import com.example.model.EnhanceMode
import com.example.model.FilterPreset
import com.example.model.NormCropRect
import com.example.model.StudioEditState
import com.example.model.TextArtPreset
import com.example.model.TextBoxStylePreset
import com.example.model.TextOverlayItem
import com.example.viewmodel.StudioViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches Bugti Editz`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Bugti Editz", appName)
    }

    @Test
    fun `image enhancer processes ultra hd square crop and freeform crop`() = runTest {
        val source = createBitmap(200, 100, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.rgb(100, 140, 180))
        }
        val state = StudioEditState(
            enhanceMode = EnhanceMode.ULTRA_HD,
            enhanceStrength = 0.8f,
            filterPreset = FilterPreset.GOLDEN_HOUR,
            aspectRatio = AspectRatioOption.SQUARE_1_1
        )
        val output = ImageEnhancerEngine.processBitmap(source, state, isExport = false)
        assertNotNull(output)
        assertEquals(output.width, output.height)
        assertTrue(output.width == 100)

        val freeformState = StudioEditState(
            aspectRatio = AspectRatioOption.FREEFORM,
            freeformCrop = NormCropRect(left = 0.25f, top = 0.25f, right = 0.75f, bottom = 0.75f),
            textOverlays = listOf(
                TextOverlayItem(
                    id = "t1",
                    text = "Bugti Style",
                    fontId = "sacramento",
                    rotationDeg = 15f,
                    artPreset = TextArtPreset.NEON_PURPLE,
                    stylePreset = TextBoxStylePreset.SOLID_PILL
                )
            )
        )
        val freeformOut = ImageEnhancerEngine.processBitmap(
            source,
            freeformState,
            isExport = true,
            context = ApplicationProvider.getApplicationContext()
        )
        assertEquals(100, freeformOut.width)
        assertEquals(50, freeformOut.height)
    }

    @Test
    fun `studio repository persists login state profile name and theme mode`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo1 = com.example.data.StudioRepository(context)
        repo1.setThemeMode(com.example.model.AppThemeMode.WHITE)
        repo1.setAuthSession(
            isLoggedIn = true,
            email = "creator@bugti.app",
            defaultDisplayName = "Bugti Creator"
        )
        repo1.saveProfileName("Bugti Pro")

        val repo2 = com.example.data.StudioRepository(context)
        val loaded = repo2.preferencesFlow.first()
        assertEquals(com.example.model.AppThemeMode.WHITE, loaded.themeMode)
        assertTrue(loaded.isLoggedIn)
        assertEquals("creator@bugti.app", loaded.loggedInEmail)
        assertEquals("Bugti Pro", loaded.profileName)
    }

    @Test
    fun `viewmodel undo and redo revert and reapply edits plus timeline split`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = StudioViewModel(app)

        assertFalse(vm.uiState.value.canUndo)
        assertFalse(vm.uiState.value.canRedo)

        vm.setEnhanceMode(EnhanceMode.ULTRA_HD)
        assertEquals(EnhanceMode.ULTRA_HD, vm.uiState.value.editState.enhanceMode)
        assertTrue(vm.uiState.value.canUndo)

        vm.splitTimelineAtPlayhead(0.5f)
        assertEquals(2, vm.uiState.value.editState.timelineSegments.size)

        vm.undoLastEdit()
        assertEquals(1, vm.uiState.value.editState.timelineSegments.size)
        assertTrue(vm.uiState.value.canRedo)

        vm.redoLastEdit()
        assertEquals(2, vm.uiState.value.editState.timelineSegments.size)
        assertFalse(vm.uiState.value.canRedo)

        vm.deleteSelectedTimelineSegment()
        assertEquals(1, vm.uiState.value.editState.timelineSegments.size)
    }

    @Test
    fun `audio dsp processor applies hall reverb vocal separation noise reduction and wav roundtrip`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sampleRate = 22_050
        val channels = 2
        val totalFrames = sampleRate / 2
        val input = ShortArray(totalFrames * channels) { idx ->
            // Short impulse in first 1000 frames, silence afterwards to verify reverb tail
            val frame = idx / channels
            if (frame in 100..400) 14_000 else 0
        }

        val processed = AudioDSPProcessor.applyDspToPcm(
            samples = input,
            sampleRate = sampleRate,
            channels = channels,
            reverbAmount = 0.75f,
            vocalSeparation = true,
            noiseReduction = true
        )
        assertEquals(input.size, processed.size)

        // Tail region around frame 2000 (was zero before reverb) should contain hall echo energy
        val tailEnergy = (2000 until 4000).sumOf { f -> abs(processed[f * channels].toInt()) }
        assertTrue("Reverb tail should add audible decay energy", tailEnergy > 0)

        // Verify WAV write/read round-trip
        val tempFile = File(context.cacheDir, "test_audio_dsp.wav")
        AudioDSPProcessor.writeWavFile(tempFile, WavData(sampleRate, channels, processed))
        val readBack = AudioDSPProcessor.readWavFile(tempFile)
        assertEquals(sampleRate, readBack.sampleRate)
        assertEquals(channels, readBack.channels)
        assertEquals(processed.size, readBack.samples.size)
        tempFile.delete()
    }

    @Test
    fun `offline catalogs provide 100 stickers 50 font colors 100 transitions and 100 templates`() {
        val stickers = com.example.model.StudioCatalog.studioStickers
        assertEquals(100, stickers.size)
        assertTrue(stickers.any { it.content == "🕌" })
        assertTrue(stickers.any { it.content == "❤️" })
        assertTrue(stickers.any { it.content == "STUDIO" && it.isBadge })

        val colors = com.example.model.StudioCatalog.textColorChoices
        assertEquals(50, colors.size)
        assertEquals(50, colors.distinct().size)

        val transitions = com.example.model.StudioCatalog.transitionEffects
        assertEquals(100, transitions.size)
        assertEquals(100, transitions.map { it.id }.distinct().size)

        val templates = com.example.model.StudioCatalog.templates
        assertEquals(100, templates.size)
        assertEquals(100, templates.map { it.id }.distinct().size)
    }

    @Test
    fun `sticker transform transition state undo redo and sticker export baking work`() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = StudioViewModel(app)

        vm.addStickerOverlay("🕌", isBadge = false)
        vm.addStickerOverlay("VIP", isBadge = true)
        assertEquals(2, vm.uiState.value.editState.stickerOverlays.size)

        val firstId = vm.uiState.value.editState.stickerOverlays.first().id
        vm.transformStickerOverlay(firstId, normX = 0.72f, normY = 0.35f, scale = 1.8f, rotationDeg = 25f)
        val transformed = vm.uiState.value.editState.stickerOverlays.first()
        assertEquals(0.72f, transformed.normX, 0.001f)
        assertEquals(1.8f, transformed.scale, 0.001f)
        assertEquals(25f, transformed.rotationDeg, 0.001f)

        val firstTransitionId = com.example.model.StudioCatalog.transitionEffects.first().id
        vm.setTransitionEffect(firstTransitionId)
        assertEquals(firstTransitionId, vm.uiState.value.editState.transitionEffectId)

        vm.undoLastEdit()
        assertEquals("none", vm.uiState.value.editState.transitionEffectId)

        val src = createBitmap(160, 160, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.DKGRAY)
        }
        val exported = ImageEnhancerEngine.processBitmap(
            src,
            vm.uiState.value.editState,
            isExport = true,
            context = app
        )
        assertEquals(160, exported.width)
        assertEquals(160, exported.height)
    }

    @Test
    fun `multilingual font catalog has 100 distinct fonts including Urdu Hindi English Chinese`() {
        val fonts = com.example.model.StudioCatalog.studioFonts
        assertEquals(100, fonts.size)
        assertEquals(100, fonts.map { it.id }.distinct().size)
        assertTrue(fonts.any { it.language == com.example.model.FontLanguageFilter.URDU })
        assertTrue(fonts.any { it.language == com.example.model.FontLanguageFilter.HINDI })
        assertTrue(fonts.any { it.language == com.example.model.FontLanguageFilter.ENGLISH })
        assertTrue(fonts.any { it.language == com.example.model.FontLanguageFilter.SIMPLIFIED_CHINESE })
        assertTrue(fonts.any { it.language == com.example.model.FontLanguageFilter.MULTI_LANGUAGE })

        val editTools = com.example.model.StudioCatalog.editorEditTools
        assertTrue(editTools.contains(com.example.model.StudioToolId.CROP))
        assertTrue(editTools.contains(com.example.model.StudioToolId.ROTATE))
        assertTrue(editTools.contains(com.example.model.StudioToolId.BG_REMOVER))
        assertTrue(editTools.contains(com.example.model.StudioToolId.TRANSITIONS))
    }

    @Test
    fun `background remover keeps center person intact and clears outer background`() {
        val src = createBitmap(120, 120, Bitmap.Config.ARGB_8888)
        // Outer white background, distinct warm skin/clothing subject centered inside
        for (y in 0 until 120) {
            for (x in 0 until 120) {
                val inCenter = x in 42..78 && y in 30..92
                src.setPixel(x, y, if (inCenter) Color.rgb(185, 95, 70) else Color.rgb(240, 240, 240))
            }
        }
        val mask = com.example.media.BackgroundRemoverEngine.buildSalientPersonMaskFallback(src)
        val cutout = com.example.media.BackgroundRemoverEngine.applyPersonCutout(
            source = src,
            alignedMask = mask,
            bgMode = com.example.model.BackgroundReplaceMode.TRANSPARENT,
            feather = 0.25f
        )

        val cornerAlpha = Color.alpha(cutout.getPixel(4, 4))
        val centerAlpha = Color.alpha(cutout.getPixel(60, 60))
        assertEquals(0, cornerAlpha)
        assertTrue("Center subject alpha should stay opaque", centerAlpha > 220)
    }

    @Test
    fun `viewmodel rotate left 180 straighten and clear background removal record undo`() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = StudioViewModel(app)

        vm.rotateLeft90()
        assertEquals(270, vm.uiState.value.editState.rotationDegrees)

        vm.rotate180()
        assertEquals(90, vm.uiState.value.editState.rotationDegrees)

        vm.setStraightenDegrees(12.5f, recordUndo = true)
        assertEquals(12.5f, vm.uiState.value.editState.straightenDegrees, 0.01f)

        vm.resetRotationAndFlip()
        assertEquals(0, vm.uiState.value.editState.rotationDegrees)
        assertEquals(0f, vm.uiState.value.editState.straightenDegrees, 0.01f)

        vm.undoLastEdit()
        assertEquals(12.5f, vm.uiState.value.editState.straightenDegrees, 0.01f)
    }

    @Test
    fun `all 24 requested edit tools exist in StudioCatalog editorEditTools`() {
        val editTools = com.example.model.StudioCatalog.editorEditTools
        val required = listOf(
            com.example.model.StudioToolId.OVERLAY,
            com.example.model.StudioToolId.ANIMATE,
            com.example.model.StudioToolId.CUTOUT,
            com.example.model.StudioToolId.CHROMA,
            com.example.model.StudioToolId.FREEZE,
            com.example.model.StudioToolId.REPLACE,
            com.example.model.StudioToolId.CAMERA_TRACKING,
            com.example.model.StudioToolId.VOCAL_SEPARATION,
            com.example.model.StudioToolId.NOISE_REDUCTION,
            com.example.model.StudioToolId.DETECT_SILENCE,
            com.example.model.StudioToolId.SEPARATE_AUDIO,
            com.example.model.StudioToolId.SOUND,
            com.example.model.StudioToolId.COPY,
            com.example.model.StudioToolId.TRIM,
            com.example.model.StudioToolId.MASK,
            com.example.model.StudioToolId.ADJUST,
            com.example.model.StudioToolId.STABILIZE,
            com.example.model.StudioToolId.REMOVAL,
            com.example.model.StudioToolId.QUALITY_ENHANCER,
            com.example.model.StudioToolId.CUT,
            com.example.model.StudioToolId.SPEED,
            com.example.model.StudioToolId.ANIMATION,
            com.example.model.StudioToolId.VOLUME,
            com.example.model.StudioToolId.DELETE
        )
        required.forEach { toolId ->
            assertTrue("Missing Edit tool: ${toolId.label}", editTools.contains(toolId))
        }
    }

    @Test
    fun `image enhancer applies circle mask chroma green screen and color balance`() = runTest {
        val greenBackdrop = createBitmap(120, 120, Bitmap.Config.ARGB_8888).apply {
            for (y in 0 until 120) {
                for (x in 0 until 120) {
                    val inSubject = x in 45..75 && y in 45..75
                    setPixel(x, y, if (inSubject) Color.rgb(190, 90, 75) else Color.rgb(0, 200, 83))
                }
            }
        }
        val chromaState = StudioEditState(
            chromaEnabled = true,
            chromaKeyColorHex = 0xFF00C853L,
            bgReplaceMode = com.example.model.BackgroundReplaceMode.TRANSPARENT,
            chromaSimilarity = 0.30f,
            chromaSmoothness = 0.10f
        )
        val chromaOut = ImageEnhancerEngine.processBitmap(greenBackdrop, chromaState, isExport = true)
        assertEquals(0, Color.alpha(chromaOut.getPixel(5, 5)))
        assertTrue(Color.alpha(chromaOut.getPixel(60, 60)) > 200)

        val maskState = StudioEditState(
            maskShape = com.example.model.MaskShapeType.CIRCLE,
            maskScale = 0.45f,
            maskFeather = 0.02f,
            warmth = 0.25f,
            tint = -0.15f
        )
        val maskOut = ImageEnhancerEngine.processBitmap(greenBackdrop, maskState, isExport = true)
        assertTrue("Corner outside circle mask should be transparent", Color.alpha(maskOut.getPixel(2, 2)) < 30)
        assertTrue("Center inside circle mask should stay opaque", Color.alpha(maskOut.getPixel(60, 60)) > 200)
    }

    @Test
    fun `audio dsp adaptive noise reduction lowers static and silence detector finds gaps`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sampleRate = 16_000
        val channels = 1
        val totalFrames = sampleRate // 1.0s
        // Quiet static floor for first 0.5s (~420 amplitude), clear 440Hz voice wave for 0.5s
        val samples = ShortArray(totalFrames) { i ->
            if (i < sampleRate / 2) {
                if (i % 2 == 0) 420 else -420
            } else {
                (kotlin.math.sin(i * 2.0 * Math.PI * 440.0 / sampleRate) * 14_000.0).toInt().toShort()
            }
        }
        val cleaned = AudioDSPProcessor.applyDspToPcm(
            samples = samples,
            sampleRate = sampleRate,
            channels = channels,
            reverbAmount = 0f,
            vocalSeparation = false,
            noiseReduction = true,
            noiseReductionStrength = 0.9f
        )
        val origNoiseEnergy = (1000 until 6000).sumOf { abs(samples[it].toInt()) }
        val cleanNoiseEnergy = (1000 until 6000).sumOf { abs(cleaned[it].toInt()) }
        assertTrue("Noise reduction should suppress quiet static floor", cleanNoiseEnergy < origNoiseEnergy / 2)

        val silenceWav = WavData(sampleRate, channels, cleaned)
        val analysis = AudioDSPProcessor.detectSilentRegions(silenceWav, thresholdDb = -25f, minSilenceMs = 250L)
        assertTrue("Silence detector should detect the 0.5s silent lead-in", analysis.silentCount >= 1)

        val wavFile = File(context.cacheDir, "silence_test.wav")
        AudioDSPProcessor.writeWavFile(wavFile, silenceWav)
        val trimmedRes = AudioDSPProcessor.trimSilentRegionsWav(context, wavFile, thresholdDb = -25f, minSilenceMs = 250L)
        assertTrue(trimmedRes.isSuccess)
        val (trimmedFile, _) = trimmedRes.getOrThrow()
        val trimmedWav = AudioDSPProcessor.readWavFile(trimmedFile)
        assertTrue("Trimmed WAV should be shorter after removing silent gap", trimmedWav.samples.size < samples.size)
        wavFile.delete()
        trimmedFile.delete()
    }

    @Test
    fun `viewmodel speed popup cut copy color balance and delete work smoothly`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = StudioViewModel(app)

        vm.selectEditorTool(com.example.model.StudioToolId.SPEED)
        assertTrue(vm.uiState.value.showSpeedPopup)
        vm.setPlaybackSpeed(2.25f, recordUndo = true)
        assertEquals(2.25f, vm.uiState.value.editState.playbackSpeed, 0.01f)
        vm.dismissSpeedPopup()
        assertFalse(vm.uiState.value.showSpeedPopup)

        vm.duplicateSelectedTimelineSegment()
        assertEquals(2, vm.uiState.value.editState.timelineSegments.size)

        vm.updateEditState { it.copy(warmth = 0.3f, tint = -0.2f) }
        assertEquals(0.3f, vm.uiState.value.editState.warmth, 0.01f)
        assertEquals(-0.2f, vm.uiState.value.editState.tint, 0.01f)

        vm.deleteActiveTarget()
        assertEquals(1, vm.uiState.value.editState.timelineSegments.size)
    }

    @Test
    fun `bottom toolbar leading order and fx and 18 filter categories are complete`() {
        val editTools = com.example.model.StudioCatalog.editorEditTools
        val expectedLead = listOf(
            com.example.model.StudioToolId.FILTERS,
            com.example.model.StudioToolId.TRIM,
            com.example.model.StudioToolId.EFFECTS,
            com.example.model.StudioToolId.SPLIT,
            com.example.model.StudioToolId.AI_EDIT,
            com.example.model.StudioToolId.CUTOUT,
            com.example.model.StudioToolId.VIDEO_QUALITY,
            com.example.model.StudioToolId.SPEED,
            com.example.model.StudioToolId.VOLUME,
            com.example.model.StudioToolId.FADE,
            com.example.model.StudioToolId.CROP,
            com.example.model.StudioToolId.ROTATE,
            com.example.model.StudioToolId.POSITION,
            com.example.model.StudioToolId.SIZE,
            com.example.model.StudioToolId.MIRROR,
            com.example.model.StudioToolId.FLIP,
            com.example.model.StudioToolId.FIT
        )
        assertEquals(expectedLead, editTools.take(expectedLead.size))

        val filterCats = com.example.model.StudioCatalog.filterPresetsCatalog.map { it.category }.toSet()
        assertEquals(18, com.example.model.FilterCategory.entries.size)
        assertEquals(18, filterCats.size)

        val fxNames = com.example.model.StudioCatalog.fxEffects.map { it.name }.toSet()
        listOf("None", "Spin 01", "Spin 02", "Spin 03", "Spin 04", "Drop 01", "Drop 02", "Drop 03", "Drop 04").forEach {
            assertTrue("Missing FX card: $it", fxNames.contains(it))
        }
        assertTrue(com.example.model.StudioCatalog.fxEffects.any { it.tab == com.example.model.FxTab.BASIC })
        assertTrue(com.example.model.StudioCatalog.fxEffects.any { it.tab == com.example.model.FxTab.BODY })
    }

    @Test
    fun `timeline junction transitions apply per cut and across all cuts`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = StudioViewModel(app)

        vm.splitTimelineAtPlayhead(0.35f)
        vm.splitTimelineAtPlayhead(0.70f)
        assertEquals(3, vm.uiState.value.editState.timelineSegments.size)

        vm.openTransitionForJunction(0)
        assertEquals(0, vm.uiState.value.selectedTransitionJunctionIndex)
        assertEquals(com.example.model.StudioToolId.TRANSITIONS, vm.uiState.value.selectedEditorTool)

        val pickedTransition = com.example.model.StudioCatalog.transitionEffects.first().id
        vm.setTransitionEffect(pickedTransition)
        assertEquals(pickedTransition, vm.uiState.value.editState.timelineSegments[0].transitionAfterId)
        assertEquals("none", vm.uiState.value.editState.timelineSegments[1].transitionAfterId)

        val secondTransition = com.example.model.StudioCatalog.transitionEffects.last().id
        vm.applyTransitionToAllJunctions(secondTransition, 900)
        assertEquals(secondTransition, vm.uiState.value.editState.timelineSegments[0].transitionAfterId)
        assertEquals(900, vm.uiState.value.editState.timelineSegments[0].transitionAfterDurationMs)
        assertEquals(secondTransition, vm.uiState.value.editState.timelineSegments[1].transitionAfterId)
    }

    @Test
    fun `speed curves volume 300 transform fit fade and cancel snapshot work cleanly`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = StudioViewModel(app)

        // Curve speed presets + custom curve point
        vm.setSpeedCurvePreset(com.example.model.SpeedCurvePreset.HERO_TIME)
        assertEquals(com.example.model.SpeedModeTab.CURVE, vm.uiState.value.editState.speedModeTab)
        assertEquals(com.example.model.SpeedCurvePreset.HERO_TIME, vm.uiState.value.editState.speedCurvePreset)
        vm.updateCustomSpeedCurvePoint(2, 0.35f)
        assertEquals(com.example.model.SpeedCurvePreset.CUSTOM, vm.uiState.value.editState.speedCurvePreset)
        assertEquals(0.35f, vm.uiState.value.editState.customSpeedCurvePoints[2], 0.01f)

        // Volume up to 300%
        vm.updateEditState { it.copy(audioVolume = 2.85f, muteAudio = false) }
        assertEquals(2.85f, vm.uiState.value.editState.audioVolume, 0.01f)

        // Mirror, Flip, Position, Size, Fit, Rotate ruler (-58°..+180°), Fade
        vm.selectEditorTool(com.example.model.StudioToolId.MIRROR)
        vm.selectEditorTool(com.example.model.StudioToolId.FLIP)
        assertTrue(vm.uiState.value.editState.flipHorizontal)
        assertTrue(vm.uiState.value.editState.flipVertical)

        val beforeCancelSnapshot = vm.uiState.value.editState
        vm.setClipPosition(0.25f, -0.30f, recordUndo = false)
        vm.setClipSize(1.4f, 0.8f, recordUndo = false)
        vm.setCanvasFitMode(com.example.model.CanvasFitMode.FILL)
        vm.setStraightenDegrees(-58f, recordUndo = false)
        vm.setVideoFade(fadeInMs = 1200, fadeOutMs = 800, recordUndo = false)
        assertEquals(-58f, vm.uiState.value.editState.straightenDegrees, 0.01f)
        assertEquals(1200, vm.uiState.value.editState.videoFadeInMs)

        // Cancel restores entry snapshot cleanly
        vm.restoreEditSnapshot(beforeCancelSnapshot)
        assertEquals(0f, vm.uiState.value.editState.clipOffsetX, 0.01f)
        assertEquals(1f, vm.uiState.value.editState.clipScaleX, 0.01f)
        assertEquals(0f, vm.uiState.value.editState.straightenDegrees, 0.01f)
        assertEquals(0, vm.uiState.value.editState.videoFadeInMs)

        // Confirm pushes undo snapshot
        val preConfirm = vm.uiState.value.editState
        vm.setStraightenDegrees(21f, recordUndo = false)
        vm.commitToolChanges(preConfirm)
        assertEquals(21f, vm.uiState.value.editState.straightenDegrees, 0.01f)
        vm.undoLastEdit()
        assertEquals(0f, vm.uiState.value.editState.straightenDegrees, 0.01f)
    }

    @Test
    fun `image enhancer applies vertical flip size scale filter swatch and fx effect`() = runTest {
        val src = createBitmap(80, 80, Bitmap.Config.ARGB_8888).apply {
            for (y in 0 until 80) {
                for (x in 0 until 80) {
                    setPixel(x, y, if (y < 20) Color.rgb(220, 40, 40) else Color.rgb(30, 80, 200))
                }
            }
        }
        val state = StudioEditState(
            flipVertical = true,
            clipScaleX = 1.25f,
            clipScaleY = 0.8f,
            filterSpecId = "viv_neon",
            filterPreset = FilterPreset.CYBER_NEON,
            fxEffectId = "fx_rgb_split",
            fxIntensity = 0.7f
        )
        val out = ImageEnhancerEngine.processBitmap(src, state, isExport = true)
        assertNotNull(out)
        assertEquals(100, out.width)
        assertEquals(64, out.height)
        // Top strip was red, after vertical flip bottom pixels should be predominantly warm/reddish vs top cool blue
        val bottomRed = Color.red(out.getPixel(50, out.height - 6))
        val topRed = Color.red(out.getPixel(50, 6))
        assertTrue("Vertical flip should move red band toward bottom", bottomRed > topRed)
    }
}

