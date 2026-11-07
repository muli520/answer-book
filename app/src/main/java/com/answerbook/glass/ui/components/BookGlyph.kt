package com.answerbook.glass.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp

enum class BookIcon {
    Sliders,
    Close,
    ArrowRight,
    Spark,
    Eye,
    EyeOff,
    Key,
    Check,
    Send,
    Trash
}

@Composable
fun BookGlyph(
    icon: BookIcon,
    tint: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 1.6f
) {
    Canvas(modifier) {
        val s = size.minDimension / 24f
        scale(s, s, pivot = Offset.Zero) {
            drawIcon(icon, tint, strokeWidth)
        }
    }
}

private fun DrawScope.drawIcon(icon: BookIcon, tint: Color, width: Float) {
    val stroke = Stroke(
        width = width,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )

    when (icon) {
        BookIcon.Sliders -> {
            drawLine(tint, Offset(3f, 6f), Offset(21f, 6f), stroke.width, StrokeCap.Round)
            drawLine(tint, Offset(3f, 12f), Offset(21f, 12f), stroke.width, StrokeCap.Round)
            drawLine(tint, Offset(3f, 18f), Offset(21f, 18f), stroke.width, StrokeCap.Round)
            drawCircle(tint, radius = 2.2f, center = Offset(9f, 6f))
            drawCircle(tint, radius = 2.2f, center = Offset(15f, 12f))
            drawCircle(tint, radius = 2.2f, center = Offset(7f, 18f))
        }

        BookIcon.Close -> {
            drawLine(tint, Offset(6f, 6f), Offset(18f, 18f), stroke.width, StrokeCap.Round)
            drawLine(tint, Offset(18f, 6f), Offset(6f, 18f), stroke.width, StrokeCap.Round)
        }

        BookIcon.ArrowRight -> {
            drawLine(tint, Offset(4f, 12f), Offset(19f, 12f), stroke.width, StrokeCap.Round)
            drawLine(tint, Offset(13f, 6f), Offset(19f, 12f), stroke.width, StrokeCap.Round)
            drawLine(tint, Offset(13f, 18f), Offset(19f, 12f), stroke.width, StrokeCap.Round)
        }

        BookIcon.Spark -> {
            val path = Path().apply {
                moveTo(12f, 2.5f)
                quadraticTo(13.1f, 10.9f, 21.5f, 12f)
                quadraticTo(13.1f, 13.1f, 12f, 21.5f)
                quadraticTo(10.9f, 13.1f, 2.5f, 12f)
                quadraticTo(10.9f, 10.9f, 12f, 2.5f)
                close()
            }
            drawPath(path, tint)
        }

        BookIcon.Eye -> {
            val path = Path().apply {
                moveTo(2f, 12f)
                quadraticTo(12f, 3f, 22f, 12f)
                quadraticTo(12f, 21f, 2f, 12f)
                close()
            }
            drawPath(path, tint, style = stroke)
            drawCircle(tint, radius = 3.2f, center = Offset(12f, 12f))
        }

        BookIcon.EyeOff -> {
            val path = Path().apply {
                moveTo(2f, 12f)
                quadraticTo(12f, 3f, 22f, 12f)
                quadraticTo(12f, 21f, 2f, 12f)
                close()
            }
            drawPath(path, tint, style = stroke)
            drawCircle(tint, radius = 3.2f, center = Offset(12f, 12f))
            drawLine(tint, Offset(4f, 4f), Offset(20f, 20f), stroke.width, StrokeCap.Round)
        }

        BookIcon.Key -> {
            drawCircle(tint, radius = 3.7f, center = Offset(8f, 12f), style = stroke)
            drawLine(tint, Offset(11.7f, 12f), Offset(21f, 12f), stroke.width, StrokeCap.Round)
            drawLine(tint, Offset(17f, 12f), Offset(17f, 16f), stroke.width, StrokeCap.Round)
            drawLine(tint, Offset(20f, 12f), Offset(20f, 15f), stroke.width, StrokeCap.Round)
        }

        BookIcon.Check -> {
            drawLine(tint, Offset(4f, 12f), Offset(9f, 17f), stroke.width, StrokeCap.Round)
            drawLine(tint, Offset(9f, 17f), Offset(20f, 6f), stroke.width, StrokeCap.Round)
        }

        BookIcon.Send -> {
            val path = Path().apply {
                moveTo(3f, 11f)
                lineTo(21f, 3f)
                lineTo(14f, 21f)
                lineTo(11f, 13.5f)
                close()
            }
            drawPath(path, tint, style = stroke)
            drawLine(tint, Offset(11f, 13.5f), Offset(21f, 3f), stroke.width, StrokeCap.Round)
        }

        BookIcon.Trash -> {
            drawLine(tint, Offset(4f, 7f), Offset(20f, 7f), stroke.width, StrokeCap.Round)
            drawLine(tint, Offset(9f, 7f), Offset(9f, 4f), stroke.width, StrokeCap.Round)
            drawLine(tint, Offset(9f, 4f), Offset(15f, 4f), stroke.width, StrokeCap.Round)
            drawLine(tint, Offset(15f, 4f), Offset(15f, 7f), stroke.width, StrokeCap.Round)
            drawLine(tint, Offset(6f, 7f), Offset(7.5f, 21f), stroke.width, StrokeCap.Round)
            drawLine(tint, Offset(7.5f, 21f), Offset(16.5f, 21f), stroke.width, StrokeCap.Round)
            drawLine(tint, Offset(16.5f, 21f), Offset(18f, 7f), stroke.width, StrokeCap.Round)
            drawLine(tint, Offset(10f, 10.5f), Offset(10f, 17.5f), stroke.width, StrokeCap.Round)
            drawLine(tint, Offset(14f, 10.5f), Offset(14f, 17.5f), stroke.width, StrokeCap.Round)
        }
    }
}
