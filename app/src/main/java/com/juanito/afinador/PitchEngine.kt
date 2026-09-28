package com.juanito.afinador

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Lee el micro y emite frecuencias (mediana de las últimas lecturas) o null. */
class PitchEngine(private val onPitch: (Float?) -> Unit) {

    private var job: Job? = null

    @SuppressLint("MissingPermission")
    fun start() {
        if (job?.isActive == true) return
        job = CoroutineScope(Dispatchers.Default).launch {
            val minBuf = AudioRecord.getMinBufferSize(RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT)
            val record = openRecord(maxOf(minBuf, SIZE * 4 * 2)) ?: return@launch
            val yin = Yin(RATE, SIZE)
            val window = FloatArray(SIZE)
            val hop = FloatArray(HOP)
            val recent = ArrayDeque<Float>()
            record.startRecording()
            try {
                while (isActive) {
                    val read = record.read(hop, 0, HOP, AudioRecord.READ_BLOCKING)
                    if (read <= 0) continue
                    System.arraycopy(window, read, window, 0, SIZE - read)
                    System.arraycopy(hop, 0, window, SIZE - read, read)

                    val hz = yin.detect(window)
                    if (hz == null) {
                        recent.clear()
                        onPitch(null)
                        continue
                    }
                    recent.addLast(hz)
                    if (recent.size > MEDIAN_OF) recent.removeFirst()
                    onPitch(recent.sorted()[recent.size / 2])
                }
            } finally {
                record.stop()
                record.release()
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    @SuppressLint("MissingPermission")
    private fun openRecord(bufferBytes: Int): AudioRecord? {
        // UNPROCESSED evita supresión de ruido y AGC, que deforman la nota.
        for (source in listOf(MediaRecorder.AudioSource.UNPROCESSED, MediaRecorder.AudioSource.MIC)) {
            val record = runCatching {
                AudioRecord(source, RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT, bufferBytes)
            }.getOrNull() ?: continue
            if (record.state == AudioRecord.STATE_INITIALIZED) return record
            record.release()
        }
        return null
    }

    private companion object {
        const val RATE = 44_100
        const val SIZE = 4096
        const val HOP = 1024
        const val MEDIAN_OF = 5
    }
}
