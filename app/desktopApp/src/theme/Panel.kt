package com.estebandacosta.jdrassistant.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Cadre de panneau : dégradé de [colors], liseré de bord puis liseré [rim]. */
fun Modifier.panel(colors: JdrColors, shape: Shape, rim: Color = colors.rim): Modifier = this
    .shadow(12.dp, shape)
    .border(1.dp, colors.edge, shape)
    .padding(1.dp)
    .border(1.dp, rim, shape)
    .background(Brush.verticalGradient(listOf(colors.panelTop, colors.panelBottom)), shape)
