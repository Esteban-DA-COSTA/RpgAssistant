package com.estebandacosta.jdrassistant

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.estebandacosta.jdrassistant.theme.FantasyTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DesktopAppTest {

    @Test
    fun `cliquer sur Créer un monde ouvre la modale`() = runComposeUiTest {
        setContent { FantasyTheme { DesktopApp() } }

        onNodeWithText("Créer un monde").performClick()

        onNodeWithText("Nom du monde").assertExists()
    }

    @Test
    fun `Annuler referme la modale`() = runComposeUiTest {
        setContent { FantasyTheme { DesktopApp() } }
        onNodeWithText("Créer un monde").performClick()

        onNodeWithText("Annuler").performClick()

        onNodeWithText("Nom du monde").assertDoesNotExist()
    }

    @Test
    fun `la croix referme la modale`() = runComposeUiTest {
        setContent { FantasyTheme { DesktopApp() } }
        onNodeWithText("Créer un monde").performClick()

        onNodeWithContentDescription("Fermer").performClick()

        onNodeWithText("Nom du monde").assertDoesNotExist()
    }
}
