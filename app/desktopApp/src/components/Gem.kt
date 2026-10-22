package com.estebandacosta.jdrassistant.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.estebandacosta.jdrassistant.theme.JdrTheme

/** Pastille ronde sertie du liseré du thème, éclairée en haut à gauche, portant [icon] ; [colors] va du reflet à l'ombre. */
@Composable
fun Gem(colors: List<Color>, icon: ImageVector, size: Dp, iconSize: Dp, modifier: Modifier = Modifier) {
    val theme = JdrTheme.colors
    Box(
        modifier
            .size(size)
            .border(1.dp, theme.edgeSoft, CircleShape)
            .padding(1.dp)
            .border(2.dp, theme.rim, CircleShape)
            .padding(2.dp)
            .border(2.dp, theme.edgeSoft, CircleShape)
            .padding(2.dp)
            .drawBehind {
                drawCircle(
                    Brush.radialGradient(
                        colors,
                        center = Offset(this.size.width * 0.36f, this.size.height * 0.30f),
                        radius = this.size.minDimension * 0.8f,
                    ),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Image(icon, contentDescription = null, Modifier.size(iconSize))
    }
}
