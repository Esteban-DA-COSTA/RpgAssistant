package com.estebandacosta.jdrassistant.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/** Couleurs d'un thème, nommées par rôle. */
@Immutable
data class JdrColors(
    val ground: Color,
    val vignette: Color,
    val panelTop: Color,
    val panelBottom: Color,
    val edge: Color,
    val edgeSoft: Color,
    val rim: Color,
    val rimStrong: Color,
    val rimDim: Color,
    val brand: Color,
    val textHighlight: Color,
    val textStrong: Color,
    val textMuted: Color,
    val textFaint: Color,
    val ornament: Color,
    val bar: Color,
    val field: Color,
    val accent: Color,
    val accentGlow: Color,
    val accentShadow: Color,
    val onAccent: Color,
    val danger: Color,
    val scrim: Color,
)

/** Styles de texte d'un thème, sans couleur : l'appelant la choisit dans [JdrColors]. */
@Immutable
data class JdrTypography(
    val display: TextStyle,
    val heading: TextStyle,
    val title: TextStyle,
    val brand: TextStyle,
    val eyebrow: TextStyle,
    val label: TextStyle,
    val button: TextStyle,
    val input: TextStyle,
    val description: TextStyle,
    val footnote: TextStyle,
    val mono: TextStyle,
)

/** Dégradés des gemmes d'un thème, du reflet à l'ombre. */
@Immutable
data class JdrGems(
    val emblem: List<Color>,
    val primary: List<Color>,
    val secondary: List<Color>,
    val tertiary: List<Color>,
)

private val LocalJdrColors = staticCompositionLocalOf<JdrColors> { error("Aucun JdrTheme fourni") }
private val LocalJdrTypography = staticCompositionLocalOf<JdrTypography> { error("Aucun JdrTheme fourni") }
private val LocalJdrGems = staticCompositionLocalOf<JdrGems> { error("Aucun JdrTheme fourni") }

/** Fournit le thème à [content], lu ensuite via [JdrTheme.colors], [JdrTheme.typography] et [JdrTheme.gems]. */
@Composable
fun JdrTheme(colors: JdrColors, typography: JdrTypography, gems: JdrGems, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalJdrColors provides colors,
        LocalJdrTypography provides typography,
        LocalJdrGems provides gems,
        content = content,
    )
}

/** Accès au thème courant ; échoue hors d'un [JdrTheme]. */
object JdrTheme {
    val colors: JdrColors
        @Composable @ReadOnlyComposable get() = LocalJdrColors.current

    val typography: JdrTypography
        @Composable @ReadOnlyComposable get() = LocalJdrTypography.current

    val gems: JdrGems
        @Composable @ReadOnlyComposable get() = LocalJdrGems.current
}
