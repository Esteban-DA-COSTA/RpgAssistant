package com.estebandacosta.jdrassistant

import androidx.compose.runtime.staticCompositionLocalOf
import io.github.vinceglb.filekit.dialogs.FileKitDialogParent

/** Fenêtre à laquelle rattacher les sélecteurs natifs ; `null` hors fenêtre, par exemple en test. */
val LocalDialogParent = staticCompositionLocalOf<FileKitDialogParent?> { null }
