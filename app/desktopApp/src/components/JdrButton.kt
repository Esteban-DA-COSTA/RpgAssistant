package com.estebandacosta.jdrassistant.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.estebandacosta.jdrassistant.theme.JdrTheme
import com.estebandacosta.jdrassistant.theme.panel

/** Bouton de formulaire ; [primary] le remplit de la couleur d'accent pour l'action principale. */
@Composable
fun JdrButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false) {
    val colors = JdrTheme.colors
    val shape = RoundedCornerShape(6.dp)
    val frame = if (primary) {
        Modifier.panel(
            colors,
            shape,
            rim = colors.rimStrong,
            fill = Brush.verticalGradient(0f to colors.accentGlow, 0.45f to colors.accent, 1f to colors.accentShadow),
            elevation = 4.dp,
        )
    } else {
        Modifier.panel(colors, shape, rim = colors.rimDim, elevation = 0.dp)
    }
    Box(
        modifier
            .heightIn(min = 50.dp)
            .then(frame)
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand)
            .padding(horizontal = if (primary) 30.dp else 26.dp),
        contentAlignment = Alignment.Center,
    ) {
        JdrText(text, JdrTheme.typography.button, if (primary) colors.onAccent else colors.textMuted)
    }
}
