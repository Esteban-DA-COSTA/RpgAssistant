package com.estebandacosta.jdrassistant.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.estebandacosta.jdrassistant.components.ActionCard
import com.estebandacosta.jdrassistant.components.AppHeader
import com.estebandacosta.jdrassistant.components.Diamond
import com.estebandacosta.jdrassistant.components.JdrText
import com.estebandacosta.jdrassistant.theme.FantasyIcons
import com.estebandacosta.jdrassistant.theme.JdrTheme

/** Écran d'accueil du MJ : créer un nouveau monde ou en ouvrir un existant. */
@Composable
fun HomeScreen(onCreateWorld: () -> Unit, onOpenWorld: () -> Unit) {
    val colors = JdrTheme.colors
    Column(
        Modifier
            .fillMaxSize()
            .background(colors.ground)
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        0.5f to Color.Transparent,
                        1f to colors.vignette.copy(alpha = 0.75f),
                        radius = size.maxDimension * 0.75f,
                    ),
                )
            },
    ) {
        AppHeader()
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Box(Modifier.widthIn(max = 1200.dp).fillMaxSize()) {
                CompassRose(Modifier.align(Alignment.TopEnd).offset(x = (-8).dp, y = (-18).dp).size(460.dp))
                Column(
                    Modifier.fillMaxWidth().padding(start = 48.dp, end = 48.dp, top = 52.dp),
                    verticalArrangement = Arrangement.spacedBy(44.dp),
                ) {
                    Welcome()
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        ActionCard(
                            title = "Créer un monde",
                            subtitle = "Partir d'une page blanche.",
                            icon = FantasyIcons.star,
                            gem = JdrTheme.gems.primary,
                            highlighted = true,
                            onClick = onCreateWorld,
                            modifier = Modifier.weight(1f),
                        )
                        ActionCard(
                            title = "Ouvrir un monde",
                            subtitle = "Reprendre un monde enregistré.",
                            icon = FantasyIcons.book,
                            gem = JdrTheme.gems.secondary,
                            highlighted = false,
                            onClick = onOpenWorld,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
        Footer()
    }
}

@Composable
private fun Welcome() {
    val colors = JdrTheme.colors
    val typography = JdrTheme.typography
    val shadow = with(LocalDensity.current) { Shadow(Color.Black, Offset(0f, 2.dp.toPx()), 4.dp.toPx()) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Diamond(colors.brand, 8.dp)
            JdrText("Bienvenue, maître du jeu", typography.eyebrow.copy(shadow = shadow), colors.textStrong)
        }
        JdrText("Quel monde ouvrons-nous ce soir ?", typography.display.copy(shadow = shadow), colors.textHighlight)
    }
}

/** Rose des vents décorative, en filigrane derrière le titre. */
@Composable
private fun CompassRose(modifier: Modifier) {
    val ornament = JdrTheme.colors.ornament
    Canvas(modifier) {
        val unit = size.minDimension / 600f
        val c = center
        val spike = Path().apply {
            moveTo(c.x, c.y - 280 * unit)
            lineTo(c.x + 14 * unit, c.y - 70 * unit)
            lineTo(c.x, c.y - 48 * unit)
            lineTo(c.x - 14 * unit, c.y - 70 * unit)
            close()
        }
        repeat(12) { i ->
            rotate(i * 30f, c) { drawPath(spike, ornament.copy(alpha = 0.45f)) }
        }
        val ring = ornament.copy(alpha = 0.6f)
        drawCircle(ring, 128 * unit, c, style = Stroke(2 * unit))
        drawCircle(ring, 150 * unit, c, style = Stroke(1 * unit))
        drawCircle(ring, 205 * unit, c, style = Stroke(1.5f * unit))
    }
}

@Composable
private fun Footer() {
    val colors = JdrTheme.colors
    Box(
        Modifier
            .fillMaxWidth()
            .background(colors.bar)
            .drawBehind {
                val stroke = 1.dp.toPx()
                drawLine(colors.rimDim, Offset(0f, stroke / 2), Offset(size.width, stroke / 2), stroke)
            }
            .padding(horizontal = 48.dp, vertical = 14.dp),
    ) {
        JdrText("Les joueurs se connectent depuis l'app mobile.", JdrTheme.typography.footnote, colors.textMuted)
    }
}
