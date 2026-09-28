package com.juanito.afinador

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private val Bg = Color(0xFF0E0F11)
private val Surface = Color(0xFF1A1C20)
private val Muted = Color(0xFF6B7078)
private val TextMain = Color(0xFFF2F3F5)
private val Green = Color(0xFF2FD37A)
private val Amber = Color(0xFFFFB020)
private val Red = Color(0xFFFF5A4E)

private fun colorFor(cents: Float): Color = when {
    abs(cents) <= Tuning.IN_TUNE_CENTS -> Green
    abs(cents) <= 15f -> Amber
    else -> Red
}

@Composable
fun TunerScreen(state: TunerState, micGranted: Boolean, onAskMic: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .safeDrawingPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Header(auto = state.locked == null)

        if (!micGranted) {
            Spacer(Modifier.weight(1f))
            Text("Necesito el micrófono para oír la guitarra", color = TextMain, fontSize = 18.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Pill("Dar permiso", Green, onAskMic)
            Spacer(Modifier.weight(1f))
            return@Column
        }

        Spacer(Modifier.weight(0.6f))
        Gauge(state)
        Spacer(Modifier.weight(1f))
        Strings(state)
        Spacer(Modifier.height(20.dp))
        Text(
            "De Literato, para sus amigos de la tuna.\nPorque por más que le pese a Nobita, a veces hay que afinar.",
            color = Muted,
            fontSize = 12.sp,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
        )
    }
}

@Composable
private fun Header(auto: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Afinador", color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Text(
            if (auto) "AUTO" else "MANUAL",
            color = if (auto) Green else Amber,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Surface)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun Gauge(state: TunerState) {
    val live = state.live
    val cents = state.cents
    val needle by animateFloatAsState(
        targetValue = if (live) cents.coerceIn(-50f, 50f) else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 120f),
        label = "needle",
    )
    val accent by animateColorAsState(if (live) colorFor(cents) else Muted, label = "accent")
    val inTune = live && abs(cents) <= Tuning.IN_TUNE_CENTS

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.fillMaxWidth().aspectRatio(2.1f)) {
            val r = size.width * 0.44f
            val c = Offset(size.width / 2, size.height - 12.dp.toPx())
            val arc = Size(r * 2, r * 2)
            val tl = Offset(c.x - r, c.y - r)
            // Arco de -50 a +50 cents = de 210° a 330° (120° en total).
            drawArc(Surface, 210f, 120f, false, tl, arc, style = Stroke(14.dp.toPx(), cap = StrokeCap.Round))
            val greenSpan = 120f * (2 * Tuning.IN_TUNE_CENTS / 100f)
            drawArc(Green.copy(alpha = 0.35f), 270f - greenSpan / 2, greenSpan, false, tl, arc, style = Stroke(14.dp.toPx()))
            for (t in -50..50 step 10) {
                val a = Math.toRadians(270.0 + t * 1.2)
                val len = if (t == 0) 22.dp.toPx() else 12.dp.toPx()
                val o = r - 20.dp.toPx()
                drawLine(
                    if (t == 0) Green else Muted,
                    Offset(c.x + o * cos(a).toFloat(), c.y + o * sin(a).toFloat()),
                    Offset(c.x + (o - len) * cos(a).toFloat(), c.y + (o - len) * sin(a).toFloat()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            val a = Math.toRadians(270.0 + needle * 1.2)
            val tip = Offset(c.x + (r + 4.dp.toPx()) * cos(a).toFloat(), c.y + (r + 4.dp.toPx()) * sin(a).toFloat())
            drawLine(accent, c, tip, strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(accent, 9.dp.toPx(), c)
        }

        Spacer(Modifier.height(8.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.alpha(if (live) 1f else 0.45f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(state.target.name, color = accent, style = TextStyle(fontSize = 96.sp, fontWeight = FontWeight.Bold))
                Text(
                    "${state.target.octave}",
                    color = accent,
                    fontSize = 28.sp,
                    modifier = Modifier.padding(bottom = 18.dp),
                )
            }
            Text(
                when {
                    !live -> "Toca una cuerda"
                    inTune -> "Afinada"
                    cents > 50 -> "Muy alta: afloja"
                    cents < -50 -> "Muy baja: aprieta"
                    cents > 0 -> "Alta: afloja"
                    else -> "Baja: aprieta"
                },
                color = if (live) accent else Muted,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (live) "%+.0f cents · %.1f Hz".format(cents, state.hz ?: 0f) else "%.2f Hz".format(state.target.hz),
                color = Muted,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun Strings(state: TunerState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            if (state.locked == null) "Detecta la cuerda sola. Pulsa una para fijarla." else "Fijada. Púlsala otra vez para volver a AUTO.",
            color = Muted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Tuning.strings.forEach { s ->
                val active = s == state.target && (state.live || state.locked == s)
                val done = s.number in state.tuned
                val ring = when {
                    state.locked == s -> Amber
                    done -> Green
                    else -> Surface
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (active) TextMain else Surface)
                            .border(2.dp, ring, CircleShape)
                            .clickable { state.toggleLock(s) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            s.name,
                            color = if (active) Bg else TextMain,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(if (done) "✓" else "${s.number}ª", color = if (done) Green else Muted, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun Pill(text: String, color: Color, onClick: () -> Unit) {
    Text(
        text,
        color = Bg,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
    )
}
