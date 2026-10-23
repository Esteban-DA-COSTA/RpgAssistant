package com.estebandacosta.jdrassistant.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.estebandacosta.jdrassistant.theme.JdrTheme
import com.estebandacosta.jdrassistant.theme.panel

/** Bouton de l'en-tête menant à un écran ; [selected] le marque comme l'écran courant, qui n'est plus cliquable. */
@Composable
fun HeaderButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, selected: Boolean = false) {
    val colors = JdrTheme.colors
    val shape = RoundedCornerShape(6.dp)
    val frame = if (selected) {
        Modifier.panel(colors, shape, rim = colors.rim, elevation = 0.dp)
    } else {
        Modifier.background(colors.field, shape).border(1.dp, colors.rimDim, shape).clip(shape)
            .clickable(role = Role.Button, onClick = onClick).pointerHoverIcon(PointerIcon.Hand)
    }
    Row(
        modifier.height(44.dp).then(frame).padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Image(icon, contentDescription = null, Modifier.size(18.dp), colorFilter = ColorFilter.tint(if (selected) colors.textHighlight else colors.brand))
        JdrText(text, JdrTheme.typography.label, if (selected) colors.textHighlight else colors.textMuted)
    }
}
