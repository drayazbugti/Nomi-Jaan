package com.example.media

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.model.StudioMusicPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class WavData(
    val sampleRate: Int,
    val channels: Int,
    val samples: ShortArray
)

data class SilenceInterval(
    val startFrame: Int,
    val endFrame: Int
)

data class SilenceAnalysisResult(
    val totalDurationMs: Long,
    val silentDurationMs: Long,
    val silentCount: Int,
    val silentIntervals: List<SilenceInterval>,
    val keptFrameCount: Int
)

object AudioDSPProcessor {

    private fun audioCacheDir(context: Context): File =
        File(context.cacheDir, "studio_audio").apply { mkdirs() }

    /**
     * Synthesizes a polyphonic studio music or ambient BGM WAV file so every built-in
     * Music List and BGM preset plays real audible music and supports Reverb / FX.
     */
    suspend fun generateStudioTrackWav(
        context: Context,
        preset: StudioMusicPreset,
        durationSeconds: Int = 8,
        onProgress: (Float) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val sampleRate = 44_100
            val channels = 2
            val totalFrames = sampleRate * durationSeconds
            val out = ShortArray(totalFrames * channels)
            val beatIntervalSec = 60.0 / preset.bpm.coerceAtLeast(50)
            val root = preset.baseFreqHz.toDouble()
            val chordRatios = if (preset.isBgm) {
                doubleArrayOf(1.0, 1.2, 1.5, 1.8)
            } else {
                doubleArrayOf(1.0, 1.25, 1.5, 2.0)
            }

            for (i in 0 until totalFrames) {
                if (i % (sampleRate / 2) == 0) {
                    onProgress((i.toFloat() / totalFrames).coerceIn(0.05f, 0.92f))
                }
                val t = i.toDouble() / sampleRate
                val barBeat = (t % (beatIntervalSec * 4)) / beatIntervalSec
                val transpose = when {
                    barBeat < 1.0 -> 1.0
                    barBeat < 2.0 -> 1.12246
                    barBeat < 3.0 -> 1.25992
                    else -> 0.89089
                }

                // Warm stereo pad + chord harmonics
                var left = 0.0
                var right = 0.0
                for ((idx, r) in chordRatios.withIndex()) {
                    val freq = root * transpose * r
                    val phase = 2.0 * PI * freq * t
                    val tone = sin(phase) * (0.28 / (idx + 1))
                    if (idx % 2 == 0) {
                        left += tone * 0.95
                        right += tone * 0.65
                    } else {
                        left += tone * 0.65
                        right += tone * 0.95
                    }
                }

                // Rhythmic kick / hi-hat pulse for non-BGM tracks
                val beatPhase = (t % beatIntervalSec) / beatIntervalSec
                val pulseEnv = if (preset.isBgm) {
                    0.5 + 0.5 * sin(2.0 * PI * t * 0.5)
                } else {
                    exp(-beatPhase * 6.0)
                }
                val bass = if (!preset.isBgm && beatPhase < 0.28) {
                    sin(2.0 * PI * (55.0 * (1.0 - beatPhase * 0.4)) * t) * exp(-beatPhase * 12.0) * 0.45
                } else 0.0

                val fadeIn = min(1.0, t / 0.15)
                val fadeOut = min(1.0, (durationSeconds - t) / 0.3)
                val masterEnv = fadeIn * fadeOut * (0.48 + 0.32 * pulseEnv)

                val lVal = ((left * masterEnv + bass) * 26000.0).roundToInt().coerceIn(-32767, 32767)
                val rVal = ((right * masterEnv + bass) * 26000.0).roundToInt().coerceIn(-32767, 32767)
                out[i * 2] = lVal.toShort()
                out[i * 2 + 1] = rVal.toShort()
            }

            val outFile = File(audioCacheDir(context), "${preset.id}_${System.currentTimeMillis()}.wav")
            writeWavFile(outFile, WavData(sampleRate, channels, out))
            onProgress(1f)
            outFile
        }
    }

    /**
     * Decodes audio from any video (or audio) content Uri via MediaExtractor + MediaCodec into a
     * standard 16-bit PCM WAV file for standalone timeline playback, DSP effects, and saving.
     */
    suspend fun extractAudioFromVideo(
        context: Context,
        mediaUri: Uri,
        prefix: String = "extracted_audio",
        onProgress: (Float) -> Unit = {}
    ): Result<Pair<File, Long>> = withContext(Dispatchers.IO) {
        runCatching {
            val extractor = MediaExtractor()
            var codec: MediaCodec? = null
            try {
                extractor.setDataSource(context, mediaUri, null)
                var audioTrackIndex = -1
                var audioFormat: MediaFormat? = null
                for (i in 0 until extractor.trackCount) {
                    val fmt = extractor.getTrackFormat(i)
                    val mime = fmt.getString(MediaFormat.KEY_MIME).orEmpty()
                    if (mime.startsWith("audio/")) {
                        audioTrackIndex = i
                        audioFormat = fmt
                        break
                    }
                }
                if (audioTrackIndex < 0 || audioFormat == null) {
                    error("This video does not contain an audio track.")
                }

                extractor.selectTrack(audioTrackIndex)
                val mime = audioFormat.getString(MediaFormat.KEY_MIME)!!
                var sampleRate = if (audioFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                    audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                } else 44_100
                var channels = if (audioFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                    audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT).coerceIn(1, 2)
                } else 2
                val totalDurationUs = if (audioFormat.containsKey(MediaFormat.KEY_DURATION)) {
                    audioFormat.getLong(MediaFormat.KEY_DURATION).coerceAtLeast(1_000_000L)
                } else 10_000_000L

                // Cap decode at 90s to avoid OOM on huge 4K videos while providing rich audio
                val maxExtractUs = 90_000_000L

                codec = MediaCodec.createDecoderByType(mime)
                codec.configure(audioFormat, null, null, 0)
                codec.start()

                val pcmBytes = ByteArrayOutputStream()
                val bufferInfo = MediaCodec.BufferInfo()
                var sawInputEOS = false
                var sawOutputEOS = false

                while (!sawOutputEOS) {
                    if (!sawInputEOS) {
                        val inIdx = codec.dequeueInputBuffer(10_000)
                        if (inIdx >= 0) {
                            val inputBuf = codec.getInputBuffer(inIdx)!!
                            val sampleSize = extractor.readSampleData(inputBuf, 0)
                            val presentationUs = extractor.sampleTime
                            if (sampleSize < 0 || presentationUs > maxExtractUs) {
                                codec.queueInputBuffer(inIdx, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                sawInputEOS = true
                            } else {
                                codec.queueInputBuffer(inIdx, 0, sampleSize, presentationUs, 0)
                                val frac = (presentationUs.toFloat() / min(totalDurationUs, maxExtractUs).toFloat())
                                    .coerceIn(0.05f, 0.90f)
                                onProgress(frac)
                                extractor.advance()
                            }
                        }
                    }

                    val outIdx = codec.dequeueOutputBuffer(bufferInfo, 10_000)
                    when {
                        outIdx >= 0 -> {
                            if (bufferInfo.size > 0) {
                                val outBuf = codec.getOutputBuffer(outIdx)!!
                                val chunk = ByteArray(bufferInfo.size)
                                outBuf.position(bufferInfo.offset)
                                outBuf.get(chunk)
                                pcmBytes.write(chunk)
                            }
                            codec.releaseOutputBuffer(outIdx, false)
                            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                sawOutputEOS = true
                            }
                        }
                        outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            val newFmt = codec.outputFormat
                            if (newFmt.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                                sampleRate = newFmt.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                            }
                            if (newFmt.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                                channels = newFmt.getInteger(MediaFormat.KEY_CHANNEL_COUNT).coerceIn(1, 2)
                            }
                        }
                    }
                }

                onProgress(0.95f)
                val rawBytes = pcmBytes.toByteArray()
                if (rawBytes.isEmpty()) {
                    error("Extracted audio stream was empty.")
                }
                val shortCount = rawBytes.size / 2
                val samples = ShortArray(shortCount)
                ByteBuffer.wrap(rawBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples)

                val outFile = File(audioCacheDir(context), "${prefix}_${System.currentTimeMillis()}.wav")
                writeWavFile(outFile, WavData(sampleRate, channels, samples))
                val durationMs = ((shortCount / max(1, channels)).toLong() * 1000L) / max(1, sampleRate)
                onProgress(1f)
                outFile to durationMs
            } finally {
                runCatching { codec?.stop() }
                runCatching { codec?.release() }
                runCatching { extractor.release() }
            }
        }
    }

    /**
     * Applies genuine PCM audio DSP effects:
     * 1. High-pass + spectral noise gate (Noise Reduction)
     * 2. Mid/side center vocal isolation or bandpass clarity (Vocal Separation)
     * 3. Multi-tap Schroeder Hall Reverb delay feedback network (Reverb)
     */
    suspend fun applyAudioEffectsToWav(
        context: Context,
        inputFile: File,
        reverbAmount: Float,
        vocalSeparation: Boolean,
        noiseReduction: Boolean,
        noiseReductionStrength: Float = if (noiseReduction) 0.75f else 0f,
        fadeInOut: Boolean = false,
        onProgress: (Float) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            onProgress(0.10f)
            val wav = readWavFile(inputFile)
            onProgress(0.28f)
            val processed = applyDspToPcm(
                samples = wav.samples,
                sampleRate = wav.sampleRate,
                channels = wav.channels,
                reverbAmount = reverbAmount,
                vocalSeparation = vocalSeparation,
                noiseReduction = noiseReduction,
                noiseReductionStrength = noiseReductionStrength,
                fadeInOut = fadeInOut,
                onProgress = { p -> onProgress(0.28f + p * 0.62f) }
            )
            onProgress(0.92f)
            val outFile = File(audioCacheDir(context), "fx_${System.currentTimeMillis()}.wav")
            writeWavFile(outFile, WavData(wav.sampleRate, wav.channels, processed))
            onProgress(1f)
            outFile
        }
    }

    /**
     * Pure PCM DSP processor (also directly unit-testable on JVM!).
     */
    fun applyDspToPcm(
        samples: ShortArray,
        sampleRate: Int,
        channels: Int,
        reverbAmount: Float,
        vocalSeparation: Boolean,
        noiseReduction: Boolean,
        noiseReductionStrength: Float = if (noiseReduction) 0.75f else 0f,
        fadeInOut: Boolean = false,
        onProgress: (Float) -> Unit = {}
    ): ShortArray {
        val ch = channels.coerceIn(1, 2)
        val frameCount = samples.size / ch
        if (frameCount <= 0) return samples.copyOf()

        val floatBuf = FloatArray(samples.size) { samples[it] / 32768f }

        // 1. Adaptive Noise Reduction: High-pass rumble/hum cut + percentile noise-floor estimation
        //    + smooth soft-knee noise gate + gentle high-frequency hiss attenuation in quiet passages.
        if (noiseReduction && noiseReductionStrength > 0.02f) {
            val strength = noiseReductionStrength.coerceIn(0f, 1f)
            val cutoffHz = 80f + strength * 65f
            val rc = 1.0f / (2.0f * PI.toFloat() * cutoffHz)
            val dt = 1.0f / sampleRate.toFloat()
            val alphaHp = rc / (rc + dt)
            val prevIn = FloatArray(ch)
            val prevOut = FloatArray(ch)

            for (f in 0 until frameCount) {
                for (c in 0 until ch) {
                    val idx = f * ch + c
                    val x = floatBuf[idx]
                    val y = alphaHp * (prevOut[c] + x - prevIn[c])
                    prevIn[c] = x
                    prevOut[c] = y
                    floatBuf[idx] = y
                }
            }

            // Estimate stationary ambient noise floor from 20ms blocks
            val blockFrames = max(64, (sampleRate * 0.02f).roundToInt())
            val blockCount = max(1, frameCount / blockFrames)
            val blockRms = FloatArray(blockCount) { b ->
                val startF = b * blockFrames
                val endF = min(frameCount, startF + blockFrames)
                var sq = 0.0
                var n = 0
                for (f in startF until endF) {
                    for (c in 0 until ch) {
                        val v = floatBuf[f * ch + c]
                        sq += v * v
                        n++
                    }
                }
                if (n > 0) sqrt(sq / n).toFloat() else 0f
            }
            val sortedRms = blockRms.sortedArray()
            val noiseFloor = sortedRms[(sortedRms.size * 0.22f).roundToInt().coerceIn(0, sortedRms.lastIndex)]
            val gateThreshold = max(0.018f, noiseFloor * (1.25f + strength * 2.0f)).coerceAtMost(0.28f)
            val floorAtten = (1f - strength * 0.92f).coerceIn(0.05f, 1f)

            var env = 0f
            val prevLp = FloatArray(ch)
            for (f in 0 until frameCount) {
                var framePeak = 0f
                for (c in 0 until ch) {
                    framePeak = max(framePeak, abs(floatBuf[f * ch + c]))
                }
                env = env * 0.990f + framePeak * 0.010f
                val gateGain = if (env < gateThreshold) {
                    val ratio = (env / gateThreshold).coerceIn(0f, 1f)
                    floorAtten + (1f - floorAtten) * (ratio * ratio)
                } else {
                    1f
                }
                val lpAlpha = 0.35f + 0.65f * gateGain
                for (c in 0 until ch) {
                    val idx = f * ch + c
                    val gated = floatBuf[idx] * gateGain
                    val smoothed = prevLp[c] + lpAlpha * (gated - prevLp[c])
                    prevLp[c] = smoothed
                    floatBuf[idx] = smoothed
                }
            }
        }
        onProgress(0.35f)

        // 2. Vocal Separation: Stereo center vocal isolation (mid emphasis / side suppression)
        //    or mono vocal formant presence boost.
        if (vocalSeparation) {
            if (ch == 2) {
                for (f in 0 until frameCount) {
                    val l = floatBuf[f * 2]
                    val r = floatBuf[f * 2 + 1]
                    val mid = (l + r) * 0.5f
                    val side = (l - r) * 0.5f
                    val isolated = (mid * 1.15f + side * 0.14f).coerceIn(-1f, 1f)
                    floatBuf[f * 2] = isolated
                    floatBuf[f * 2 + 1] = isolated
                }
            } else {
                // Mono vocal formant bandpass-like presence boost
                var lpLow = 0f
                var lpHigh = 0f
                for (f in floatBuf.indices) {
                    val x = floatBuf[f]
                    lpLow += 0.03f * (x - lpLow)
                    lpHigh += 0.35f * (x - lpHigh)
                    floatBuf[f] = ((lpHigh - lpLow) * 1.35f).coerceIn(-1f, 1f)
                }
            }
        }
        onProgress(0.65f)

        // 3. Multi-tap Schroeder Hall Reverb (comb feedback delay lines per channel)
        val wet = reverbAmount.coerceIn(0f, 1f)
        if (wet > 0.01f) {
            val combDelaysMs = floatArrayOf(29.7f, 37.1f, 43.3f, 59.9f, 83.7f)
            val feedbacks = floatArrayOf(0.76f, 0.72f, 0.68f, 0.63f, 0.56f)
            val delayLengths = IntArray(combDelaysMs.size) { idx ->
                max(64, (sampleRate * combDelaysMs[idx] / 1000f).roundToInt())
            }
            val delayLines = Array(ch) {
                Array(combDelaysMs.size) { tap -> FloatArray(delayLengths[tap]) }
            }
            val writeIdx = Array(ch) { IntArray(combDelaysMs.size) }
            val lpDamp = Array(ch) { FloatArray(combDelaysMs.size) }

            val dryMix = 1f - wet * 0.32f
            val wetScale = wet * 0.65f

            for (f in 0 until frameCount) {
                for (c in 0 until ch) {
                    val idx = f * ch + c
                    val input = floatBuf[idx]
                    var reverbSum = 0f

                    for (tap in combDelaysMs.indices) {
                        val buf = delayLines[c][tap]
                        val pos = writeIdx[c][tap]
                        val delayed = buf[pos]
                        // High-frequency damping in feedback path for realistic warm hall tail
                        val damped = lpDamp[c][tap] * 0.32f + delayed * 0.68f
                        lpDamp[c][tap] = damped
                        buf[pos] = (input + damped * feedbacks[tap]).coerceIn(-1.5f, 1.5f)
                        writeIdx[c][tap] = (pos + 1) % buf.size
                        reverbSum += delayed
                    }

                    val wetSample = reverbSum / combDelaysMs.size
                    floatBuf[idx] = (input * dryMix + wetSample * wetScale).coerceIn(-0.99f, 0.99f)
                }
            }
        }
        if (fadeInOut && frameCount > 100) {
            val fadeInFrames = min(frameCount / 4, (sampleRate * 0.35f).roundToInt())
            val fadeOutFrames = min(frameCount / 4, (sampleRate * 0.45f).roundToInt())
            for (f in 0 until fadeInFrames) {
                val g = f.toFloat() / max(1, fadeInFrames)
                for (c in 0 until ch) floatBuf[f * ch + c] *= g
            }
            for (f in 0 until fadeOutFrames) {
                val frameIdx = frameCount - 1 - f
                val g = f.toFloat() / max(1, fadeOutFrames)
                for (c in 0 until ch) floatBuf[frameIdx * ch + c] *= g
            }
        }
        onProgress(1f)

        return ShortArray(floatBuf.size) { i ->
            (floatBuf[i].coerceIn(-1f, 1f) * 32767f).roundToInt().coerceIn(-32768, 32767).toShort()
        }
    }

    /**
     * Scans PCM RMS windows to detect silent pauses above [minSilenceMs] below [thresholdDb].
     */
    fun detectSilentRegions(
        wav: WavData,
        thresholdDb: Float = -35f,
        minSilenceMs: Long = 350L
    ): SilenceAnalysisResult {
        val ch = wav.channels.coerceIn(1, 2)
        val totalFrames = wav.samples.size / ch
        val totalDurationMs = (totalFrames.toLong() * 1000L) / max(1, wav.sampleRate)
        if (totalFrames <= 0) {
            return SilenceAnalysisResult(0L, 0L, 0, emptyList(), 0)
        }

        val winFrames = max(64, (wav.sampleRate * 0.025f).roundToInt()) // 25ms windows
        val minSilentFrames = max(winFrames, ((wav.sampleRate * minSilenceMs) / 1000L).toInt())
        val intervals = mutableListOf<SilenceInterval>()

        var runStart = -1
        var f = 0
        while (f < totalFrames) {
            val end = min(totalFrames, f + winFrames)
            var sq = 0.0
            var count = 0
            for (i in f until end) {
                for (c in 0 until ch) {
                    val v = wav.samples[i * ch + c] / 32768.0
                    sq += v * v
                    count++
                }
            }
            val rms = if (count > 0) sqrt(sq / count) else 0.0
            val db = if (rms > 1e-6) (20.0 * log10(rms)).toFloat() else -96f
            if (db < thresholdDb) {
                if (runStart < 0) runStart = f
            } else {
                if (runStart >= 0) {
                    if (f - runStart >= minSilentFrames) {
                        intervals.add(SilenceInterval(runStart, f))
                    }
                    runStart = -1
                }
            }
            f = end
        }
        if (runStart >= 0 && totalFrames - runStart >= minSilentFrames) {
            intervals.add(SilenceInterval(runStart, totalFrames))
        }

        val silentFrames = intervals.sumOf { it.endFrame - it.startFrame }
        val silentDurationMs = (silentFrames.toLong() * 1000L) / max(1, wav.sampleRate)
        val keptFrames = max(0, totalFrames - silentFrames)
        return SilenceAnalysisResult(
            totalDurationMs = totalDurationMs,
            silentDurationMs = silentDurationMs,
            silentCount = intervals.size,
            silentIntervals = intervals,
            keptFrameCount = keptFrames
        )
    }

    suspend fun analyzeSilenceInWavFile(
        inputFile: File,
        thresholdDb: Float,
        minSilenceMs: Long
    ): Result<SilenceAnalysisResult> = withContext(Dispatchers.IO) {
        runCatching {
            val wav = readWavFile(inputFile)
            detectSilentRegions(wav, thresholdDb, minSilenceMs)
        }
    }

    suspend fun trimSilentRegionsWav(
        context: Context,
        inputFile: File,
        thresholdDb: Float,
        minSilenceMs: Long,
        onProgress: (Float) -> Unit = {}
    ): Result<Pair<File, SilenceAnalysisResult>> = withContext(Dispatchers.IO) {
        runCatching {
            onProgress(0.12f)
            val wav = readWavFile(inputFile)
            val ch = wav.channels.coerceIn(1, 2)
            val totalFrames = wav.samples.size / ch
            val analysis = detectSilentRegions(wav, thresholdDb, minSilenceMs)
            onProgress(0.45f)

            if (analysis.silentIntervals.isEmpty() || analysis.keptFrameCount < ch * 10) {
                val copyOut = File(audioCacheDir(context), "trimmed_${System.currentTimeMillis()}.wav")
                inputFile.copyTo(copyOut, overwrite = true)
                onProgress(1f)
                return@runCatching copyOut to analysis
            }

            val outSamples = ShortArray(analysis.keptFrameCount * ch)
            var outCursor = 0
            var intervalIdx = 0
            for (frame in 0 until totalFrames) {
                while (intervalIdx < analysis.silentIntervals.size &&
                    frame >= analysis.silentIntervals[intervalIdx].endFrame
                ) {
                    intervalIdx++
                }
                val inSilence = intervalIdx < analysis.silentIntervals.size &&
                    frame >= analysis.silentIntervals[intervalIdx].startFrame
                if (!inSilence && outCursor + ch <= outSamples.size) {
                    for (c in 0 until ch) {
                        outSamples[outCursor++] = wav.samples[frame * ch + c]
                    }
                }
            }

            onProgress(0.85f)
            val trimmed = if (outCursor == outSamples.size) outSamples else outSamples.copyOf(outCursor)
            val outFile = File(audioCacheDir(context), "silence_cut_${System.currentTimeMillis()}.wav")
            writeWavFile(outFile, WavData(wav.sampleRate, wav.channels, trimmed))
            onProgress(1f)
            outFile to analysis
        }
    }

    suspend fun saveAudioToMusicCollection(
        context: Context,
        wavFile: File,
        title: String
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val safeName = title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(28).ifBlank { "BugtiAudio" }
            val fileName = "${safeName}_${System.currentTimeMillis()}.wav"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
                    put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/Bugti Editz")
                    put(MediaStore.Audio.Media.IS_MUSIC, 1)
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                    ?: error("Failed to create MediaStore audio entry")
                resolver.openOutputStream(uri)?.use { out ->
                    FileInputStream(wavFile).use { input -> input.copyTo(out) }
                } ?: error("Failed to open audio output stream")
                values.clear()
                values.put(MediaStore.Audio.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                uri
            } else {
                val musicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: context.filesDir
                val outDir = File(musicDir, "Bugti Editz").apply { mkdirs() }
                val dest = File(outDir, fileName)
                wavFile.copyTo(dest, overwrite = true)
                Uri.fromFile(dest)
            }
        }
    }

    fun writeWavFile(file: File, wav: WavData) {
        val byteRate = wav.sampleRate * wav.channels * 2
        val dataSize = wav.samples.size * 2
        val totalSize = 36 + dataSize
        FileOutputStream(file).use { fos ->
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            header.put("RIFF".toByteArray(Charsets.US_ASCII))
            header.putInt(totalSize)
            header.put("WAVE".toByteArray(Charsets.US_ASCII))
            header.put("fmt ".toByteArray(Charsets.US_ASCII))
            header.putInt(16) // PCM fmt chunk size
            header.putShort(1.toShort()) // PCM format
            header.putShort(wav.channels.toShort())
            header.putInt(wav.sampleRate)
            header.putInt(byteRate)
            header.putShort((wav.channels * 2).toShort())
            header.putShort(16.toShort()) // Bits per sample
            header.put("data".toByteArray(Charsets.US_ASCII))
            header.putInt(dataSize)
            fos.write(header.array())

            val dataBytes = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)
            dataBytes.asShortBuffer().put(wav.samples)
            fos.write(dataBytes.array())
        }
    }

    fun readWavFile(file: File): WavData {
        val bytes = file.readBytes()
        require(bytes.size >= 44) { "Invalid WAV file" }
        val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        var pos = 12 // skip RIFF + size + WAVE
        var channels = 2
        var sampleRate = 44_100
        var dataOffset = 44
        var dataSize = bytes.size - 44

        while (pos + 8 <= bytes.size) {
            val id = String(bytes, pos, 4, Charsets.US_ASCII)
            val chunkSize = buf.getInt(pos + 4).coerceAtLeast(0)
            if (id == "fmt " && pos + 8 + 16 <= bytes.size) {
                channels = buf.getShort(pos + 10).toInt().coerceIn(1, 2)
                sampleRate = buf.getInt(pos + 12).coerceAtLeast(8000)
            } else if (id == "data") {
                dataOffset = pos + 8
                dataSize = min(chunkSize, bytes.size - dataOffset)
                break
            }
            pos += 8 + chunkSize + (chunkSize and 1)
        }

        val shortCount = (dataSize / 2).coerceAtLeast(0)
        val samples = ShortArray(shortCount)
        ByteBuffer.wrap(bytes, dataOffset, shortCount * 2)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asShortBuffer()
            .get(samples)
        return WavData(sampleRate, channels, samples)
    }

    class MicAudioRecorder(private val context: Context) {
        private var audioRecord: AudioRecord? = null
        private var recordingThread: Thread? = null
        @Volatile
        private var isRecordingFlag = false
        private var outputFile: File? = null

        @SuppressLint("MissingPermission")
        fun startRecording(): Result<File> = runCatching {
            val sampleRate = 44_100
            val minBuf = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)
            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBuf * 2
            )
            if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                recorder.release()
                error("Microphone initialization failed")
            }
            val file = File(audioCacheDir(context), "mic_rec_${System.currentTimeMillis()}.wav")
            // Reserve 44-byte placeholder header
            FileOutputStream(file).use { it.write(ByteArray(44)) }

            audioRecord = recorder
            outputFile = file
            isRecordingFlag = true
            recorder.startRecording()

            recordingThread = Thread {
                val buffer = ByteArray(minBuf)
                var totalPcmBytes = 0
                FileOutputStream(file, true).use { fos ->
                    while (isRecordingFlag) {
                        val read = recorder.read(buffer, 0, buffer.size)
                        if (read > 0) {
                            fos.write(buffer, 0, read)
                            totalPcmBytes += read
                        }
                    }
                }
                // Patch WAV header with exact sizes
                runCatching {
                    RandomAccessFile(file, "rw").use { raf ->
                        raf.seek(0)
                        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
                        header.put("RIFF".toByteArray(Charsets.US_ASCII))
                        header.putInt(36 + totalPcmBytes)
                        header.put("WAVE".toByteArray(Charsets.US_ASCII))
                        header.put("fmt ".toByteArray(Charsets.US_ASCII))
                        header.putInt(16)
                        header.putShort(1.toShort())
                        header.putShort(1.toShort())
                        header.putInt(sampleRate)
                        header.putInt(sampleRate * 2)
                        header.putShort(2.toShort())
                        header.putShort(16.toShort())
                        header.put("data".toByteArray(Charsets.US_ASCII))
                        header.putInt(totalPcmBytes)
                        raf.write(header.array())
                    }
                }
            }.apply { start() }

            file
        }

        fun stopRecording(): File? {
            isRecordingFlag = false
            runCatching { audioRecord?.stop() }
            runCatching { recordingThread?.join(1500L) }
            runCatching { audioRecord?.release() }
            audioRecord = null
            recordingThread = null
            return outputFile?.takeIf { it.exists() && it.length() > 44L }
        }
    }
}
