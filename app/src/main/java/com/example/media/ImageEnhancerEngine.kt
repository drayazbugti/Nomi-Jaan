package com.example.media

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.BlurMaskFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import com.example.model.AspectRatioOption
import com.example.model.BackgroundReplaceMode
import com.example.model.EffectMode
import com.example.model.EnhanceMode
import com.example.model.FilterPreset
import com.example.model.FrameStyle
import com.example.model.FxTab
import com.example.model.MaskShapeType
import com.example.model.MediaSource
import com.example.model.OverlayMode
import com.example.model.StabilizeLevel
import com.example.model.StudioCatalog
import com.example.model.StudioEditState
import com.example.model.StudioFilterSpec
import com.example.model.TextArtPreset
import com.example.model.TextBoxStylePreset
import com.example.model.TrackingTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

object ImageEnhancerEngine {

    suspend fun loadSourceBitmap(
        context: Context,
        source: MediaSource,
        maxDimension: Int = 1440
    ): Bitmap? = withContext(Dispatchers.IO) {
        when (source) {
            is MediaSource.SampleDrawable -> {
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeResource(context.resources, source.resId, options)
                val decodeOpts = BitmapFactory.Options().apply {
                    inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, maxDimension)
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val raw = BitmapFactory.decodeResource(context.resources, source.resId, decodeOpts)
                    ?: return@withContext null
                scaleToMax(raw, maxDimension)
            }
            is MediaSource.ContentUri -> {
                if (source.isVideo) return@withContext null
                val uri = source.uri
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, bounds)
                }
                val decodeOpts = BitmapFactory.Options().apply {
                    inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val raw = context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, decodeOpts)
                } ?: return@withContext null

                val oriented = applyExifOrientation(context, uri, raw)
                scaleToMax(oriented, maxDimension)
            }
        }
    }

    suspend fun processBitmap(
        source: Bitmap,
        editState: StudioEditState,
        isExport: Boolean = false,
        context: Context? = null,
        personMask: Bitmap? = null
    ): Bitmap = withContext(Dispatchers.Default) {
        var working = applyGeometryAndCrop(source, editState)

        // Apply Background Removal (Cutout / Keep Person) if active
        if (editState.removeBackground) {
            val maskToUse = personMask ?: BackgroundRemoverEngine.extractPersonMask(source)
            val alignedMask = applyGeometryAndCrop(maskToUse, editState)
            working = BackgroundRemoverEngine.applyPersonCutout(
                source = working,
                alignedMask = alignedMask,
                bgMode = editState.bgReplaceMode,
                feather = editState.bgEdgeFeather
            )
            if (alignedMask != maskToUse) alignedMask.recycle()
            if (personMask == null && maskToUse != working) maskToUse.recycle()
        }

        // Apply Chroma Key (Green / Blue / Custom color screen removal) if active
        if (editState.chromaEnabled) {
            working = applyChromaKey(working, editState)
        }

        // Optional upscale during export for Ultra HD / AI UHD / 4K
        if (isExport && (editState.enhanceMode == EnhanceMode.ULTRA_HD ||
                editState.enhanceMode == EnhanceMode.AI_UHD ||
                editState.enhanceMode == EnhanceMode.ANIME_4K ||
                editState.exportResolution.maxDim >= 3840)
        ) {
            val targetMax = editState.exportResolution.maxDim
            val currentMax = max(working.width, working.height)
            if (currentMax < targetMax) {
                val scale = min(2.0f, targetMax.toFloat() / currentMax.toFloat())
                if (scale > 1.05f) {
                    val upscaled = Bitmap.createScaledBitmap(
                        working,
                        (working.width * scale).roundToInt(),
                        (working.height * scale).roundToInt(),
                        true
                    )
                    if (upscaled != working) {
                        working = upscaled
                    }
                }
            }
        }

        // 1. Skin Smooth / Soft Focus pass (soft blend)
        val totalSmooth = max(
            editState.skinSmooth,
            if (editState.enhanceMode == EnhanceMode.PORTRAIT) 0.25f * editState.enhanceStrength else 0f
        ).coerceIn(0f, 1f)
        if (totalSmooth > 0.02f) {
            working = applySoftSkinBlend(working, totalSmooth * 0.55f)
        }

        // 2. ColorMatrix pass: Enhance tonal boost + Filter Preset + Adjust (inc. Color Balance Warmth/Tint)
        working = applyColorGrading(working, editState)

        // 3. Sharpening / Clarity convolution pass
        val enhanceSharpen = when (editState.enhanceMode) {
            EnhanceMode.NONE -> 0f
            EnhanceMode.HD -> 0.42f * editState.enhanceStrength
            EnhanceMode.ULTRA_HD -> 0.68f * editState.enhanceStrength
            EnhanceMode.PORTRAIT -> 0.35f * editState.enhanceStrength
            EnhanceMode.AI_UHD -> 0.85f * editState.enhanceStrength
            EnhanceMode.NIGHT_DENOISE -> 0.45f * editState.enhanceStrength
            EnhanceMode.ANIME_4K -> 0.80f * editState.enhanceStrength
            EnhanceMode.OLD_PHOTO -> 0.60f * editState.enhanceStrength
            EnhanceMode.MOTION_FIX -> 0.90f * editState.enhanceStrength
        }
        val totalSharpen = (enhanceSharpen + editState.sharpen).coerceIn(0f, 1.45f)
        if (totalSharpen > 0.04f) {
            working = applyUnsharpSharpen(working, totalSharpen)
        }

        // 4. Touch Removal (soft radial patch blur) & Mosaic Pixelation
        if (editState.removalPoints.isNotEmpty() || editState.mosaicPoints.isNotEmpty()) {
            working = applyMosaicAndRemoval(working, editState)
        }

        // 5. Effects, Overlays (incl. PIP Image Overlay), Vignette, Frames
        working = applyEffectsOverlaysAndFrames(working, editState, context)

        // 6. Shape / Linear / Circle Masking
        if (editState.maskShape != MaskShapeType.NONE) {
            working = applyShapeMask(working, editState)
        }

        // 7. Bake Text & Sticker Overlays only on export (live editor renders interactive Compose overlays)
        if (isExport && (editState.textOverlays.isNotEmpty() || editState.stickerOverlays.isNotEmpty())) {
            working = bakeCreativeOverlays(working, editState, context)
        }

        working
    }

    suspend fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        displayName: String,
        preserveAlpha: Boolean = false
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val ext = if (preserveAlpha) "png" else "jpg"
            val mimeType = if (preserveAlpha) "image/png" else "image/jpeg"
            val format = if (preserveAlpha) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
            val quality = if (preserveAlpha) 100 else 96
            val fileName = "${displayName}_${System.currentTimeMillis()}.$ext"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Bugti Editz")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: error("Failed to create MediaStore image entry")
                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(format, quality, out)
                } ?: error("Failed to open output stream")
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                uri
            } else {
                val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    ?: context.filesDir
                val outDir = File(picturesDir, "Bugti Editz").apply { mkdirs() }
                val outFile = File(outDir, fileName)
                FileOutputStream(outFile).use { out ->
                    bitmap.compress(format, quality, out)
                }
                Uri.fromFile(outFile)
            }
        }
    }

    private fun applyGeometryAndCrop(src: Bitmap, state: StudioEditState): Bitmap {
        var working = src

        // Apply deterministic horizontal/vertical flip on pixel buffer
        if (state.flipHorizontal || state.flipVertical) {
            val w = working.width
            val h = working.height
            val srcPixels = IntArray(w * h)
            val outPixels = IntArray(w * h)
            working.getPixels(srcPixels, 0, w, 0, 0, w, h)
            val fh = state.flipHorizontal
            val fv = state.flipVertical
            for (y in 0 until h) {
                val dstY = if (fv) (h - 1 - y) else y
                val srcRow = y * w
                val dstRow = dstY * w
                if (!fh) {
                    System.arraycopy(srcPixels, srcRow, outPixels, dstRow, w)
                } else {
                    for (x in 0 until w) {
                        outPixels[dstRow + (w - 1 - x)] = srcPixels[srcRow + x]
                    }
                }
            }
            working = createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
                setPixels(outPixels, 0, w, 0, 0, w, h)
            }
        }

        // Apply rotation & straighten via Matrix
        val totalRotation = state.rotationDegrees.toFloat() + state.straightenDegrees
        if (totalRotation != 0f) {
            val rotMatrix = Matrix().apply { postRotate(totalRotation) }
            working = Bitmap.createBitmap(working, 0, 0, working.width, working.height, rotMatrix, true)
        }

        // Apply Size (clipScaleX/Y) & Body proportion scale
        val customScaleX = state.clipScaleX.coerceIn(0.35f, 2.5f)
        val customScaleY = state.clipScaleY.coerceIn(0.35f, 2.5f)
        val bodyScaleX = if (state.bodyBalance != 0f) {
            (1f - state.bodyBalance * 0.12f).coerceIn(0.85f, 1.15f)
        } else 1f
        val finalScaleX = customScaleX * bodyScaleX
        if (abs(finalScaleX - 1f) > 0.02f || abs(customScaleY - 1f) > 0.02f) {
            val targetW = (working.width * finalScaleX).roundToInt().coerceAtLeast(8)
            val targetH = (working.height * customScaleY).roundToInt().coerceAtLeast(8)
            working = Bitmap.createScaledBitmap(working, targetW, targetH, true)
        }

        // Apply Freeform Crop rectangle if modified
        if (!state.freeformCrop.isFull) {
            val l = (state.freeformCrop.left.coerceIn(0f, 0.85f) * working.width).roundToInt()
            val t = (state.freeformCrop.top.coerceIn(0f, 0.85f) * working.height).roundToInt()
            val r = (state.freeformCrop.right.coerceIn(state.freeformCrop.left + 0.1f, 1f) * working.width).roundToInt()
            val b = (state.freeformCrop.bottom.coerceIn(state.freeformCrop.top + 0.1f, 1f) * working.height).roundToInt()
            val cw = (r - l).coerceIn(1, working.width - l)
            val ch = (b - t).coerceIn(1, working.height - t)
            if (cw > 2 && ch > 2) {
                working = Bitmap.createBitmap(working, l, t, cw, ch)
            }
        }

        // Stabilize safety margin crop (eliminates edge shake/black borders)
        if (state.stabilizeLevel != StabilizeLevel.OFF) {
            val m = state.stabilizeLevel.cropMargin.coerceIn(0f, 0.22f)
            val l = (working.width * m).roundToInt()
            val t = (working.height * m).roundToInt()
            val cw = (working.width - l * 2).coerceAtLeast(8)
            val ch = (working.height - t * 2).coerceAtLeast(8)
            if (cw < working.width && ch < working.height) {
                working = Bitmap.createBitmap(working, l, t, cw, ch)
            }
        }

        // Camera Tracking focus lock crop around tracked subject/anchor
        if (state.cameraTrackingEnabled && state.cameraTrackingStrength > 0.05f) {
            val zoomFrac = (1f - 0.22f * state.cameraTrackingStrength).coerceIn(0.72f, 0.98f)
            val cropW = (working.width * zoomFrac).roundToInt().coerceAtLeast(8)
            val cropH = (working.height * zoomFrac).roundToInt().coerceAtLeast(8)
            val ax = when (state.cameraTrackingTarget) {
                TrackingTarget.FACE -> 0.5f
                TrackingTarget.SUBJECT -> 0.5f
                TrackingTarget.MOTION_POINT -> state.trackingAnchorX.coerceIn(0.1f, 0.9f)
            }
            val ay = when (state.cameraTrackingTarget) {
                TrackingTarget.FACE -> 0.36f
                TrackingTarget.SUBJECT -> 0.5f
                TrackingTarget.MOTION_POINT -> state.trackingAnchorY.coerceIn(0.1f, 0.9f)
            }
            val left = (ax * working.width - cropW / 2f).roundToInt().coerceIn(0, working.width - cropW)
            val top = (ay * working.height - cropH / 2f).roundToInt().coerceAtLeast(0).coerceAtMost(working.height - cropH)
            working = Bitmap.createBitmap(working, left, top, cropW, cropH)
        }

        val targetRatio = state.aspectRatio.ratio ?: return working
        val curRatio = working.width.toFloat() / working.height.toFloat()
        if (kotlin.math.abs(curRatio - targetRatio) < 0.01f) return working

        val cropW: Int
        val cropH: Int
        if (curRatio > targetRatio) {
            cropH = working.height
            cropW = (cropH * targetRatio).roundToInt().coerceIn(1, working.width)
        } else {
            cropW = working.width
            cropH = (cropW / targetRatio).roundToInt().coerceIn(1, working.height)
        }
        val left = ((working.width - cropW) / 2).coerceAtLeast(0)
        val top = ((working.height - cropH) / 2).coerceAtLeast(0)
        return Bitmap.createBitmap(working, left, top, cropW, cropH)
    }

    private fun applyColorGrading(src: Bitmap, state: StudioEditState): Bitmap {
        val cm = ColorMatrix()

        // Enhance mode baseline contrast/saturation/brightness
        val enhStr = state.enhanceStrength
        val enhContrast = when (state.enhanceMode) {
            EnhanceMode.NONE -> 0f
            EnhanceMode.HD -> 0.10f * enhStr
            EnhanceMode.ULTRA_HD -> 0.16f * enhStr
            EnhanceMode.PORTRAIT -> 0.08f * enhStr
            EnhanceMode.AI_UHD -> 0.22f * enhStr
            EnhanceMode.NIGHT_DENOISE -> 0.14f * enhStr
            EnhanceMode.ANIME_4K -> 0.20f * enhStr
            EnhanceMode.OLD_PHOTO -> 0.26f * enhStr
            EnhanceMode.MOTION_FIX -> 0.14f * enhStr
        }
        val enhSat = when (state.enhanceMode) {
            EnhanceMode.NONE -> 0f
            EnhanceMode.HD -> 0.08f * enhStr
            EnhanceMode.ULTRA_HD -> 0.15f * enhStr
            EnhanceMode.PORTRAIT -> 0.06f * enhStr
            EnhanceMode.AI_UHD -> 0.18f * enhStr
            EnhanceMode.NIGHT_DENOISE -> 0.10f * enhStr
            EnhanceMode.ANIME_4K -> 0.28f * enhStr
            EnhanceMode.OLD_PHOTO -> 0.18f * enhStr
            EnhanceMode.MOTION_FIX -> 0.08f * enhStr
        }
        val enhBright = when (state.enhanceMode) {
            EnhanceMode.NIGHT_DENOISE -> 0.14f * enhStr
            EnhanceMode.PORTRAIT -> 0.05f * enhStr
            else -> 0f
        }

        val filterSpec: StudioFilterSpec? = StudioCatalog.filterPresetsCatalog.firstOrNull {
            it.id == state.filterSpecId
        }
        val fi = if (state.filterPreset != FilterPreset.NATURAL || (filterSpec != null && filterSpec.id != "natural")) {
            state.filterIntensity.coerceIn(0f, 1f)
        } else 0f

        // Total brightness (-128..128), contrast (0.5..1.8), saturation (0..2)
        val specBright = (filterSpec?.brightBoost ?: 0f) * fi
        val specContrast = (filterSpec?.contrastBoost ?: 0f) * fi
        val specSat = (filterSpec?.satBoost ?: 0f) * fi
        val specWarmth = (filterSpec?.warmthBoost ?: 0f) * fi
        val specTint = (filterSpec?.tintBoost ?: 0f) * fi

        val totalBrightness = (state.brightness + specBright + enhBright + state.faceGlow * 0.12f) * 255f * 0.5f
        val totalContrast = (1f + state.contrast + specContrast + enhContrast + state.hairGloss * 0.15f).coerceIn(0.4f, 2.0f)
        val totalSat = (1f + state.saturation + specSat + enhSat + state.makeupTint * 0.22f).coerceIn(0f, 2.2f)

        val satMatrix = ColorMatrix().apply { setSaturation(totalSat) }
        cm.postConcat(satMatrix)

        val scale = totalContrast
        val translate = (-0.5f * scale + 0.5f) * 255f + totalBrightness
        val effectiveWarmth = state.warmth + specWarmth
        val effectiveTint = state.tint + specTint
        val warmthRed = effectiveWarmth * 36f + effectiveTint * 18f + state.makeupTint * 12f
        val warmthGreen = -effectiveTint * 34f
        val warmthBlue = -effectiveWarmth * 36f + effectiveTint * 18f
        val adjustMatrix = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate + warmthRed,
                0f, scale, 0f, 0f, translate + warmthGreen,
                0f, 0f, scale, 0f, translate + warmthBlue,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(adjustMatrix)

        // Filter Preset Color Matrix blended by filterIntensity
        if (state.filterPreset != FilterPreset.NATURAL && state.filterIntensity > 0.01f) {
            val presetMatrix = buildFilterPresetMatrix(state.filterPreset, state.filterIntensity)
            cm.postConcat(presetMatrix)
        }

        val out = createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(cm)
        }
        canvas.drawBitmap(src, 0f, 0f, paint)
        return out
    }

    private fun buildFilterPresetMatrix(preset: FilterPreset, intensity: Float): ColorMatrix {
        val t = intensity.coerceIn(0f, 1f)
        val target = when (preset) {
            FilterPreset.NATURAL -> floatArrayOf(
                1f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterPreset.TOKYO_NIGHT -> floatArrayOf(
                0.92f, 0f, 0.08f, 0f, -8f,
                0f, 1.04f, 0.08f, 0f, 2f,
                0.05f, 0.10f, 1.18f, 0f, 12f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterPreset.GOLDEN_HOUR -> floatArrayOf(
                1.16f, 0.05f, 0f, 0f, 14f,
                0.04f, 1.04f, 0f, 0f, 6f,
                0f, 0f, 0.84f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterPreset.TEAL_ORANGE -> floatArrayOf(
                1.18f, 0.04f, -0.08f, 0f, 10f,
                0f, 1.02f, 0.04f, 0f, 0f,
                -0.08f, 0.12f, 1.12f, 0f, 6f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterPreset.CRISP_BW -> floatArrayOf(
                0.33f, 0.59f, 0.08f, 0f, 0f,
                0.33f, 0.59f, 0.08f, 0f, 0f,
                0.33f, 0.59f, 0.08f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterPreset.VELVET_MATTE -> floatArrayOf(
                0.96f, 0.04f, 0.02f, 0f, 14f,
                0.02f, 0.94f, 0.04f, 0f, 10f,
                0.04f, 0.02f, 0.98f, 0f, 16f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterPreset.CYBER_NEON -> floatArrayOf(
                1.12f, 0f, 0.14f, 0f, 6f,
                0f, 0.96f, 0.10f, 0f, -4f,
                0.16f, 0.06f, 1.20f, 0f, 14f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterPreset.FUJI_WARM -> floatArrayOf(
                1.08f, 0.04f, 0f, 0f, 8f,
                0.02f, 1.06f, 0.02f, 0f, 4f,
                0f, 0.04f, 0.92f, 0f, 2f,
                0f, 0f, 0f, 1f, 0f
            )
        }
        val identity = floatArrayOf(
            1f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
        val blended = FloatArray(20) { i ->
            identity[i] + (target[i] - identity[i]) * t
        }
        return ColorMatrix(blended)
    }

    /**
     * Fast box-downsample + upsample soft blend for skin smoothing / soft focus.
     */
    private fun applySoftSkinBlend(src: Bitmap, alphaFraction: Float): Bitmap {
        val w = src.width
        val h = src.height
        val downW = max(8, w / 6)
        val downH = max(8, h / 6)
        val tiny = Bitmap.createScaledBitmap(src, downW, downH, true)
        val blurred = Bitmap.createScaledBitmap(tiny, w, h, true)
        tiny.recycle()

        val out = src.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            alpha = (alphaFraction.coerceIn(0f, 1f) * 255f).roundToInt()
        }
        canvas.drawBitmap(blurred, 0f, 0f, paint)
        blurred.recycle()
        return out
    }

    /**
     * Unsharp-mask sharpening: enhanced = original + strength * (original - blurred)
     * Runs cleanly in Kotlin integer math on ARGB pixels for responsive studio clarity.
     */
    private fun applyUnsharpSharpen(src: Bitmap, amount: Float): Bitmap {
        val w = src.width
        val h = src.height
        if (w < 4 || h < 4) return src

        val blurDownW = max(4, (w * 0.5f).roundToInt())
        val blurDownH = max(4, (h * 0.5f).roundToInt())
        val small = Bitmap.createScaledBitmap(src, blurDownW, blurDownH, true)
        val blurred = Bitmap.createScaledBitmap(small, w, h, true)
        small.recycle()

        val total = w * h
        val origPixels = IntArray(total)
        val blurPixels = IntArray(total)
        src.getPixels(origPixels, 0, w, 0, 0, w, h)
        blurred.getPixels(blurPixels, 0, w, 0, 0, w, h)
        blurred.recycle()

        val strength = amount.coerceIn(0f, 1.5f) * 1.15f
        val outPixels = IntArray(total)

        for (i in 0 until total) {
            val o = origPixels[i]
            val b = blurPixels[i]
            val a = (o ushr 24) and 0xFF
            val or = (o ushr 16) and 0xFF
            val og = (o ushr 8) and 0xFF
            val ob = o and 0xFF

            val br = (b ushr 16) and 0xFF
            val bg = (b ushr 8) and 0xFF
            val bb = b and 0xFF

            val r = (or + ((or - br) * strength)).roundToInt().coerceIn(0, 255)
            val g = (og + ((og - bg) * strength)).roundToInt().coerceIn(0, 255)
            val bl = (ob + ((ob - bb) * strength)).roundToInt().coerceIn(0, 255)

            outPixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or bl
        }

        return createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            setPixels(outPixels, 0, w, 0, 0, w, h)
        }
    }

    private fun applyMosaicAndRemoval(src: Bitmap, state: StudioEditState): Bitmap {
        val out = src.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(out)
        val w = out.width.toFloat()
        val h = out.height.toFloat()

        // Pixelated Mosaic blocks
        if (state.mosaicPoints.isNotEmpty()) {
            val tiny = Bitmap.createScaledBitmap(src, max(6, src.width / 24), max(6, src.height / 24), false)
            val pixelated = Bitmap.createScaledBitmap(tiny, src.width, src.height, false)
            tiny.recycle()
            val radius = max(18f, min(w, h) * 0.055f)
            val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            state.mosaicPoints.forEach { pt ->
                val cx = pt.x * w
                val cy = pt.y * h
                canvas.save()
                val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
                val path = android.graphics.Path().apply { addOval(rect, android.graphics.Path.Direction.CW) }
                canvas.clipPath(path)
                canvas.drawBitmap(pixelated, 0f, 0f, clipPaint)
                canvas.restore()
            }
            pixelated.recycle()
        }

        // Soft Removal Patch Heal
        if (state.removalPoints.isNotEmpty()) {
            val tiny = Bitmap.createScaledBitmap(src, max(6, src.width / 10), max(6, src.height / 10), true)
            val healed = Bitmap.createScaledBitmap(tiny, src.width, src.height, true)
            tiny.recycle()
            val radius = max(16f, min(w, h) * state.removalBrushSize.coerceIn(0.025f, 0.14f))
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            state.removalPoints.forEach { pt ->
                val cx = pt.x * w
                val cy = pt.y * h
                canvas.save()
                val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
                val path = Path().apply { addOval(rect, Path.Direction.CW) }
                canvas.clipPath(path)
                canvas.drawBitmap(healed, 0f, 0f, paint)
                canvas.restore()
            }
            healed.recycle()
        }
        return out
    }

    /**
     * Real Chroma Keyer: removes target green/blue/custom screen color with smooth edge spill feathering.
     */
    fun applyChromaKey(src: Bitmap, state: StudioEditState): Bitmap {
        val w = src.width
        val h = src.height
        val total = w * h
        val pixels = IntArray(total)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        val keyColor = state.chromaKeyColorHex.toInt()
        val kr = (keyColor ushr 16) and 0xFF
        val kg = (keyColor ushr 8) and 0xFF
        val kb = keyColor and 0xFF

        val maxDist = (state.chromaSimilarity.coerceIn(0.05f, 0.85f) * 441.67f)
        val featherSpan = (state.chromaSmoothness.coerceIn(0.01f, 0.5f) * 160f)

        val outPixels = IntArray(total)
        for (i in 0 until total) {
            val p = pixels[i]
            val a = (p ushr 24) and 0xFF
            val r = (p ushr 16) and 0xFF
            val g = (p ushr 8) and 0xFF
            val b = p and 0xFF

            val dr = (r - kr).toFloat()
            val dg = (g - kg).toFloat()
            val db = (b - kb).toFloat()
            val dist = sqrt(dr * dr + dg * dg + db * db)

            val maskFrac = when {
                dist <= maxDist -> 0f
                dist >= maxDist + featherSpan -> 1f
                else -> (dist - maxDist) / featherSpan
            }
            val newA = (a * maskFrac).roundToInt().coerceIn(0, 255)
            outPixels[i] = (newA shl 24) or (r shl 16) or (g shl 8) or b
        }

        val keyed = createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            setPixels(outPixels, 0, w, 0, 0, w, h)
        }
        if (state.bgReplaceMode == BackgroundReplaceMode.TRANSPARENT) {
            return keyed
        }
        return BackgroundRemoverEngine.applyPersonCutout(
            source = keyed,
            alignedMask = keyed,
            bgMode = state.bgReplaceMode,
            feather = state.bgEdgeFeather
        )
    }

    /**
     * Interactive Shape / Linear / Circle / Heart / Star / Mirror Masking with Feather & Invert.
     */
    fun applyShapeMask(src: Bitmap, state: StudioEditState): Bitmap {
        if (state.maskShape == MaskShapeType.NONE) return src
        val w = src.width
        val h = src.height
        val total = w * h
        val srcPixels = IntArray(total)
        src.getPixels(srcPixels, 0, w, 0, 0, w, h)

        val cx = state.maskCenterX.coerceIn(0.05f, 0.95f) * w
        val cy = state.maskCenterY.coerceIn(0.05f, 0.95f) * h
        val minDim = min(w, h).toFloat()
        val halfSize = (minDim * 0.5f * state.maskScale.coerceIn(0.15f, 1.35f)).coerceAtLeast(12f)
        val featherPx = max(1.5f, minDim * 0.18f * state.maskFeather.coerceIn(0f, 1f))
        val rad = -Math.toRadians(state.maskRotation.toDouble())
        val cosR = cos(rad).toFloat()
        val sinR = sin(rad).toFloat()
        val invert = state.maskInvert

        val sinPi5 = sin(Math.PI / 5.0).toFloat()
        val cosPi5 = cos(Math.PI / 5.0).toFloat()
        val sectorStep = (2.0 * Math.PI / 5.0).toFloat()
        val innerStarR = halfSize * 0.44f

        val outPixels = IntArray(total)
        var idx = 0
        for (y in 0 until h) {
            val py = y - cy
            for (x in 0 until w) {
                val px = x - cx
                val dx = cosR * px - sinR * py
                val dy = sinR * px + cosR * py

                val sd: Float = when (state.maskShape) {
                    MaskShapeType.NONE -> -featherPx
                    MaskShapeType.CIRCLE -> sqrt(dx * dx + dy * dy) - halfSize
                    MaskShapeType.RECTANGLE -> {
                        val rx = halfSize * 1.15f
                        val ry = halfSize * 0.85f
                        val cr = halfSize * 0.18f
                        val qx = kotlin.math.abs(dx) - (rx - cr)
                        val qy = kotlin.math.abs(dy) - (ry - cr)
                        sqrt(max(qx, 0f) * max(qx, 0f) + max(qy, 0f) * max(qy, 0f)) +
                            min(max(qx, qy), 0f) - cr
                    }
                    MaskShapeType.LINEAR -> dy
                    MaskShapeType.MIRROR -> kotlin.math.abs(dy) - halfSize * 0.55f
                    MaskShapeType.HEART -> {
                        val nx = dx / (halfSize * 0.92f)
                        val ny = -(dy - halfSize * 0.12f) / (halfSize * 0.92f)
                        val a = nx * nx + ny * ny - 1f
                        val f = a * a * a - (nx * nx) * (ny * ny * ny)
                        f * halfSize * 0.45f
                    }
                    MaskShapeType.STAR -> {
                        val angle = (kotlin.math.atan2(dy, dx) + (Math.PI / 2.0).toFloat() + (2.0 * Math.PI).toFloat()) % sectorStep
                        val a = kotlin.math.abs(angle - sectorStep * 0.5f - (-sectorStep * 0.5f)).let { raw ->
                            val centered = ((angle % sectorStep) + sectorStep) % sectorStep
                            kotlin.math.abs(if (centered > sectorStep * 0.5f) centered - sectorStep else centered)
                        }
                        val denom = (innerStarR * cos(a) * sinPi5 + (halfSize - innerStarR * cosPi5) * sin(a))
                            .coerceAtLeast(1e-4f)
                        val rMax = (halfSize * innerStarR * sinPi5) / denom
                        sqrt(dx * dx + dy * dy) - rMax
                    }
                }

                var maskAlpha = when {
                    sd <= -featherPx * 0.5f -> 1f
                    sd >= featherPx * 0.5f -> 0f
                    else -> {
                        val t = 1f - (sd + featherPx * 0.5f) / featherPx
                        t * t * (3f - 2f * t)
                    }
                }
                if (invert) maskAlpha = 1f - maskAlpha

                val sp = srcPixels[idx]
                val srcA = (sp ushr 24) and 0xFF
                val finalA = (srcA * maskAlpha).roundToInt().coerceIn(0, 255)
                outPixels[idx] = (finalA shl 24) or (sp and 0x00FFFFFF)
                idx++
            }
        }
        return createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            setPixels(outPixels, 0, w, 0, 0, w, h)
        }
    }

    private fun applyEffectsOverlaysAndFrames(
        src: Bitmap,
        state: StudioEditState,
        context: Context? = null
    ): Bitmap {
        val hasFxPreset = state.fxEffectId != "fx_none" && state.fxEffectId != "fx_body_none"
        val needsFx = state.effectMode != EffectMode.NONE ||
            hasFxPreset ||
            state.overlayMode != OverlayMode.NONE ||
            !state.overlayImageUriString.isNullOrBlank() ||
            state.vignette > 0.02f ||
            state.frameStyle != FrameStyle.NONE
        if (!needsFx) return src

        var working = src.copy(Bitmap.Config.ARGB_8888, true)

        if (state.effectMode == EffectMode.SOFT_FOCUS || state.effectMode == EffectMode.GLOW_BLOOM ||
            state.fxEffectId.startsWith("fx_blur_") || state.fxEffectId.startsWith("fx_glow_")
        ) {
            val blurFrac = if (state.fxEffectId.startsWith("fx_blur_") || state.fxEffectId.startsWith("fx_glow_")) {
                state.fxIntensity * 0.5f
            } else {
                state.effectIntensity * 0.45f
            }
            working = applySoftSkinBlend(working, blurFrac)
        }

        val canvas = Canvas(working)
        val w = working.width.toFloat()
        val h = working.height.toFloat()

        // FX Effects Picker Export / Preview rendering
        if (hasFxPreset) {
            val fxSpec = StudioCatalog.fxEffects.firstOrNull { it.id == state.fxEffectId }
            val accent = fxSpec?.accentArgb?.toInt() ?: Color.MAGENTA
            val fxi = state.fxIntensity.coerceIn(0.15f, 1f)
            when {
                state.fxEffectId.startsWith("fx_rgb_") || state.fxEffectId.startsWith("fx_glitch_") -> {
                    val shiftPx = (w * 0.018f * fxi).coerceAtLeast(3f)
                    val ghostPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        alpha = (85 * fxi).roundToInt().coerceIn(25, 110)
                    }
                    canvas.drawBitmap(working, shiftPx, 0f, ghostPaint)
                    val scanPaint = Paint().apply {
                        color = Color.argb((65 * fxi).roundToInt(), Color.red(accent), Color.green(accent), Color.blue(accent))
                    }
                    var row = 0f
                    while (row < h) {
                        canvas.drawRect(0f, row, w, row + max(2f, h * 0.008f), scanPaint)
                        row += max(12f, h * 0.06f)
                    }
                }
                fxSpec?.tab == FxTab.BODY -> {
                    val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        shader = RadialGradient(
                            w * 0.5f, h * 0.45f, max(w, h) * 0.55f,
                            intArrayOf(
                                Color.TRANSPARENT,
                                Color.argb((120 * fxi).roundToInt(), Color.red(accent), Color.green(accent), Color.blue(accent)),
                                Color.TRANSPARENT
                            ),
                            floatArrayOf(0.28f, 0.62f, 1f),
                            Shader.TileMode.CLAMP
                        )
                    }
                    canvas.drawRect(0f, 0f, w, h, haloPaint)
                }
                else -> {
                    val tintOverlay = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        shader = LinearGradient(
                            0f, 0f, w, h,
                            Color.argb((65 * fxi).roundToInt(), Color.red(accent), Color.green(accent), Color.blue(accent)),
                            Color.TRANSPARENT,
                            Shader.TileMode.CLAMP
                        )
                    }
                    canvas.drawRect(0f, 0f, w, h, tintOverlay)
                }
            }
        }

        // Film grain or haze
        if (state.effectMode == EffectMode.FILM_GRAIN || state.fxEffectId.startsWith("fx_retro_")) {
            val strength = if (state.fxEffectId.startsWith("fx_retro_")) state.fxIntensity else state.effectIntensity
            val grainPaint = Paint().apply {
                alpha = (strength * 45).roundToInt().coerceIn(8, 55)
            }
            val rng = Random(42)
            val step = max(3, (min(w, h) / 180f).roundToInt())
            var y = 0
            while (y < working.height) {
                var x = 0
                while (x < working.width) {
                    val v = rng.nextInt(256)
                    grainPaint.color = Color.argb(grainPaint.alpha, v, v, v)
                    canvas.drawRect(x.toFloat(), y.toFloat(), (x + step).toFloat(), (y + step).toFloat(), grainPaint)
                    x += step
                }
                y += step
            }
        } else if (state.effectMode == EffectMode.CINEMA_HAZE) {
            canvas.drawColor(Color.argb((state.effectIntensity * 42).roundToInt(), 210, 195, 225))
        }

        // Light Leak / Prism Overlays
        when (state.overlayMode) {
            OverlayMode.NONE -> Unit
            OverlayMode.SUNSET_LEAK -> {
                val p = Paint().apply {
                    shader = LinearGradient(
                        0f, 0f, w * 0.7f, h * 0.7f,
                        Color.argb((state.overlayIntensity * 140).roundToInt(), 255, 118, 64),
                        Color.TRANSPARENT,
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, w, h, p)
            }
            OverlayMode.PRISM_FLARE -> {
                val p = Paint().apply {
                    shader = LinearGradient(
                        w, 0f, 0f, h,
                        Color.argb((state.overlayIntensity * 130).roundToInt(), 74, 219, 200),
                        Color.argb((state.overlayIntensity * 90).roundToInt(), 255, 92, 168),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, w, h, p)
            }
            OverlayMode.STUDIO_DUO -> {
                val p = Paint().apply {
                    shader = LinearGradient(
                        0f, h, w, 0f,
                        Color.argb((state.overlayIntensity * 115).roundToInt(), 226, 194, 117),
                        Color.TRANSPARENT,
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, w, h, p)
            }
        }

        // Custom Picture-in-Picture Photo Overlay
        val pipUriStr = state.overlayImageUriString
        if (!pipUriStr.isNullOrBlank() && context != null) {
            runCatching {
                val pipUri = Uri.parse(pipUriStr)
                val pipBmp = context.contentResolver.openInputStream(pipUri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
                if (pipBmp != null) {
                    val targetW = (w * 0.42f).roundToInt().coerceAtLeast(24)
                    val targetH = ((pipBmp.height.toFloat() / max(1, pipBmp.width)) * targetW).roundToInt().coerceAtLeast(24)
                    val scaledPip = Bitmap.createScaledBitmap(pipBmp, targetW, targetH, true)
                    val left = (w - targetW) * 0.5f
                    val top = (h - targetH) * 0.5f
                    val pipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        alpha = (state.overlayImageOpacity.coerceIn(0.05f, 1f) * 255f).roundToInt()
                    }
                    canvas.drawBitmap(scaledPip, left, top, pipPaint)
                    if (scaledPip != pipBmp) scaledPip.recycle()
                    pipBmp.recycle()
                }
            }
        }

        // Vignette
        if (state.vignette > 0.02f) {
            val radius = max(w, h) * 0.75f
            val vigPaint = Paint().apply {
                shader = RadialGradient(
                    w / 2f, h / 2f, radius,
                    intArrayOf(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT,
                        Color.argb((state.vignette * 210).roundToInt().coerceIn(0, 225), 0, 0, 0)
                    ),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, w, h, vigPaint)
        }

        // Frames
        when (state.frameStyle) {
            FrameStyle.NONE -> Unit
            FrameStyle.CINEMA_BARS -> {
                val barH = h * 0.11f
                val barPaint = Paint().apply { color = Color.BLACK }
                canvas.drawRect(0f, 0f, w, barH, barPaint)
                canvas.drawRect(0f, h - barH, w, h, barPaint)
            }
            FrameStyle.POLAROID -> {
                val pad = min(w, h) * 0.045f
                val bottomPad = min(w, h) * 0.14f
                val outW = working.width + (pad * 2).roundToInt()
                val outH = working.height + (pad + bottomPad).roundToInt()
                val framed = createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
                val fCanvas = Canvas(framed)
                fCanvas.drawColor(Color.parseColor("#F5F3EF"))
                fCanvas.drawBitmap(working, pad, pad, null)
                return framed
            }
            FrameStyle.DARK_FILM -> {
                val stroke = min(w, h) * 0.035f
                val borderPaint = Paint().apply {
                    color = Color.parseColor("#121112")
                    style = Paint.Style.STROKE
                    strokeWidth = stroke * 2f
                }
                canvas.drawRect(0f, 0f, w, h, borderPaint)
            }
        }

        return working
    }

    private fun bakeCreativeOverlays(src: Bitmap, state: StudioEditState, context: Context?): Bitmap {
        val out = src.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(out)
        val w = out.width.toFloat()
        val h = out.height.toFloat()
        val baseSize = min(w, h) * 0.055f

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val boxBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val boxStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = max(2f, baseSize * 0.08f)
        }

        state.textOverlays.forEach { item ->
            val fontSpec = StudioCatalog.studioFonts.firstOrNull { it.id == item.fontId }
            val loadedTypeface = if (context != null && fontSpec != null) {
                runCatching { ResourcesCompat.getFont(context, fontSpec.fontRes) }.getOrNull()
            } else null
            textPaint.typeface = loadedTypeface ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = baseSize * item.scale

            val effectiveColor = if (item.artPreset != TextArtPreset.NONE) {
                item.artPreset.accentArgb.toInt()
            } else {
                item.colorArgb.toInt()
            }
            textPaint.color = effectiveColor

            if (item.stylePreset == TextBoxStylePreset.SHADOW_GLOW || item.artPreset != TextArtPreset.NONE) {
                val glowColor = if (item.artPreset != TextArtPreset.NONE) {
                    item.artPreset.accentArgb.toInt()
                } else Color.argb(190, 0, 0, 0)
                textPaint.setShadowLayer(baseSize * 0.28f, 0f, baseSize * 0.07f, glowColor)
            } else {
                textPaint.clearShadowLayer()
            }

            val cx = item.normX * w
            val cy = item.normY * h
            val bounds = Rect()
            textPaint.getTextBounds(item.text, 0, item.text.length, bounds)
            val padX = baseSize * 0.5f * item.scale
            val padY = baseSize * 0.32f * item.scale

            canvas.save()
            canvas.rotate(item.rotationDeg, cx, cy)
            when (item.stylePreset) {
                TextBoxStylePreset.SOLID_PILL -> {
                    boxBgPaint.color = Color.argb(230, 18, 18, 24)
                    val rect = RectF(
                        cx - bounds.width() / 2f - padX,
                        cy - bounds.height() / 2f - padY,
                        cx + bounds.width() / 2f + padX,
                        cy + bounds.height() / 2f + padY
                    )
                    canvas.drawRoundRect(rect, rect.height() / 2f, rect.height() / 2f, boxBgPaint)
                }
                TextBoxStylePreset.FROST_CARD -> {
                    boxBgPaint.color = Color.argb(145, 255, 255, 255)
                    val rect = RectF(
                        cx - bounds.width() / 2f - padX,
                        cy - bounds.height() / 2f - padY,
                        cx + bounds.width() / 2f + padX,
                        cy + bounds.height() / 2f + padY
                    )
                    canvas.drawRoundRect(rect, baseSize * 0.28f, baseSize * 0.28f, boxBgPaint)
                }
                TextBoxStylePreset.OUTLINE_BADGE -> {
                    boxStrokePaint.color = effectiveColor
                    val rect = RectF(
                        cx - bounds.width() / 2f - padX,
                        cy - bounds.height() / 2f - padY,
                        cx + bounds.width() / 2f + padX,
                        cy + bounds.height() / 2f + padY
                    )
                    canvas.drawRoundRect(rect, baseSize * 0.24f, baseSize * 0.24f, boxStrokePaint)
                }
                else -> Unit
            }
            canvas.drawText(item.text, cx, cy + bounds.height() / 2f, textPaint)
            canvas.restore()
        }

        val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(215, 18, 17, 18)
        }
        val badgeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(120, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val stickerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        state.stickerOverlays.forEach { sticker ->
            val cx = sticker.normX * w
            val cy = sticker.normY * h
            canvas.save()
            canvas.rotate(sticker.rotationDeg, cx, cy)
            if (sticker.isBadge) {
                stickerTextPaint.textSize = baseSize * 0.72f * sticker.scale
                stickerTextPaint.clearShadowLayer()
                val bounds = Rect()
                stickerTextPaint.getTextBounds(sticker.label, 0, sticker.label.length, bounds)
                val padX = baseSize * 0.6f * sticker.scale
                val padY = baseSize * 0.38f * sticker.scale
                val pill = RectF(
                    cx - bounds.width() / 2f - padX,
                    cy - bounds.height() / 2f - padY,
                    cx + bounds.width() / 2f + padX,
                    cy + bounds.height() / 2f + padY
                )
                val corner = pill.height() / 2f
                canvas.drawRoundRect(pill, corner, corner, badgeBgPaint)
                canvas.drawRoundRect(pill, corner, corner, badgeBorderPaint)
                canvas.drawText(sticker.label, cx, cy + bounds.height() / 2f, stickerTextPaint)
            } else {
                stickerTextPaint.textSize = baseSize * 1.45f * sticker.scale
                stickerTextPaint.setShadowLayer(baseSize * 0.22f, 0f, baseSize * 0.06f, Color.argb(160, 0, 0, 0))
                val metrics = stickerTextPaint.fontMetrics
                val baseline = cy - (metrics.ascent + metrics.descent) / 2f
                canvas.drawText(sticker.label, cx, baseline, stickerTextPaint)
            }
            canvas.restore()
        }

        return out
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxDim: Int): Int {
        var inSampleSize = 1
        while (width / inSampleSize > maxDim * 2 || height / inSampleSize > maxDim * 2) {
            inSampleSize *= 2
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun scaleToMax(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val curMax = max(w, h)
        if (curMax <= maxDimension) return bitmap
        val ratio = maxDimension.toFloat() / curMax.toFloat()
        val targetW = max(1, (w * ratio).roundToInt())
        val targetH = max(1, (h * ratio).roundToInt())
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }

    private fun applyExifOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        val orientation = runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
