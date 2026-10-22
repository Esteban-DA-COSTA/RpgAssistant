package com.estebandacosta.jdrassistant.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.estebandacosta.jdrassistant.theme.JdrTheme

/** Séparateur horizontal : double filet gravé, coupé en son centre par un losange. */
@Composable
fun Separator(modifier: Modifier = Modifier) {
    val colors = JdrTheme.colors
    val groove = Modifier.height(2.dp).drawBehind {
        val stroke = 1.dp.toPx()
        drawLine(colors.edge, Offset(0f, stroke / 2), Offset(size.width, stroke / 2), stroke)
        drawLine(colors.rimDim, Offset(0f, size.height - stroke / 2), Offset(size.width, size.height - stroke / 2), stroke)
    }
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f).then(groove))
        Diamond(colors.rim, 7.dp)
        Box(Modifier.weight(1f).then(groove))
    }
}
