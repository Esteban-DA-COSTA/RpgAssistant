package com.estebandacosta.jdrassistant.storage

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.estebandacosta.jdrassistant.creation.NewWorld
import java.io.IOException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject

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

/** Monde lu depuis l'archive [archive]. */
data class OpenedWorld(val name: String, val archive: Path)

/** Raisons pour lesquelles une archive ne peut pas être ouverte comme un monde. */
sealed interface OpenWorldError {
    /** Le fichier n'est pas une archive ZIP. */
    data object NotAnArchive : OpenWorldError

    /** L'archive ne contient pas de `world.json`. */
    data object MissingManifest : OpenWorldError

    /** Le `world.json` n'est pas un manifeste lisible. */
    data object InvalidManifest : OpenWorldError

    /** Le manifeste annonce un format [found] que cette version ne sait pas lire. */
    data class UnsupportedFormat(val found: Int) : OpenWorldError

    /** Le fichier n'a pas pu être lu, pour la raison [cause]. */
    class ReadFailed(val cause: IOException) : OpenWorldError
}

/** Lecture et écriture des mondes sur le disque, chacun dans une archive ZIP `<nom>.rpga`. */
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

    /** Lit l'archive [archive] et renvoie le monde qu'elle contient. */
    suspend fun open(archive: Path): Either<OpenWorldError, OpenedWorld> = withContext(Dispatchers.IO) {
        either {
            val text = try {
                ZipFile(archive.toFile()).use { zip ->
                    val entry = ensureNotNull(zip.getEntry(MANIFEST)) { OpenWorldError.MissingManifest }
                    zip.getInputStream(entry).reader().readText()
                }
            } catch (e: ZipException) {
                raise(OpenWorldError.NotAnArchive)
            } catch (e: IOException) {
                raise(OpenWorldError.ReadFailed(e))
            }
            val tree = try {
                json.parseToJsonElement(text).jsonObject
            } catch (e: IllegalArgumentException) {
                raise(OpenWorldError.InvalidManifest)
            }
            val format = ensureNotNull((tree["format"] as? JsonPrimitive)?.intOrNull) { OpenWorldError.InvalidManifest }
            ensure(format == FORMAT) { OpenWorldError.UnsupportedFormat(format) }
            val manifest = try {
                json.decodeFromJsonElement<WorldManifest>(tree)
            } catch (e: SerializationException) {
                raise(OpenWorldError.InvalidManifest)
            }
            OpenedWorld(manifest.name, archive)
        }
    }
}
