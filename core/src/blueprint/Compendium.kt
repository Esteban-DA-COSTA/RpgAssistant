package com.estebandacosta.jdrassistant.core.blueprint

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.right
import arrow.core.separateEither
import arrow.core.toNonEmptyListOrNull
import com.estebandacosta.jdrassistant.core.BlueprintId
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.decodeEntries
import com.estebandacosta.jdrassistant.core.element.ElementError
import com.estebandacosta.jdrassistant.core.schema.KindRegistry

/** Emplacement d'une déclaration : le fichier [source] et la position [index] dans sa liste. */
data class Position(val source: String, val index: Int)

/** Raisons pour lesquelles les fichiers du compendium sont refusés. */
sealed interface BlueprintDeclarationError {
    /** Le fichier [source] n'est pas un tableau JSON lisible. */
    data class NotAList(val source: String) : BlueprintDeclarationError

    /** Le Blueprint en [at] est mal formé, pour la raison [reason]. */
    data class Malformed(val at: Position, val reason: String) : BlueprintDeclarationError

    /** Le Blueprint en [at] est d'un Kind [kind] qui n'est pas chargé. */
    data class UnknownKind(val at: Position, val kind: KindId) : BlueprintDeclarationError

    /** Le Blueprint en [at] n'est pas conforme à son Kind, pour les raisons [causes]. */
    data class NonConforming(val at: Position, val causes: NonEmptyList<ElementError>) : BlueprintDeclarationError

    /** Le Blueprint [blueprint] est déclaré aux positions [at]. */
    data class DuplicateBlueprint(val blueprint: BlueprintId, val at: List<Position>) : BlueprintDeclarationError
}

/**
 * Décode les fichiers du compendium, indexés par leur chemin, et vérifie chaque Blueprint contre [kinds].
 * Renvoie les Blueprints par identifiant, ou toutes les erreurs trouvées.
 */
fun decodeCompendium(
    files: Map<String, String>,
    kinds: KindRegistry,
): Either<NonEmptyList<BlueprintDeclarationError>, Map<BlueprintId, Blueprint>> = either {
    val (unreadable, entries) = files.map { (source, text) -> decodeFile(source, text) }.separateEither()
    val (malformed, decoded) = entries.flatten().separateEither()
    val nonConforming = decoded.mapNotNull { (at, blueprint) -> check(at, blueprint, kinds).leftOrNull() }
    (unreadable + malformed + nonConforming + duplicates(decoded)).toNonEmptyListOrNull()?.let { raise(it) }
    decoded.associate { (_, blueprint) -> blueprint.id to blueprint }
}

private fun decodeFile(
    source: String,
    text: String,
): Either<BlueprintDeclarationError, List<Either<BlueprintDeclarationError, Pair<Position, Blueprint>>>> {
    val entries = decodeEntries(text, Blueprint.serializer())
        ?: return BlueprintDeclarationError.NotAList(source).left()
    return entries.mapIndexed { index, entry ->
        val at = Position(source, index)
        entry.mapLeft { BlueprintDeclarationError.Malformed(at, it) }.map { at to it }
    }.right()
}

private fun check(at: Position, blueprint: Blueprint, kinds: KindRegistry): Either<BlueprintDeclarationError, Blueprint> =
    either {
        val kind = ensureNotNull(kinds[blueprint.kind]) { BlueprintDeclarationError.UnknownKind(at, blueprint.kind) }
        kind.validate(blueprint.toElement()).mapLeft { BlueprintDeclarationError.NonConforming(at, it) }.bind()
        blueprint
    }

private fun duplicates(decoded: List<Pair<Position, Blueprint>>): List<BlueprintDeclarationError> =
    decoded.groupBy({ (_, blueprint) -> blueprint.id }, { (at, _) -> at })
        .filterValues { it.size > 1 }
        .map { (id, at) -> BlueprintDeclarationError.DuplicateBlueprint(id, at) }
