package com.estebandacosta.jdrassistant.creation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.estebandacosta.jdrassistant.theme.FantasyTheme
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CreateWorldDialogTest {

    @Test
    fun `valider un formulaire vide affiche les deux erreurs sans créer de monde`() = runComposeUiTest {
        var created: Path? = null
        setContent { FantasyTheme { CreateWorldDialog(onDismiss = {}, onCreated = { created = it }) } }

        onNodeWithText("Créer le monde").performClick()

        onNodeWithText("Donnez un nom à votre monde.").assertExists()
        onNodeWithText("Choisissez le dossier où enregistrer le monde.").assertExists()
        assertEquals(null, created)
    }

    @Test
    fun `saisir un nom efface l'erreur de nom`() = runComposeUiTest {
        setContent { FantasyTheme { CreateWorldDialog(onDismiss = {}, onCreated = {}) } }
        onNodeWithText("Créer le monde").performClick()

        onNode(hasSetTextAction()).performTextInput("Valdor")

        onNodeWithText("Donnez un nom à votre monde.").assertDoesNotExist()
        onNodeWithText("Choisissez le dossier où enregistrer le monde.").assertExists()
    }

    @Test
    fun `un nom avec des caractères interdits affiche lesquels`() = runComposeUiTest {
        setContent { FantasyTheme { CreateWorldDialog(onDismiss = {}, onCreated = {}) } }
        onNode(hasSetTextAction()).performTextInput("Valdor/Est ?")

        onNodeWithText("Créer le monde").performClick()

        onNodeWithText("Un nom de fichier ne peut pas contenir : / ?").assertExists()
    }
}
