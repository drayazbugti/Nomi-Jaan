package com.example.media

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.core.graphics.createBitmap
import com.example.model.BackgroundReplaceMode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import kotlin.coroutines.resume
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Offline On-Device Background Remover (Keep Person, Remove Background).
 * Uses Google ML Kit Selfie Segmentation when native libraries are available,
 * with a deterministic center-foreground + edge-color matte fallback for robustness.
 */
object BackgroundRemoverEngine {

    /**
     * Extracts a grayscale ARGB_8888 person confidence mask (0..255 alpha stored in RGB & A)
     * matching [source] dimensions so it can be cleanly rotated/cropped alongside the photo.
     */
    suspend fun extractPersonMask(source: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val mlKitMask = runCatching { extractWithMlKit(source) }.getOrNull()
        mlKitMask ?: buildSalientPersonMaskFallback(source)
    }

    private suspend fun extractWithMlKit(source: Bitmap): Bitmap? {
        val options = SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
            .enableRawSizeMask()
            .build()
        val segmenter = Segmentation.getClient(options)
        val inputImage = InputImage.fromBitmap(source, 0)

        return try {
            val mask = suspendCancellableCoroutine<com.google.mlkit.vision.segmentation.SegmentationMask?> { cont ->
                segmenter.process(inputImage)
                    .addOnSuccessListener { result ->
                        if (cont.isActive) cont.resume(result)
                    }
                    .addOnFailureListener {
                        if (cont.isActive) cont.resume(null)
                    }
                    .addOnCanceledListener {
                        if (cont.isActive) cont.resume(null)
                    }
            } ?: return null

            val maskW = mask.width
            val maskH = mask.height
            if (maskW <= 0 || maskH <= 0) return null

            val buffer: ByteBuffer = mask.buffer
            buffer.rewind()
            val smallPixels = IntArray(maskW * maskH)
            var nonZeroCount = 0
            for (i in 0 until maskW * maskH) {
                val confidence = buffer.float.coerceIn(0f, 1f)
                val alphaByte = (confidence * 255f).roundToInt().coerceIn(0, 255)
                if (alphaByte > 20) nonZeroCount++
                smallPixels[i] = (alphaByte shl 24) or (alphaByte shl 16) or (alphaByte shl 8) or alphaByte
            }

            // If ML Kit found a meaningful person region, upscale mask bilinearly to source resolution
            if (nonZeroCount < (maskW * maskH * 0.015f).toInt()) {
                return null
            }

            val rawMaskBmp = createBitmap(maskW, maskH, Bitmap.Config.ARGB_8888).apply {
                setPixels(smallPixels, 0, maskW, 0, 0, maskW, maskH)
            }
            if (maskW == source.width && maskH == source.height) {
                rawMaskBmp
            } else {
                Bitmap.createScaledBitmap(rawMaskBmp, source.width, source.height, true).also {
                    rawMaskBmp.recycle()
                }
            }
        } finally {
            runCatching { segmenter.close() }
        }
    }

    /**
     * Deterministic offline fallback segmentation that isolates a central subject/person
     * against perimeter background colors using border-sampled color distance + soft ellipse prior.
     */
    fun buildSalientPersonMaskFallback(source: Bitmap): Bitmap {
        val w = source.width
        val h = source.height
        val total = w * h
        val pixels = IntArray(total)
        source.getPixels(pixels, 0, w, 0, 0, w, h)

        // Sample border pixels (top, bottom, left, right margins) to model background color
        var sumR = 0L
        var sumG = 0L
        var sumB = 0L
        var count = 0L
        val step = max(1, min(w, h) / 24)
        for (x in 0 until w step step) {
            val topPx = pixels[x]
            val botPx = pixels[(h - 1) * w + x]
            sumR += ((topPx ushr 16) and 0xFF) + ((botPx ushr 16) and 0xFF)
            sumG += ((topPx ushr 8) and 0xFF) + ((botPx ushr 8) and 0xFF)
            sumB += (topPx and 0xFF) + (botPx and 0xFF)
            count += 2
        }
        for (y in 0 until h step step) {
            val leftPx = pixels[y * w]
            val rightPx = pixels[y * w + (w - 1)]
            sumR += ((leftPx ushr 16) and 0xFF) + ((rightPx ushr 16) and 0xFF)
            sumG += ((leftPx ushr 8) and 0xFF) + ((rightPx ushr 8) and 0xFF)
            sumB += (leftPx and 0xFF) + (rightPx and 0xFF)
            count += 2
        }

        val bgR = (sumR / max(1L, count)).toFloat()
        val bgG = (sumG / max(1L, count)).toFloat()
        val bgB = (sumB / max(1L, count)).toFloat()

        val maskPixels = IntArray(total)
        val cx = w * 0.5f
        val cy = h * 0.52f
        val rx = max(1f, w * 0.36f)
        val ry = max(1f, h * 0.44f)

        for (y in 0 until h) {
            val dy = (y - cy) / ry
            for (x in 0 until w) {
                val dx = (x - cx) / rx
                val radial = sqrt(dx * dx + dy * dy)
                val centerPrior = (1.18f - radial).coerceIn(0f, 1f)

                val px = pixels[y * w + x]
                val pr = ((px ushr 16) and 0xFF).toFloat()
                val pg = ((px ushr 8) and 0xFF).toFloat()
                val pb = (px and 0xFF).toFloat()
                val colorDist = sqrt((pr - bgR) * (pr - bgR) + (pg - bgG) * (pg - bgG) + (pb - bgB) * (pb - bgB)) / 442f

                // Keep central person & distinct foreground features; remove outer background
                val score = when {
                    radial > 1.35f && colorDist < 0.28f -> 0f
                    radial < 0.65f -> (0.72f + colorDist * 0.45f).coerceIn(0f, 1f)
                    else -> (centerPrior * 0.62f + colorDist * 0.92f - 0.15f).coerceIn(0f, 1f)
                }
                val a = (score * 255f).roundToInt().coerceIn(0, 255)
                maskPixels[y * w + x] = (a shl 24) or (a shl 16) or (a shl 8) or a
            }
        }

        return createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            setPixels(maskPixels, 0, w, 0, 0, w, h)
        }
    }

    /**
     * Applies background removal keeping the person intact and compositing over the chosen
     * transparent, blurred, solid, or gradient backdrop.
     */
    fun applyPersonCutout(
        source: Bitmap,
        alignedMask: Bitmap,
        bgMode: BackgroundReplaceMode,
        feather: Float
    ): Bitmap {
        val w = source.width
        val h = source.height
        val maskScaled = if (alignedMask.width == w && alignedMask.height == h) {
            alignedMask
        } else {
            Bitmap.createScaledBitmap(alignedMask, w, h, true)
        }

        val total = w * h
        val srcPixels = IntArray(total)
        val maskPixels = IntArray(total)
        source.getPixels(srcPixels, 0, w, 0, 0, w, h)
        maskScaled.getPixels(maskPixels, 0, w, 0, 0, w, h)
        if (maskScaled != alignedMask) maskScaled.recycle()

        // Ramp window controlled by feather (0.0 .. 1.0)
        val halfBand = 0.04f + feather.coerceIn(0f, 1f) * 0.22f
        val lowCut = (0.48f - halfBand).coerceIn(0.05f, 0.45f)
        val highCut = (0.48f + halfBand).coerceIn(0.55f, 0.95f)
        val invSpan = 1f / max(0.02f, highCut - lowCut)

        val fgPixels = IntArray(total)
        for (i in 0 until total) {
            val m = (maskPixels[i] ushr 24) and 0xFF
            val conf = m / 255f
            val alphaOut = when {
                conf <= lowCut -> 0
                conf >= highCut -> 255
                else -> (((conf - lowCut) * invSpan) * 255f).roundToInt().coerceIn(0, 255)
            }
            val rgb = srcPixels[i] and 0x00FFFFFF
            fgPixels[i] = (alphaOut shl 24) or rgb
        }

        val foregroundCutout = createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            setPixels(fgPixels, 0, w, 0, 0, w, h)
        }

        if (bgMode == BackgroundReplaceMode.TRANSPARENT) {
            return foregroundCutout
        }

        val out = createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        when (bgMode) {
            BackgroundReplaceMode.TRANSPARENT -> Unit
            BackgroundReplaceMode.STUDIO_WHITE -> canvas.drawColor(Color.WHITE)
            BackgroundReplaceMode.SOLID_DARK -> canvas.drawColor(Color.parseColor("#121116"))
            BackgroundReplaceMode.BLUR_BG -> {
                val tiny = Bitmap.createScaledBitmap(source, max(8, w / 18), max(8, h / 18), true)
                val blurredBg = Bitmap.createScaledBitmap(tiny, w, h, true)
                tiny.recycle()
                canvas.drawBitmap(blurredBg, 0f, 0f, null)
                blurredBg.recycle()
                canvas.drawColor(Color.argb(45, 0, 0, 0))
            }
            BackgroundReplaceMode.GRADIENT_PURPLE -> {
                val p = Paint().apply {
                    shader = LinearGradient(
                        0f, 0f, w.toFloat(), h.toFloat(),
                        Color.parseColor("#6C47FF"),
                        Color.parseColor("#16112B"),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
            }
            BackgroundReplaceMode.GRADIENT_SUNSET -> {
                val p = Paint().apply {
                    shader = LinearGradient(
                        0f, 0f, w.toFloat(), h.toFloat(),
                        Color.parseColor("#FF6B4A"),
                        Color.parseColor("#2A1635"),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
            }
        }
        canvas.drawBitmap(foregroundCutout, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG))
        foregroundCutout.recycle()
        return out
    }
}
