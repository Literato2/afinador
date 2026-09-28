package com.juanito.afinador

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.abs

class TunerState(initial: Instrument) {
    var instrument by mutableStateOf(initial)
        private set
    /** null = automático; si no, la cuerda fijada a mano. */
    var locked by mutableStateOf<GuitarString?>(null)
    var target by mutableStateOf(initial.strings[0])
        private set
    var hz by mutableStateOf<Float?>(null)
        private set
    var cents by mutableStateOf(0f)
        private set
    /** Hay nota ahora mismo (si no, se mantiene la última atenuada). */
    var live by mutableStateOf(false)
        private set
    val tuned = mutableStateListOf<Int>()
    /** Ya sonó alguna nota en esta sesión (oculta la dedicatoria). */
    var started by mutableStateOf(false)
        private set

    private var inTuneSince = 0L

    fun onPitch(value: Float?) {
        if (value == null) {
            live = false
            inTuneSince = 0L
            return
        }
        val string = locked ?: Tuning.closest(value, instrument.strings)
        val c = Tuning.cents(value, string.hz)
        if (string != target) inTuneSince = 0L
        target = string
        hz = value
        cents = c
        live = true
        started = true

        if (abs(c) <= Tuning.IN_TUNE_CENTS) {
            val now = SystemClock.elapsedRealtime()
            if (inTuneSince == 0L) inTuneSince = now
            if (now - inTuneSince > 700 && string.number !in tuned) tuned.add(string.number)
        } else {
            inTuneSince = 0L
        }
    }

    fun select(next: Instrument) {
        if (next == instrument) return
        instrument = next
        locked = null
        target = next.strings[0]
        live = false
        tuned.clear()
    }

    fun toggleLock(string: GuitarString) {
        locked = if (locked == string) null else string
        target = string
        live = false
    }
}
