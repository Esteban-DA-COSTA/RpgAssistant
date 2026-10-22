package com.estebandacosta.jdrassistant.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.estebandacosta.jdrassistant.theme.JdrTheme
import com.estebandacosta.jdrassistant.theme.panel

/** Bouton compact précédé d'une gemme, à la hauteur d'un champ de formulaire. */
@Composable
fun GemButton(text: String, gem: List<Color>, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = JdrTheme.colors
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier
            .height(52.dp)
            .panel(colors, shape, elevation = 0.dp)
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand)
            .padding(start = 8.dp, end = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Gem(colors = gem, icon = icon, size = 36.dp, iconSize = 18.dp)
        JdrText(text, JdrTheme.typography.button, colors.textStrong)
    }
}
