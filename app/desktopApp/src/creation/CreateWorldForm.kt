package com.estebandacosta.jdrassistant.creation

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.core.raise.zipOrAccumulate
import java.nio.file.Path

/** Saisie en cours dans la modale de création, telle que le MJ l'a tapée. */
data class CreateWorldForm(
    val name: String = "",
    val location: Path? = null,
)

/** Monde prêt à être créé : [name] sans espaces superflus et utilisable comme nom de fichier, dans [location]. */
data class NewWorld(val name: String, val location: Path)

/** Raisons pour lesquelles un [CreateWorldForm] ne donne pas de [NewWorld]. */
sealed interface CreateWorldError {
    /** Erreurs portant sur le nom du monde. */
    sealed interface Name : CreateWorldError

    /** Le nom est vide ou ne contient que des espaces. */
    data object BlankName : Name

    /** Le nom contient des [characters] interdits dans un nom de fichier. */
    data class ForbiddenCharacters(val characters: Set<Char>) : Name

    /** Aucun dossier n'a été choisi. */
    data object MissingLocation : CreateWorldError
}

private const val FORBIDDEN_IN_FILE_NAME = """/\:*?"<>|"""

/** Transforme la saisie en [NewWorld], ou renvoie toutes ses erreurs à la fois. */
fun CreateWorldForm.validate(): Either<NonEmptyList<CreateWorldError>, NewWorld> = either {
    zipOrAccumulate(
        { validName(name) },
        { ensureNotNull(location) { CreateWorldError.MissingLocation } },
        ::NewWorld,
    )
}

private fun Raise<CreateWorldError.Name>.validName(name: String): String {
    val trimmed = name.trim()
    ensure(trimmed.isNotEmpty()) { CreateWorldError.BlankName }
    val forbidden = trimmed.filter { it in FORBIDDEN_IN_FILE_NAME || it.isISOControl() }.toSet()
    ensure(forbidden.isEmpty()) { CreateWorldError.ForbiddenCharacters(forbidden) }
    return trimmed
}
