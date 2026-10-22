package com.estebandacosta.jdrassistant.world

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.estebandacosta.jdrassistant.storage.OpenedWorld
import com.estebandacosta.jdrassistant.theme.FantasyTheme
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class WorldScreenTest {

    private val valdor = OpenedWorld("Royaumes de Valdor", Path.of("/home/mj/Valdor.rpga"))

    @Test
    fun `affiche le nom du monde ouvert`() = runComposeUiTest {
        setContent { FantasyTheme { WorldScreen(valdor, onClose = {}) } }

        onNodeWithText("Royaumes de Valdor").assertExists()
    }

    @Test
    fun `Fermer le monde ramène à l'accueil`() = runComposeUiTest {
        var closed = 0
        setContent { FantasyTheme { WorldScreen(valdor, onClose = { closed++ }) } }

        onNodeWithText("Fermer le monde").performClick()

        assertEquals(1, closed)
    }
}
