package com.estebandacosta.jdrassistant.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.estebandacosta.jdrassistant.theme.FantasyIcons
import com.estebandacosta.jdrassistant.theme.JdrTheme

/** Bandeau du haut de l'app desktop : emblème et nom de l'app, sur toute la largeur. */
@Composable
fun AppHeader(modifier: Modifier = Modifier) {
    val colors = JdrTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(colors.panelTop, colors.panelBottom)))
            .drawBehind {
                val stroke = 1.dp.toPx()
                val y = size.height - stroke / 2
                drawLine(colors.rim, Offset(0f, y), Offset(size.width, y), stroke)
            }
            .padding(horizontal = 48.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Gem(colors = JdrTheme.gems.emblem, icon = FantasyIcons.sword, size = 44.dp, iconSize = 24.dp)
        BasicText("Assistant JdR", style = JdrTheme.typography.brand.copy(color = colors.brand))
    }
}
