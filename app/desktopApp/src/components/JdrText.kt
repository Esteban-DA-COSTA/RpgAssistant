package com.estebandacosta.jdrassistant.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

/** Texte dans un style de `JdrTheme.typography`, teinté de [color]. */
@Composable
fun JdrText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    BasicText(text, modifier, style.copy(color = color), overflow = overflow, maxLines = maxLines)
}
