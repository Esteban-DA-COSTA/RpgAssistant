package com.estebandacosta.jdrassistant.storage

import arrow.core.left
import arrow.core.right
import com.estebandacosta.jdrassistant.creation.NewWorld
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.outputStream
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

@OptIn(ExperimentalPathApi::class)
class WorldStorageTest {

    private val parent: Path = Files.createTempDirectory("rpga-test")

    @AfterTest
    fun cleanUp() {
        parent.deleteRecursively()
    }

    @Test
    fun `crée un unique fichier rpga portant le nom du monde`() = runBlocking {
        val created = WorldStorage.create(NewWorld("Royaumes de Valdor", parent))

        val archive = parent.resolve("Royaumes de Valdor.rpga")
        assertEquals(archive.right(), created)
        assertTrue(archive.isRegularFile())
    }

    @Test
    fun `l'archive contient un manifeste avec le numéro de format et le nom du monde`() = runBlocking {
        WorldStorage.create(NewWorld("Royaumes de Valdor", parent))

        val manifest = ZipFile(parent.resolve("Royaumes de Valdor.rpga").toFile()).use { zip ->
            zip.getInputStream(zip.getEntry("world.json")).reader().readText()
        }
        assertEquals(WorldManifest(format = 1, name = "Royaumes de Valdor"), Json.decodeFromString(manifest))
    }

    @Test
    fun `refuse un monde dont le fichier existe déjà sans toucher à son contenu`() = runBlocking {
        val existing = parent.resolve("Valdor.rpga").also { it.writeText("ne pas écraser") }

        val created = WorldStorage.create(NewWorld("Valdor", parent))

        assertEquals(WorldStorageError.AlreadyExists(existing), created.leftOrNull())
        assertEquals("ne pas écraser", existing.readText())
    }

    @Test
    fun `signale l'échec quand le dossier de destination n'existe pas`() = runBlocking<Unit> {
        val missing = parent.resolve("absent")

        val created = WorldStorage.create(NewWorld("Valdor", missing))

        assertIs<WorldStorageError.WriteFailed>(created.leftOrNull())
        assertFalse(missing.exists())
    }

    @Test
    fun `relit le monde qu'il vient de créer`() = runBlocking {
        val archive = WorldStorage.create(NewWorld("Royaumes de Valdor", parent)).getOrNull()!!

        assertEquals(OpenedWorld("Royaumes de Valdor", archive).right(), WorldStorage.open(archive))
    }

    @Test
    fun `refuse un fichier qui n'est pas une archive`() = runBlocking {
        val notes = parent.resolve("notes.rpga").also { it.writeText("simple texte") }

        assertEquals(OpenWorldError.NotAnArchive.left(), WorldStorage.open(notes))
    }

    @Test
    fun `refuse une archive sans manifeste`() = runBlocking {
        val archive = zip("vide.rpga", "notes.txt" to "rien")

        assertEquals(OpenWorldError.MissingManifest.left(), WorldStorage.open(archive))
    }

    @Test
    fun `refuse un manifeste qui n'est pas du JSON`() = runBlocking {
        val archive = zip("abime.rpga", "world.json" to "pas du json")

        assertEquals(OpenWorldError.InvalidManifest.left(), WorldStorage.open(archive))
    }

    @Test
    fun `refuse un manifeste sans nom`() = runBlocking {
        val archive = zip("anonyme.rpga", "world.json" to """{"format": 1}""")

        assertEquals(OpenWorldError.InvalidManifest.left(), WorldStorage.open(archive))
    }

    @Test
    fun `annonce un format plus récent même s'il ajoute des champs inconnus`() = runBlocking {
        val archive = zip("futur.rpga", "world.json" to """{"format": 2, "name": "Valdor", "id": "abc"}""")

        assertEquals(OpenWorldError.UnsupportedFormat(2).left(), WorldStorage.open(archive))
    }

    @Test
    fun `signale un fichier introuvable`() = runBlocking<Unit> {
        assertIs<OpenWorldError.ReadFailed>(WorldStorage.open(parent.resolve("absent.rpga")).leftOrNull())
    }

    private fun zip(name: String, vararg entries: Pair<String, String>): Path {
        val archive = parent.resolve(name)
        ZipOutputStream(archive.outputStream()).use { zip ->
            entries.forEach { (entry, content) ->
                zip.putNextEntry(ZipEntry(entry))
                zip.write(content.toByteArray())
                zip.closeEntry()
            }
        }
        return archive
    }
}
