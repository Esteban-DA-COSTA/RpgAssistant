package com.estebandacosta.jdrassistant.storage

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.either
import com.estebandacosta.jdrassistant.core.module.Module
import com.estebandacosta.jdrassistant.core.module.ModuleError
import com.estebandacosta.jdrassistant.core.module.decodeModule
import java.io.IOException
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Raisons pour lesquelles un dossier ne peut pas être chargé comme un module. */
sealed interface ModuleFolderError {
    /** Le fichier obligatoire [name] est absent du dossier. */
    data class MissingFile(val name: String) : ModuleFolderError

    /** [file] n'a pas pu être lu, pour la raison [cause]. */
    class ReadFailed(val file: Path, val cause: IOException) : ModuleFolderError

    /** Le contenu du module est refusé, pour les raisons [errors]. */
    data class Rejected(val errors: NonEmptyList<ModuleError>) : ModuleFolderError
}

/** Module trouvé dans le sous-dossier [folder] du dossier des modules. */
sealed interface InstalledModule {
    val folder: Path

    /** Module chargé et vérifié. */
    data class Loaded(override val folder: Path, val module: Module) : InstalledModule

    /** Dossier refusé, pour la raison [error]. */
    data class Invalid(override val folder: Path, val error: ModuleFolderError) : InstalledModule
}

/** Le dossier des modules [root] n'a pas pu être parcouru, pour la raison [cause]. */
class ScanFailed(val root: Path, val cause: IOException)

/** Lecture des modules sur le disque, chacun dans son dossier. */
object ModuleStorage {
    const val MANIFEST = "module.json"
    const val KINDS = "kinds.json"
    const val COMPENDIUM = "compendium"

    /** Lit chaque sous-dossier de [root], triés par nom ; un dossier [root] absent ne contient aucun module. */
    suspend fun scan(root: Path): Either<ScanFailed, List<InstalledModule>> = withContext(Dispatchers.IO) {
        either {
            if (!root.exists()) return@either emptyList()
            val folders = try {
                root.listDirectoryEntries().filter { it.isDirectory() }.sortedBy { it.name }
            } catch (e: IOException) {
                raise(ScanFailed(root, e))
            }
            folders.map { folder ->
                read(folder).fold({ InstalledModule.Invalid(folder, it) }, { InstalledModule.Loaded(folder, it) })
            }
        }
    }

    /** Lit le module du dossier [folder] ; les fichiers du compendium sont repérés par `compendium/<nom>.json`. */
    suspend fun read(folder: Path): Either<ModuleFolderError, Module> = withContext(Dispatchers.IO) {
        either {
            val manifest = readRequired(folder, MANIFEST)
            val kinds = readRequired(folder, KINDS)
            val compendium = readCompendium(folder.resolve(COMPENDIUM))
            decodeModule(manifest, kinds, compendium).mapLeft(ModuleFolderError::Rejected).bind()
        }
    }

    private fun Raise<ModuleFolderError>.readRequired(folder: Path, name: String): String =
        try {
            folder.resolve(name).readText()
        } catch (e: NoSuchFileException) {
            raise(ModuleFolderError.MissingFile(name))
        } catch (e: IOException) {
            raise(ModuleFolderError.ReadFailed(folder.resolve(name), e))
        }

    private fun Raise<ModuleFolderError>.readCompendium(directory: Path): Map<String, String> {
        if (!directory.exists()) return emptyMap()
        val files = try {
            directory.listDirectoryEntries("*.json").filter { it.isRegularFile() }.sortedBy { it.name }
        } catch (e: IOException) {
            raise(ModuleFolderError.ReadFailed(directory, e))
        }
        return files.associate { file ->
            val text = try {
                file.readText()
            } catch (e: IOException) {
                raise(ModuleFolderError.ReadFailed(file, e))
            }
            "$COMPENDIUM/${file.name}" to text
        }
    }
}
