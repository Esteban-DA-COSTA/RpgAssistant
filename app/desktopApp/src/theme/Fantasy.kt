package com.estebandacosta.jdrassistant.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** Thème Fantasy : parchemin, bronze et encre. */
@Composable
fun FantasyTheme(content: @Composable () -> Unit) {
    JdrTheme(FantasyColors, FantasyTypography, FantasyGems, content)
}

private val FantasyColors = JdrColors(
    ground = Color(0xFF5E5236),
    vignette = Color(0xFF1A140B),
    panelTop = Color(0xFF36322B),
    panelBottom = Color(0xFF1F1C18),
    edge = Color(0xFF0E0C09),
    edgeSoft = Color(0xFF16130F),
    rim = Color(0xFFB8994F),
    rimStrong = Color(0xFFD9BE77),
    rimDim = Color(0xFF6E5A2E),
    brand = Color(0xFFE2C77E),
    textHighlight = Color(0xFFF4E2A8),
    textStrong = Color(0xFFF1E6C2),
    textMuted = Color(0xFFCDB678),
    ornament = Color(0xFF2E2614),
    bar = Color(0xC714110C),
)

private val Cinzel = FontFamily(
    Font("font/cinzel.ttf", FontWeight.SemiBold),
    Font("font/cinzel.ttf", FontWeight.Bold),
)

private val Alegreya = FontFamily(
    Font("font/alegreya.ttf", FontWeight.Normal),
    Font("font/alegreya_italic.ttf", FontWeight.Normal, FontStyle.Italic),
)

private val FantasyTypography = JdrTypography(
    display = TextStyle(fontFamily = Cinzel, fontWeight = FontWeight.Bold, fontSize = 44.sp, lineHeight = 1.15.em),
    title = TextStyle(fontFamily = Cinzel, fontWeight = FontWeight.Bold, fontSize = 23.sp),
    brand = TextStyle(fontFamily = Cinzel, fontWeight = FontWeight.Bold, fontSize = 20.sp, letterSpacing = 0.06.em),
    eyebrow = TextStyle(fontFamily = Cinzel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 0.12.em),
    description = TextStyle(fontFamily = Alegreya, fontStyle = FontStyle.Italic, fontSize = 16.sp, lineHeight = 1.4.em),
    footnote = TextStyle(fontFamily = Alegreya, fontStyle = FontStyle.Italic, fontSize = 15.sp),
)

private val FantasyGems = JdrGems(
    emblem = listOf(Color(0xFFF4D88A), Color(0xFFB8862F), Color(0xFF4A3210)),
    primary = listOf(Color(0xFFFFE2A8), Color(0xFFF0A04A), Color(0xFFB3361F), Color(0xFF2E0A06)),
    secondary = listOf(Color(0xFFCFF0FF), Color(0xFF4FA6D8), Color(0xFF1B4E7A), Color(0xFF0A1B2C)),
)
