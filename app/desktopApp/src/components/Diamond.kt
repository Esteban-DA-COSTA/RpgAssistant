package com.estebandacosta.jdrassistant.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/** Losange plein de côté [size], pour ponctuer un titre ou un séparateur. */
@Composable
fun Diamond(color: Color, size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).rotate(45f).background(color))
}
