package com.estebandacosta.jdrassistant.creation

import arrow.core.Either
import arrow.core.NonEmptyList
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

/** Monde prêt à être créé : [name] sans espaces superflus, dans le dossier [location]. */
data class NewWorld(val name: String, val location: Path)

/** Raisons pour lesquelles un [CreateWorldForm] ne donne pas de [NewWorld]. */
sealed interface CreateWorldError {
    /** Le nom est vide ou ne contient que des espaces. */
    data object BlankName : CreateWorldError

    /** Aucun dossier n'a été choisi. */
    data object MissingLocation : CreateWorldError
}

/** Transforme la saisie en [NewWorld], ou renvoie toutes ses erreurs à la fois. */
fun CreateWorldForm.validate(): Either<NonEmptyList<CreateWorldError>, NewWorld> = either {
    zipOrAccumulate(
        { name.trim().also { ensure(it.isNotEmpty()) { CreateWorldError.BlankName } } },
        { ensureNotNull(location) { CreateWorldError.MissingLocation } },
        ::NewWorld,
    )
}
