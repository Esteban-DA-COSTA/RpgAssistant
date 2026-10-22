package com.estebandacosta.jdrassistant.storage

import arrow.core.Either
import arrow.core.raise.either
import com.estebandacosta.jdrassistant.creation.NewWorld
import java.io.IOException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Contenu de l'entrée `world.json` d'une archive de monde. */
@Serializable
data class WorldManifest(val format: Int, val name: String)

/** Raisons pour lesquelles un monde ne peut pas être écrit sur le disque. */
sealed interface WorldStorageError {
    /** Le fichier [archive] existe déjà ; il n'a pas été modifié. */
    data class AlreadyExists(val archive: Path) : WorldStorageError

    /** L'écriture de [archive] a échoué, pour la raison [cause] ; rien n'est laissé sur le disque. */
    class WriteFailed(val archive: Path, val cause: IOException) : WorldStorageError
}

/** Écriture des mondes sur le disque, chacun dans une archive ZIP `<nom>.rpga`. */
object WorldStorage {
    const val EXTENSION = "rpga"
    const val MANIFEST = "world.json"
    const val FORMAT = 1

    private val json = Json { prettyPrint = true }

    /** Crée l'archive de [world] avec son manifeste, puis renvoie le chemin de l'archive. */
    suspend fun create(world: NewWorld): Either<WorldStorageError, Path> = withContext(Dispatchers.IO) {
        either {
            val archive = world.location.resolve("${world.name}.$EXTENSION")
            val manifest = json.encodeToString(WorldManifest(FORMAT, world.name)).toByteArray()
            try {
                ZipOutputStream(archive.outputStream(CREATE_NEW)).use { zip ->
                    zip.putNextEntry(ZipEntry(MANIFEST))
                    zip.write(manifest)
                    zip.closeEntry()
                }
            } catch (e: FileAlreadyExistsException) {
                raise(WorldStorageError.AlreadyExists(archive))
            } catch (e: IOException) {
                archive.deleteIfExists()
                raise(WorldStorageError.WriteFailed(archive, e))
            }
            archive
        }
    }
}
