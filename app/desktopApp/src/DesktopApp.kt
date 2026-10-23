package com.estebandacosta.jdrassistant

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.estebandacosta.jdrassistant.components.JdrToast
import com.estebandacosta.jdrassistant.creation.CreateWorldDialog
import com.estebandacosta.jdrassistant.home.HomeScreen
import com.estebandacosta.jdrassistant.modules.ModulesScreen
import com.estebandacosta.jdrassistant.modules.ModulesState
import com.estebandacosta.jdrassistant.storage.AppFolders
import com.estebandacosta.jdrassistant.storage.ModuleStorage
import com.estebandacosta.jdrassistant.storage.OpenWorldError
import com.estebandacosta.jdrassistant.storage.OpenedWorld
import com.estebandacosta.jdrassistant.storage.WorldStorage
import com.estebandacosta.jdrassistant.world.WorldScreen
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import java.nio.file.Path
import kotlinx.coroutines.launch

/** Écran affiché par l'app du MJ. */
private sealed interface DesktopScreen {
    /** L'accueil, surmonté de la modale de création si [creatingWorld]. */
    data class Home(val creatingWorld: Boolean = false) : DesktopScreen

    /** Le monde [world], ouvert. */
    data class WorldOpened(val world: OpenedWorld) : DesktopScreen

    /** Les modules installés. */
    data object Modules : DesktopScreen
}

/** Racine de l'app du MJ : l'écran courant et les messages d'erreur éphémères. */
@Composable
fun DesktopApp() {
    var screen by remember { mutableStateOf<DesktopScreen>(DesktopScreen.Home()) }
    val toasts = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun open(archive: Path) {
        scope.launch {
            WorldStorage.open(archive).fold(
                { toasts.showSnackbar(openErrorMessage(archive, it)) },
                { screen = DesktopScreen.WorldOpened(it) },
            )
        }
    }

    val worldPicker = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = setOf(WorldStorage.EXTENSION)),
        dialogSettings = FileKitDialogSettings(title = "Ouvrir un monde", parent = LocalDialogParent.current),
    ) { picked ->
        if (picked != null) open(picked.file.toPath())
    }

    Box(Modifier.fillMaxSize()) {
        when (val current = screen) {
            is DesktopScreen.Home -> {
                HomeScreen(
                    onCreateWorld = { screen = DesktopScreen.Home(creatingWorld = true) },
                    onOpenWorld = { worldPicker.launch() },
                    onOpenModules = { screen = DesktopScreen.Modules },
                )
                if (current.creatingWorld) {
                    CreateWorldDialog(
                        onDismiss = { screen = DesktopScreen.Home() },
                        onCreated = { archive ->
                            screen = DesktopScreen.Home()
                            open(archive)
                        },
                    )
                }
            }
            is DesktopScreen.WorldOpened -> WorldScreen(current.world, onClose = { screen = DesktopScreen.Home() })
            DesktopScreen.Modules -> InstalledModules(onBack = { screen = DesktopScreen.Home() })
        }
        SnackbarHost(toasts, Modifier.align(Alignment.BottomCenter).padding(bottom = 72.dp)) { data ->
            JdrToast(data.visuals.message)
        }
    }
}

/** Écran des modules, qui parcourt le dossier des modules à chaque entrée. */
@Composable
private fun InstalledModules(onBack: () -> Unit) {
    val root = remember { AppFolders.modules() }
    var state by remember { mutableStateOf<ModulesState>(ModulesState.Scanning) }
    LaunchedEffect(root) {
        state = ModuleStorage.scan(root).fold(ModulesState::Failed, ModulesState::Scanned)
    }
    ModulesScreen(root, state, onBack)
}

private fun openErrorMessage(archive: Path, error: OpenWorldError): String {
    val file = archive.fileName
    return when (error) {
        OpenWorldError.NotAnArchive -> "« $file » n'est pas un monde : ce n'est pas une archive .rpga."
        OpenWorldError.MissingManifest, OpenWorldError.InvalidManifest -> "« $file » est endommagé : son manifeste est absent ou illisible."
        is OpenWorldError.UnsupportedFormat -> "« $file » utilise le format ${error.found}, que cette version de l'app ne sait pas lire."
        is OpenWorldError.ReadFailed -> "Impossible de lire « $file »."
    }
}
