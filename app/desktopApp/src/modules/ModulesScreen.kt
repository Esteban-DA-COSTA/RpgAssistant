package com.estebandacosta.jdrassistant.modules

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.estebandacosta.jdrassistant.components.AppHeader
import com.estebandacosta.jdrassistant.components.Diamond
import com.estebandacosta.jdrassistant.components.Gem
import com.estebandacosta.jdrassistant.components.HeaderButton
import com.estebandacosta.jdrassistant.components.JdrText
import com.estebandacosta.jdrassistant.storage.InstalledModule
import com.estebandacosta.jdrassistant.storage.ModuleFolderError
import com.estebandacosta.jdrassistant.storage.ScanFailed
import com.estebandacosta.jdrassistant.theme.FantasyIcons
import com.estebandacosta.jdrassistant.theme.JdrTheme
import com.estebandacosta.jdrassistant.theme.panel
import com.estebandacosta.jdrassistant.theme.vignetteGround
import java.nio.file.Path

/** État du parcours du dossier des modules affiché par [ModulesScreen]. */
sealed interface ModulesState {
    /** Le dossier est en cours de lecture. */
    data object Scanning : ModulesState

    /** Le dossier a été lu ; [modules] liste ce qu'il contient. */
    data class Scanned(val modules: List<InstalledModule>) : ModulesState

    /** Le dossier n'a pas pu être lu, pour la raison [failure]. */
    data class Failed(val failure: ScanFailed) : ModulesState
}

/** Écran des modules installés dans le dossier [root], selon [state] ; [onBack] ramène à l'accueil. */
@Composable
fun ModulesScreen(root: Path, state: ModulesState, onBack: () -> Unit) {
    val colors = JdrTheme.colors
    val typography = JdrTheme.typography
    Column(Modifier.fillMaxSize().vignetteGround(colors)) {
        AppHeader {
            HeaderButton("Modules", FantasyIcons.modules, onClick = {}, selected = true)
        }
        Box(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 1200.dp).fillMaxWidth().padding(start = 48.dp, end = 48.dp, top = 28.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                BackLink(onBack)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Diamond(colors.brand, 8.dp)
                        JdrText("Bibliothèque du maître du jeu", typography.eyebrow, colors.textStrong)
                    }
                    JdrText("Modules installés", typography.display, colors.textHighlight)
                    JdrText(
                        "Chaque dossier déposé dans le dossier des modules est un système de jeu. " +
                            "Seuls les modules prêts peuvent servir à créer un monde.",
                        typography.description,
                        colors.textMuted,
                    )
                }
                RootFolder(root)
                when (state) {
                    ModulesState.Scanning -> JdrText("Lecture du dossier des modules…", typography.description, colors.textMuted)
                    is ModulesState.Failed -> FailedPanel()
                    is ModulesState.Scanned ->
                        if (state.modules.isEmpty()) {
                            EmptyPanel()
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                state.modules.forEach { ModuleRow(it) }
                            }
                        }
                }
            }
        }
    }
}

@Composable
private fun BackLink(onBack: () -> Unit) {
    val colors = JdrTheme.colors
    Row(
        Modifier
            .heightIn(min = 44.dp)
            .clickable(role = Role.Button, onClick = onBack)
            .pointerHoverIcon(PointerIcon.Hand),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(FantasyIcons.chevronLeft, contentDescription = null, Modifier.size(18.dp), colorFilter = ColorFilter.tint(colors.textMuted))
        JdrText("Retour à l'accueil", JdrTheme.typography.label, colors.textMuted)
    }
}

@Composable
private fun RootFolder(root: Path) {
    val colors = JdrTheme.colors
    val shape = RoundedCornerShape(6.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .background(colors.field, shape)
            .border(1.dp, colors.rimDim, shape)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        JdrText("Dossier des modules", JdrTheme.typography.label, colors.textFaint)
        JdrText(root.toString(), JdrTheme.typography.mono, colors.textStrong)
    }
}

/** Apparence d'une ligne : gemme, liseré et pastille de statut. */
private class RowLook(val gem: List<Color>, val icon: ImageVector, val rim: Color, val status: String, val statusColor: Color)

@Composable
private fun ModuleRow(entry: InstalledModule) {
    val colors = JdrTheme.colors
    val gems = JdrTheme.gems
    val folder = entry.folder.fileName.toString()
    val look = when (entry) {
        is InstalledModule.Loaded -> RowLook(gems.tertiary, FantasyIcons.check, colors.rim, "Prêt", colors.success)
        is InstalledModule.Invalid -> RowLook(gems.danger, FantasyIcons.close, gems.danger[1], refusal(entry.error), colors.danger)
        is InstalledModule.Duplicate -> RowLook(gems.warning, FantasyIcons.copy, gems.warning[1], "Écarté · doublon", colors.warning)
    }
    val shape = RoundedCornerShape(10.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .panel(colors, shape, rim = look.rim)
            .padding(start = 18.dp, top = 18.dp, end = 24.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Gem(colors = look.gem, icon = look.icon, size = 52.dp, iconSize = 24.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            when (entry) {
                is InstalledModule.Loaded -> {
                    ModuleTitle(entry.module.manifest.title, entry.module.manifest.id.value)
                    JdrText("Dossier $folder", JdrTheme.typography.footnote, colors.textMuted)
                }
                is InstalledModule.Invalid -> {
                    JdrText(folder, JdrTheme.typography.mono, colors.textStrong)
                    JdrText("Ce dossier n'a pas pu être chargé comme un module.", JdrTheme.typography.footnote, colors.danger)
                }
                is InstalledModule.Duplicate -> {
                    ModuleTitle(entry.module.manifest.title, entry.module.manifest.id.value)
                    JdrText(
                        "Dossier $folder · même identifiant que ${others(entry)} : retirez ou corrigez l'un d'eux.",
                        JdrTheme.typography.footnote,
                        colors.warning,
                    )
                }
            }
        }
        StatusChip(look.status, look.statusColor, look.gem[1])
    }
}

@Composable
private fun ModuleTitle(title: String, id: String) {
    val colors = JdrTheme.colors
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        JdrText(title, JdrTheme.typography.title, colors.textStrong)
        JdrText(id, JdrTheme.typography.mono, colors.textFaint, Modifier.padding(bottom = 3.dp))
    }
}

@Composable
private fun StatusChip(text: String, textColor: Color, tone: Color) {
    val shape = RoundedCornerShape(50)
    Box(
        Modifier
            .background(tone.copy(alpha = 0.3f), shape)
            .border(1.dp, tone, shape)
            .padding(horizontal = 14.dp, vertical = 5.dp),
    ) {
        JdrText(text, JdrTheme.typography.label, textColor)
    }
}

@Composable
private fun EmptyPanel() {
    val colors = JdrTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .panel(colors, RoundedCornerShape(10.dp), rim = colors.rimDim)
            .padding(horizontal = 32.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Image(FantasyIcons.modules, contentDescription = null, Modifier.size(56.dp), colorFilter = ColorFilter.tint(colors.rim))
        JdrText("Aucun module installé", JdrTheme.typography.heading, colors.textStrong)
        JdrText(
            "Déposez le dossier d'un module dans le dossier des modules, puis revenez sur cet écran.",
            JdrTheme.typography.description.copy(textAlign = TextAlign.Center),
            colors.textMuted,
        )
    }
}

@Composable
private fun FailedPanel() {
    val colors = JdrTheme.colors
    val gems = JdrTheme.gems
    Row(
        Modifier
            .fillMaxWidth()
            .panel(colors, RoundedCornerShape(10.dp), rim = gems.danger[1])
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Gem(colors = gems.danger, icon = FantasyIcons.close, size = 52.dp, iconSize = 24.dp)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            JdrText("Impossible de lire le dossier des modules", JdrTheme.typography.title, colors.textStrong)
            JdrText("Vérifiez que l'app peut lire ce dossier, puis revenez sur cet écran.", JdrTheme.typography.footnote, colors.danger)
        }
    }
}

private fun refusal(error: ModuleFolderError): String {
    val count = when (error) {
        is ModuleFolderError.MissingFile, is ModuleFolderError.ReadFailed -> 1
        is ModuleFolderError.Rejected -> error.errors.size
    }
    return if (count == 1) "Refusé · 1 erreur" else "Refusé · $count erreurs"
}

private fun others(entry: InstalledModule.Duplicate): String {
    val names = entry.others.map { it.fileName.toString() }
    return if (names.size == 1) "le dossier ${names.single()}" else "les dossiers ${names.joinToString(", ")}"
}
