package com.estebandacosta.jdrassistant.storage

import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import com.estebandacosta.jdrassistant.core.BlueprintId
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.ModuleId
import com.estebandacosta.jdrassistant.core.blueprint.BlueprintDeclarationError
import com.estebandacosta.jdrassistant.core.module.ModuleError
import com.estebandacosta.jdrassistant.core.schema.KindDeclarationError
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalPathApi::class)
class ModuleStorageTest {

    private val folder: Path = Files.createTempDirectory("module-test")

    private val manifest = """{"id":"test","title":"Module de test"}"""
    private val kinds = """[{"id":"test:npc","fields":{"hp":{"type":"resource"}}}]"""
    private val goblin = """{"id":"test:goblin","kind":"test:npc","values":{"hp":{"type":"resource","current":7,"max":7}}}"""

    @AfterTest
    fun cleanUp() {
        folder.deleteRecursively()
    }

    @Test
    fun `charge un module complet depuis son dossier`() = runBlocking {
        write("module.json" to manifest, "kinds.json" to kinds, "compendium/monsters.json" to "[$goblin]")

        val module = assertNotNull(ModuleStorage.read(folder).getOrNull())

        assertEquals(ModuleId("test"), module.manifest.id)
        assertEquals(setOf(KindId("test:npc")), module.kinds.ids)
        assertEquals(setOf(BlueprintId("test:goblin")), module.compendium.keys)
    }

    @Test
    fun `charge un module sans compendium avec un compendium vide`() = runBlocking {
        write("module.json" to manifest, "kinds.json" to kinds)

        assertEquals(emptyMap(), assertNotNull(ModuleStorage.read(folder).getOrNull()).compendium)
    }

    @Test
    fun `ignore les fichiers du compendium qui ne sont pas du json`() = runBlocking {
        write("module.json" to manifest, "kinds.json" to kinds, "compendium/notes.txt" to "pas un compendium")

        assertEquals(emptyMap(), assertNotNull(ModuleStorage.read(folder).getOrNull()).compendium)
    }

    @Test
    fun `refuse un dossier sans manifeste`() = runBlocking {
        write("kinds.json" to kinds)

        assertEquals(ModuleFolderError.MissingFile("module.json").left(), ModuleStorage.read(folder))
    }

    @Test
    fun `refuse un dossier sans fichier de kinds`() = runBlocking {
        write("module.json" to manifest)

        assertEquals(ModuleFolderError.MissingFile("kinds.json").left(), ModuleStorage.read(folder))
    }

    @Test
    fun `transmet les erreurs du contenu refusé par le core`() = runBlocking {
        write("module.json" to manifest, "kinds.json" to "{}")

        val expected = ModuleFolderError.Rejected(nonEmptyListOf(ModuleError.InKinds(KindDeclarationError.NotAList)))
        assertEquals(expected.left(), ModuleStorage.read(folder))
    }

    @Test
    fun `situe les erreurs du compendium par leur chemin dans le module`() = runBlocking {
        write("module.json" to manifest, "kinds.json" to kinds, "compendium/monsters.json" to "pas du json")

        val notAList = BlueprintDeclarationError.NotAList("compendium/monsters.json")
        val expected = ModuleFolderError.Rejected(nonEmptyListOf(ModuleError.InCompendium(notAList)))
        assertEquals(expected.left(), ModuleStorage.read(folder))
    }

    @Test
    fun `signale un compendium illisible`() = runBlocking<Unit> {
        write("module.json" to manifest, "kinds.json" to kinds, "compendium" to "un fichier, pas un dossier")

        assertIs<ModuleFolderError.ReadFailed>(ModuleStorage.read(folder).leftOrNull())
    }

    @Test
    fun `liste chaque sous-dossier du dossier des modules, triés par nom`() = runBlocking {
        write("zeta/module.json" to manifest, "zeta/kinds.json" to kinds)
        write("alpha/module.json" to manifest, "alpha/kinds.json" to kinds)

        val installed = assertNotNull(ModuleStorage.scan(folder).getOrNull())

        assertEquals(listOf(folder.resolve("alpha"), folder.resolve("zeta")), installed.map { it.folder })
        assertTrue(installed.all { it is InstalledModule.Loaded })
    }

    @Test
    fun `un module refusé est listé sans empêcher le chargement des autres`() = runBlocking<Unit> {
        write("alpha/kinds.json" to kinds)
        write("beta/module.json" to manifest, "beta/kinds.json" to kinds)

        val installed = assertNotNull(ModuleStorage.scan(folder).getOrNull())

        assertEquals(InstalledModule.Invalid(folder.resolve("alpha"), ModuleFolderError.MissingFile("module.json")), installed[0])
        assertIs<InstalledModule.Loaded>(installed[1])
    }

    @Test
    fun `ignore les fichiers posés à la racine du dossier des modules`() = runBlocking {
        write("notes.txt" to "pas un module", "alpha/module.json" to manifest, "alpha/kinds.json" to kinds)

        assertEquals(listOf(folder.resolve("alpha")), ModuleStorage.scan(folder).getOrNull()?.map { it.folder })
    }

    @Test
    fun `un dossier des modules absent ne contient aucun module`() = runBlocking {
        assertEquals(emptyList<InstalledModule>().right(), ModuleStorage.scan(folder.resolve("absent")))
    }

    @Test
    fun `signale un dossier des modules illisible`() = runBlocking<Unit> {
        write("modules" to "un fichier, pas un dossier")

        assertIs<ScanFailed>(ModuleStorage.scan(folder.resolve("modules")).leftOrNull())
    }

    private fun write(vararg files: Pair<String, String>) {
        files.forEach { (path, content) -> folder.resolve(path).createParentDirectories().writeText(content) }
    }
}
