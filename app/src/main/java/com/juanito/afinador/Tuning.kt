package com.juanito.afinador

import kotlin.math.abs
import kotlin.math.log2

data class GuitarString(val number: Int, val name: String, val octave: Int, val hz: Float)

enum class Instrument(val label: String, val strings: List<GuitarString>) {
    // De la 6ª (grave) a la 1ª (aguda).
    GUITARRA(
        "Guitarra",
        listOf(
            GuitarString(6, "E", 2, 82.41f),
            GuitarString(5, "A", 2, 110.00f),
            GuitarString(4, "D", 3, 146.83f),
            GuitarString(3, "G", 3, 196.00f),
            GuitarString(2, "B", 3, 246.94f),
            GuitarString(1, "E", 4, 329.63f),
        ),
    ),

    // Bandurria española: seis órdenes dobles en cuartas.
    BANDURRIA(
        "Bandurria",
        listOf(
            GuitarString(6, "G♯", 3, 207.65f),
            GuitarString(5, "C♯", 4, 277.18f),
            GuitarString(4, "F♯", 4, 369.99f),
            GuitarString(3, "B", 4, 493.88f),
            GuitarString(2, "E", 5, 659.26f),
            GuitarString(1, "A", 5, 880.00f),
        ),
    ),
}

object Tuning {
    const val IN_TUNE_CENTS = 4f

    fun cents(hz: Float, target: Float): Float = 1200f * log2(hz / target)

    fun closest(hz: Float, strings: List<GuitarString>): GuitarString =
        strings.minBy { abs(cents(hz, it.hz)) }
}
