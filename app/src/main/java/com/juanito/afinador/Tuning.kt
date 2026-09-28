package com.juanito.afinador

import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

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

    // Cualquier nota: se calcula la más cercana (temperado, La4 = 440 Hz).
    CROMATICO("Cromático", emptyList()),
}

object Tuning {
    const val IN_TUNE_CENTS = 4f

    fun cents(hz: Float, target: Float): Float = 1200f * log2(hz / target)

    private val NAMES = listOf("C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B")

    fun closest(hz: Float, strings: List<GuitarString>): GuitarString =
        if (strings.isEmpty()) nearestNote(hz) else strings.minBy { abs(cents(hz, it.hz)) }

    /** Nota temperada más cercana; `number` = 0 porque no es una cuerda. */
    fun nearestNote(hz: Float): GuitarString {
        val midi = (69 + 12 * log2(hz / 440f)).roundToInt()
        val noteHz = 440f * 2f.pow((midi - 69) / 12f)
        return GuitarString(0, NAMES[midi.mod(12)], midi / 12 - 1, noteHz)
    }

    fun initialTarget(instrument: Instrument): GuitarString =
        instrument.strings.firstOrNull() ?: GuitarString(0, "A", 4, 440f)
}
