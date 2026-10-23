package com.estebandacosta.jdrassistant.modules

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import arrow.core.nonEmptyListOf
import com.estebandacosta.jdrassistant.core.module.Module
import com.estebandacosta.jdrassistant.core.module.decodeModule
import com.estebandacosta.jdrassistant.storage.InstalledModule
import com.estebandacosta.jdrassistant.storage.ModuleFolderError
import com.estebandacosta.jdrassistant.storage.ScanFailed
import com.estebandacosta.jdrassistant.theme.FantasyTheme
import java.io.IOException
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ModulesScreenTest {

    private val root: Path = Path.of("donnees", "modules")

    private fun module(id: String, title: String): Module = decodeModule(
        """{"id":"$id","title":"$title"}""",
        """[{"id":"$id:npc","fields":{"hp":{"type":"resource"}}}]""",
        emptyMap(),
    ).getOrNull()!!

    @Test
    fun `un module chargé affiche son titre et son statut prêt`() = runComposeUiTest {
        val loaded = InstalledModule.Loaded(root.resolve("dnd5e"), module("dnd5e", "Donjons & Dragons 5e"))
        setContent { FantasyTheme { ModulesScreen(root, ModulesState.Scanned(listOf(loaded)), onBack = {}) } }

        onNodeWithText("Donjons & Dragons 5e").assertExists()
        onNodeWithText("Prêt").assertExists()
    }

    @Test
    fun `un dossier refusé affiche son nom et son nombre d'erreurs`() = runComposeUiTest {
        val invalid = InstalledModule.Invalid(root.resolve("pathfinder-wip"), ModuleFolderError.MissingFile("module.json"))
        setContent { FantasyTheme { ModulesScreen(root, ModulesState.Scanned(listOf(invalid)), onBack = {}) } }

        onNodeWithText("pathfinder-wip").assertExists()
        onNodeWithText("Refusé · 1 erreur").assertExists()
    }

    @Test
    fun `un doublon cite l'autre dossier qui déclare le même identifiant`() = runComposeUiTest {
        val duplicate = InstalledModule.Duplicate(root.resolve("pf2e"), module("pf2e", "Pathfinder 2e"), nonEmptyListOf(root.resolve("pf2e-copie")))
        setContent { FantasyTheme { ModulesScreen(root, ModulesState.Scanned(listOf(duplicate)), onBack = {}) } }

        onNodeWithText("même identifiant que le dossier pf2e-copie", substring = true).assertExists()
        onNodeWithText("Écarté · doublon").assertExists()
    }

    @Test
    fun `une liste vide indique le dossier où déposer les modules`() = runComposeUiTest {
        setContent { FantasyTheme { ModulesScreen(root, ModulesState.Scanned(emptyList()), onBack = {}) } }

        onNodeWithText("Aucun module installé").assertExists()
        onNodeWithText(root.toString()).assertExists()
    }

    @Test
    fun `un dossier des modules illisible est annoncé`() = runComposeUiTest {
        val failed = ModulesState.Failed(ScanFailed(root, IOException("Accès refusé")))
        setContent { FantasyTheme { ModulesScreen(root, failed, onBack = {}) } }

        onNodeWithText("Impossible de lire le dossier des modules").assertExists()
    }

    @Test
    fun `Retour à l'accueil ramène à l'accueil`() = runComposeUiTest {
        var back = 0
        setContent { FantasyTheme { ModulesScreen(root, ModulesState.Scanned(emptyList()), onBack = { back++ }) } }

        onNodeWithText("Retour à l'accueil").performClick()

        assertEquals(1, back)
    }
}
