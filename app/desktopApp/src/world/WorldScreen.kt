package com.estebandacosta.jdrassistant.world

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.estebandacosta.jdrassistant.components.AppHeader
import com.estebandacosta.jdrassistant.components.Diamond
import com.estebandacosta.jdrassistant.components.JdrButton
import com.estebandacosta.jdrassistant.components.JdrText
import com.estebandacosta.jdrassistant.storage.OpenedWorld
import com.estebandacosta.jdrassistant.theme.JdrTheme
import com.estebandacosta.jdrassistant.theme.vignetteGround

/** Écran du monde ouvert [world] ; [onClose] le ferme et ramène à l'accueil. */
@Composable
fun WorldScreen(world: OpenedWorld, onClose: () -> Unit) {
    val colors = JdrTheme.colors
    val typography = JdrTheme.typography
    Column(Modifier.fillMaxSize().vignetteGround(colors)) {
        AppHeader()
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 1200.dp).fillMaxWidth().padding(start = 48.dp, end = 48.dp, top = 52.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Diamond(colors.brand, 8.dp)
                    JdrText("Monde ouvert", typography.eyebrow, colors.textStrong)
                }
                JdrText(world.name, typography.display, colors.textHighlight)
                JdrText(world.archive.toString(), typography.mono, colors.textMuted)
                JdrButton("Fermer le monde", onClose, Modifier.padding(top = 32.dp))
            }
        }
    }
}
