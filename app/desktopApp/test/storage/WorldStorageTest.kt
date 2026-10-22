package com.estebandacosta.jdrassistant.storage

import arrow.core.right
import com.estebandacosta.jdrassistant.creation.NewWorld
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions
import java.util.zip.ZipFile
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText
import kotlin.io.path.setPosixFilePermissions
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
        parent.setPosixFilePermissions(PosixFilePermissions.fromString("rwx------"))
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
    fun `signale l'échec quand le dossier parent est en lecture seule`() = runBlocking {
        parent.setPosixFilePermissions(PosixFilePermissions.fromString("r-x------"))

        val created = WorldStorage.create(NewWorld("Valdor", parent))

        assertIs<WorldStorageError.WriteFailed>(created.leftOrNull())
        assertFalse(parent.resolve("Valdor.rpga").exists())
    }
}
