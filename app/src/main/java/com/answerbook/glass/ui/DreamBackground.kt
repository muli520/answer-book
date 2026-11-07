package com.answerbook.glass.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.answerbook.glass.ui.theme.BookColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

private data class Dust(val x: Float, val y: Float, val radius: Float, val alpha: Float, val phase: Float)
private data class Speck(val x: Float, val y: Float, val alpha: Float)

@Composable
fun DreamBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "dream")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 26_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )
    val breath by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "breath"
    )
    val twinkle by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "twinkle"
    )

    val dust = remember {
        val random = Random(731)
        List(110) {
            Dust(
                x = random.nextFloat(),
                y = random.nextFloat(),
                radius = 0.45f + random.nextFloat() * 1.25f,
                alpha = 0.14f + random.nextFloat() * 0.45f,
                phase = random.nextFloat() * (2f * PI).toFloat()
            )
        }
    }
    val specks = remember {
        val random = Random(1207)
        List(460) {
            Speck(
                x = random.nextFloat(),
                y = random.nextFloat(),
                alpha = 0.012f + random.nextFloat() * 0.035f
            )
        }
    }

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val minDim = minOf(w, h)
        val maxDim = max(w, h)

        drawRect(
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0A0910),
                    Color(0xFF0D0B16),
                    Color(0xFF080C11)
                )
            )
        )

        // Warm light from the upper left.
        val warmCenter = Offset(w * (0.12f + 0.06f * drift), h * (0.12f - 0.02f * drift))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(BookColors.Gold.copy(alpha = 0.20f), Color.Transparent),
                center = warmCenter,
                radius = w * 0.88f
            ),
            radius = w * 0.88f,
            center = warmCenter
        )

        // Violet nebula drifting across the right.
        val violetCenter = Offset(w * (0.94f - 0.05f * drift), h * (0.32f + 0.05f * drift))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF7355B7).copy(alpha = 0.22f), Color.Transparent),
                center = violetCenter,
                radius = w * 0.76f
            ),
            radius = w * 0.76f,
            center = violetCenter
        )

        // Teal at the lower left, rose at the lower right.
        val tealCenter = Offset(w * (0.02f + 0.04f * drift), h * (0.78f - 0.03f * drift))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF2F8580).copy(alpha = 0.14f), Color.Transparent),
                center = tealCenter,
                radius = w * 0.62f
            ),
            radius = w * 0.62f,
            center = tealCenter
        )
        val roseCenter = Offset(w * (0.88f - 0.03f * drift), h * 0.92f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFC47463).copy(alpha = 0.11f), Color.Transparent),
                center = roseCenter,
                radius = w * 0.58f
            ),
            radius = w * 0.58f,
            center = roseCenter
        )

        // A cool wash behind the book so the glass has something to bend.
        val centerWash = Offset(w * 0.42f, h * 0.5f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF6D57A8).copy(alpha = 0.075f), Color.Transparent),
                center = centerWash,
                radius = w * 0.66f
            ),
            radius = w * 0.66f,
            center = centerWash
        )

        // A quiet halo behind the book.
        val halo = Offset(w * 0.5f, h * (0.48f + 0.008f * sin(breath)))
        val haloPulse = 0.085f + 0.026f * (0.5f + 0.5f * sin(breath))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(BookColors.Gold.copy(alpha = haloPulse), Color.Transparent),
                center = halo,
                radius = w * 0.58f
            ),
            radius = w * 0.58f,
            center = halo
        )

        // Fine orbital lines.
        val orbit = Offset(w * 0.84f, h * 0.16f)
        drawCircle(
            color = BookColors.Gold.copy(alpha = 0.06f),
            radius = w * 0.46f,
            center = orbit,
            style = Stroke(width = 0.7.dp.toPx())
        )
        drawCircle(
            color = BookColors.Gold.copy(alpha = 0.035f),
            radius = w * 0.56f,
            center = orbit,
            style = Stroke(width = 0.5.dp.toPx())
        )
        drawArc(
            color = BookColors.Gold.copy(alpha = 0.09f),
            startAngle = 112f,
            sweepAngle = 104f,
            useCenter = false,
            topLeft = Offset(w * 0.11f, h * 0.19f),
            size = Size(minDim * 0.78f, minDim * 0.78f),
            style = Stroke(width = 0.6.dp.toPx())
        )

        // Dust.
        for (particle in dust) {
            val x = particle.x * w
            val y = particle.y * h
            val flicker = 0.55f + 0.45f * sin(twinkle + particle.phase)
            val color = if (particle.phase > 4.6f) BookColors.Gold else Color(0xFFF4F0E8)
            drawCircle(
                color = color.copy(alpha = particle.alpha * flicker),
                radius = particle.radius.dp.toPx(),
                center = Offset(x, y)
            )
        }

        // Three four-pointed stars.
        drawFourPointStar(
            center = Offset(w * 0.79f, h * 0.24f),
            radius = 7.5.dp.toPx(),
            color = BookColors.GoldBright.copy(alpha = 0.6f + 0.18f * sin(breath))
        )
        drawFourPointStar(
            center = Offset(w * 0.17f, h * 0.69f),
            radius = 4.3.dp.toPx(),
            color = BookColors.Ivory.copy(alpha = 0.32f + 0.13f * sin(breath + 1.7f))
        )
        drawFourPointStar(
            center = Offset(w * 0.9f, h * 0.73f),
            radius = 3.2.dp.toPx(),
            color = BookColors.Teal.copy(alpha = 0.24f + 0.1f * sin(breath + 3.4f))
        )

        // Film grain, kept extremely faint.
        for (speck in specks) {
            drawCircle(
                color = Color.White.copy(alpha = speck.alpha),
                radius = 0.55f,
                center = Offset(speck.x * w, speck.y * h)
            )
        }

        // Vignette.
        drawRect(
            Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.54f)),
                center = Offset(w * 0.5f, h * 0.44f),
                radius = maxDim * 0.72f
            )
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFourPointStar(
    center: Offset,
    radius: Float,
    color: Color
) {
    val waist = radius * 0.13f
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x + waist, center.y - waist, center.x + radius, center.y)
        quadraticTo(center.x + waist, center.y + waist, center.x, center.y + radius)
        quadraticTo(center.x - waist, center.y + waist, center.x - radius, center.y)
        quadraticTo(center.x - waist, center.y - waist, center.x, center.y - radius)
        close()
    }
    drawPath(path, color = color)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = color.alpha * 0.45f), Color.Transparent),
            center = center,
            radius = radius * 3.1f
        ),
        radius = radius * 3.1f,
        center = center
    )
}
