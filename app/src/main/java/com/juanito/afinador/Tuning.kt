package com.juanito.afinador

import kotlin.math.abs
import kotlin.math.log2

data class GuitarString(val number: Int, val name: String, val octave: Int, val hz: Float)

object Tuning {
    const val IN_TUNE_CENTS = 4f

    // De la 6ª (grave) a la 1ª (aguda), afinación estándar.
    val strings = listOf(
        GuitarString(6, "E", 2, 82.41f),
        GuitarString(5, "A", 2, 110.00f),
        GuitarString(4, "D", 3, 146.83f),
        GuitarString(3, "G", 3, 196.00f),
        GuitarString(2, "B", 3, 246.94f),
        GuitarString(1, "E", 4, 329.63f),
    )

    fun cents(hz: Float, target: Float): Float = 1200f * log2(hz / target)

    fun closest(hz: Float): GuitarString = strings.minBy { abs(cents(hz, it.hz)) }
}
