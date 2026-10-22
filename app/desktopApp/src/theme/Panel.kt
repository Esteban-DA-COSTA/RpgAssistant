package com.estebandacosta.jdrassistant.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Cadre de panneau : remplissage [fill], liseré de bord puis liseré [rim], ombre portée [elevation]. */
fun Modifier.panel(
    colors: JdrColors,
    shape: Shape,
    rim: Color = colors.rim,
    fill: Brush = Brush.verticalGradient(listOf(colors.panelTop, colors.panelBottom)),
    elevation: Dp = 12.dp,
): Modifier = this
    .shadow(elevation, shape)
    .border(1.dp, colors.edge, shape)
    .padding(1.dp)
    .border(1.dp, rim, shape)
    .background(fill, shape)

/** Fond d'écran : couleur de sol, assombrie vers les bords par une vignette. */
fun Modifier.vignetteGround(colors: JdrColors): Modifier = this
    .background(colors.ground)
    .drawBehind {
        drawRect(
            Brush.radialGradient(
                0.5f to Color.Transparent,
                1f to colors.vignette.copy(alpha = 0.75f),
                radius = size.maxDimension * 0.75f,
            ),
        )
    }
