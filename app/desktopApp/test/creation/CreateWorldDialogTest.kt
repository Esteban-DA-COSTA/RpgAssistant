package com.estebandacosta.jdrassistant.creation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.estebandacosta.jdrassistant.theme.FantasyTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CreateWorldDialogTest {

    @Test
    fun `valider un formulaire vide affiche les deux erreurs sans créer de monde`() = runComposeUiTest {
        var created: NewWorld? = null
        setContent { FantasyTheme { CreateWorldDialog(onDismiss = {}, onCreate = { created = it }) } }

        onNodeWithText("Créer le monde").performClick()

        onNodeWithText("Donnez un nom à votre monde.").assertExists()
        onNodeWithText("Choisissez le dossier où enregistrer le monde.").assertExists()
        assertEquals(null, created)
    }

    @Test
    fun `saisir un nom efface l'erreur de nom`() = runComposeUiTest {
        setContent { FantasyTheme { CreateWorldDialog(onDismiss = {}, onCreate = {}) } }
        onNodeWithText("Créer le monde").performClick()

        onNode(hasSetTextAction()).performTextInput("Valdor")

        onNodeWithText("Donnez un nom à votre monde.").assertDoesNotExist()
        onNodeWithText("Choisissez le dossier où enregistrer le monde.").assertExists()
    }
}
