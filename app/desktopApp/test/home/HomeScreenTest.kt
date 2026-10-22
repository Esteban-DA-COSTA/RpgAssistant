package com.estebandacosta.jdrassistant.home

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.estebandacosta.jdrassistant.theme.FantasyTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class HomeScreenTest {

    @Test
    fun `cliquer sur Créer un monde déclenche la création`() = runComposeUiTest {
        var created = 0
        setContent { FantasyTheme { HomeScreen(onCreateWorld = { created++ }, onOpenWorld = {}) } }

        onNodeWithText("Créer un monde").performClick()

        assertEquals(1, created)
    }

    @Test
    fun `cliquer sur Ouvrir un monde déclenche l'ouverture`() = runComposeUiTest {
        var opened = 0
        setContent { FantasyTheme { HomeScreen(onCreateWorld = {}, onOpenWorld = { opened++ }) } }

        onNodeWithText("Ouvrir un monde").performClick()

        assertEquals(1, opened)
    }
}
