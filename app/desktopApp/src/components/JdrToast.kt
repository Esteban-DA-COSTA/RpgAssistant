package com.estebandacosta.jdrassistant.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.estebandacosta.jdrassistant.theme.JdrTheme
import com.estebandacosta.jdrassistant.theme.panel

/** Message d'erreur éphémère dans le cadre du thème, à afficher via un `SnackbarHost`. */
@Composable
fun JdrToast(message: String, modifier: Modifier = Modifier) {
    val colors = JdrTheme.colors
    Row(
        modifier
            .widthIn(max = 640.dp)
            .panel(colors, RoundedCornerShape(8.dp), rim = colors.rimStrong)
            .padding(horizontal = 22.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Diamond(colors.danger, 8.dp)
        JdrText(message, JdrTheme.typography.description, colors.textStrong)
    }
}
