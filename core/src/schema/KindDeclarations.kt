package com.estebandacosta.jdrassistant.core.schema

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.separateEither
import arrow.core.toNonEmptyListOrNull
import com.estebandacosta.jdrassistant.core.KindId
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

/** Raisons pour lesquelles le contenu de `kinds.json` est refusé. */
sealed interface KindDeclarationError {
    /** Le contenu n'est pas un tableau JSON lisible. */
    data object NotAList : KindDeclarationError

    /** Le Kind à la position [index] est mal formé, pour la raison [reason]. */
    data class Malformed(val index: Int, val reason: String) : KindDeclarationError

    /** Le Kind [kind] est déclaré aux positions [indices]. */
    data class DuplicateKind(val kind: KindId, val indices: List<Int>) : KindDeclarationError
}

/** Décode le contenu de `kinds.json` en registre, ou renvoie toutes les erreurs trouvées. */
fun decodeKinds(text: String): Either<NonEmptyList<KindDeclarationError>, KindRegistry> = either {
    val entries = ensureNotNull(parseList(text)) { nonEmptyListOf(KindDeclarationError.NotAList) }
    val (malformed, kinds) = entries.mapIndexed(::decodeKind).separateEither()
    (malformed + duplicates(kinds)).toNonEmptyListOrNull()?.let { raise(it) }
    KindRegistry(kinds.map { it.value })
}

private fun parseList(text: String): JsonArray? =
    try {
        Json.parseToJsonElement(text) as? JsonArray
    } catch (_: SerializationException) {
        null
    }

private fun decodeKind(index: Int, entry: JsonElement): Either<KindDeclarationError, IndexedValue<Kind>> =
    Either.catchOrThrow<SerializationException, Kind> { Json.decodeFromJsonElement<Kind>(entry) }
        .mapLeft { KindDeclarationError.Malformed(index, it.message.orEmpty()) }
        .map { IndexedValue(index, it) }

private fun duplicates(kinds: List<IndexedValue<Kind>>): List<KindDeclarationError> =
    kinds.groupBy({ it.value.id }, { it.index })
        .filterValues { it.size > 1 }
        .map { (kind, indices) -> KindDeclarationError.DuplicateKind(kind, indices) }
