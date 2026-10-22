package com.estebandacosta.jdrassistant

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.estebandacosta.jdrassistant.creation.CreateWorldDialog
import com.estebandacosta.jdrassistant.home.HomeScreen

/** Racine de l'app du MJ : l'accueil, surmonté de la modale de création quand elle est ouverte. */
@Composable
fun DesktopApp() {
    var creatingWorld by remember { mutableStateOf(false) }
    HomeScreen(onCreateWorld = { creatingWorld = true }, onOpenWorld = {})
    if (creatingWorld) {
        CreateWorldDialog(onDismiss = { creatingWorld = false }, onCreate = { creatingWorld = false })
    }
}
