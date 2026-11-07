package com.answerbook.glass.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object BookColors {
    val Ink = Color(0xFF07070C)
    val InkSoft = Color(0xFF0C0C13)
    val Gold = Color(0xFFE8C48A)
    val GoldBright = Color(0xFFF6DEB4)
    val GoldDim = Color(0xFF9C7B4B)
    val Ivory = Color(0xFFF7F0E3)
    val IvorySoft = Color(0xFFE6DCCB)
    val Muted = Color(0xFF9C948B)
    val Faint = Color(0xFF6E675F)
    val Teal = Color(0xFF79C7BE)
    val Plum = Color(0xFF8C72C9)
    val Rose = Color(0xFFD99A8C)
    val GlassWhite = Color(0xFFFFFFFF)
}

val Serif = FontFamily.Serif
val Sans = FontFamily.SansSerif

object BookType {
    val Wordmark = TextStyle(
        fontFamily = Serif,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 2.sp
    )
    val Kicker = TextStyle(
        fontFamily = Sans,
        fontSize = 9.sp,
        lineHeight = 12.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 3.2.sp
    )
    val Label = TextStyle(
        fontFamily = Sans,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.2.sp
    )
    val Body = TextStyle(
        fontFamily = Sans,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.3.sp
    )
    val Small = TextStyle(
        fontFamily = Sans,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    val Answer = TextStyle(
        fontFamily = Serif,
        fontSize = 31.sp,
        lineHeight = 47.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.6.sp
    )
    val AnswerSmall = TextStyle(
        fontFamily = Serif,
        fontSize = 25.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.2.sp
    )
    val Question = TextStyle(
        fontFamily = Serif,
        fontSize = 19.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.6.sp
    )
    val Field = TextStyle(
        fontFamily = Sans,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.2.sp
    )
    val Page = TextStyle(
        fontFamily = Sans,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 2.4.sp
    )
    val Button = TextStyle(
        fontFamily = Sans,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 2.2.sp
    )
}
