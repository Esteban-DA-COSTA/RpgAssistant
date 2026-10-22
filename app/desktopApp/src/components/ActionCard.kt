package com.estebandacosta.jdrassistant.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.estebandacosta.jdrassistant.theme.FantasyIcons
import com.estebandacosta.jdrassistant.theme.JdrTheme
import com.estebandacosta.jdrassistant.theme.panel

/** Grande carte cliquable : gemme, titre et sous-titre ; [highlighted] la met en avant d'un liseré plus clair. */
@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gem: List<Color>,
    highlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = JdrTheme.colors
    val typography = JdrTheme.typography
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier
            .panel(colors, shape, rim = if (highlighted) colors.rimStrong else colors.rim)
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand)
            .padding(start = 22.dp, top = 22.dp, end = 28.dp, bottom = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Gem(colors = gem, icon = icon, size = 76.dp, iconSize = 38.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            BasicText(
                title,
                style = typography.title.copy(color = if (highlighted) colors.textHighlight else colors.textStrong),
            )
            BasicText(subtitle, style = typography.description.copy(color = colors.textMuted))
        }
        Image(
            FantasyIcons.chevronRight,
            contentDescription = null,
            Modifier.size(22.dp),
            colorFilter = ColorFilter.tint(colors.brand),
        )
    }
}
