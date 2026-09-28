package com.juanito.afinador

/**
 * Detector de tono YIN (de Cheveigné & Kawahara, 2002): el de casi todos los
 * afinadores. Devuelve Hz o null si no hay una nota clara.
 */
class Yin(private val sampleRate: Int, private val bufferSize: Int) {

    private val diff = FloatArray(bufferSize / 2)

    fun detect(buffer: FloatArray): Float? {
        val half = bufferSize / 2
        val minLag = sampleRate / MAX_HZ
        val maxLag = minOf(half - 1, sampleRate / MIN_HZ)

        var rms = 0f
        for (s in buffer) rms += s * s
        if (kotlin.math.sqrt(rms / buffer.size) < MIN_RMS) return null

        // Función diferencia + normalización por media acumulada.
        diff[0] = 1f
        var running = 0f
        for (tau in 1 until half) {
            var sum = 0f
            for (i in 0 until half) {
                val d = buffer[i] - buffer[i + tau]
                sum += d * d
            }
            running += sum
            diff[tau] = if (running == 0f) 1f else sum * tau / running
        }

        // Primer mínimo bajo el umbral: evita saltar a la octava de abajo.
        var tau = minLag
        while (tau < maxLag) {
            if (diff[tau] < THRESHOLD) {
                while (tau + 1 < maxLag && diff[tau + 1] < diff[tau]) tau++
                break
            }
            tau++
        }
        if (tau >= maxLag || diff[tau] >= THRESHOLD) return null

        // Interpolación parabólica para precisión sub-muestra.
        val better = if (tau in 1 until half - 1) {
            val s0 = diff[tau - 1]
            val s1 = diff[tau]
            val s2 = diff[tau + 1]
            val denom = 2f * (2f * s1 - s2 - s0)
            if (denom != 0f) tau + (s2 - s0) / denom else tau.toFloat()
        } else {
            tau.toFloat()
        }
        return sampleRate / better
    }

    private companion object {
        const val THRESHOLD = 0.12f
        const val MIN_HZ = 60
        const val MAX_HZ = 1000
        const val MIN_RMS = 0.006f
    }
}
