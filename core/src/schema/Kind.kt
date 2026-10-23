package com.estebandacosta.jdrassistant.core.schema

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.core.toNonEmptyListOrNull
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.element.Element
import com.estebandacosta.jdrassistant.core.element.ElementError
import com.estebandacosta.jdrassistant.core.element.FieldOp
import com.estebandacosta.jdrassistant.core.value.ValueType
import kotlinx.serialization.Serializable

/** Schéma d'un type d'élément déclaré par un module : ses champs et leur type. */
@Serializable
data class Kind(
    val id: KindId,
    val fields: Map<String, ValueType>,
) {
    /** Vérifie que [element] est conforme à ce Kind ; renvoie toutes les erreurs de ses champs, ou l'élément. */
    fun validate(element: Element): Either<NonEmptyList<ElementError>, Element> = either {
        ensure(element.kind == id) { nonEmptyListOf(ElementError.KindMismatch(expected = id, actual = element.kind)) }
        // Champs du kind - ceux présents dans l'élément => champs manquants dans l'élément
        val missing = (fields.keys - element.values.keys).map(ElementError::MissingValue)
        // Champs de l'élément - ceux du kind => champs inconnu pour ce kind
        val unknown = (element.values.keys - fields.keys).map(ElementError::UnknownField)
        // Vérification champs par champs
        val invalid = fields.mapNotNull { (field, type) ->
            val value = element.values[field] ?: return@mapNotNull null
            type.validate(value).leftOrNull()?.let { ElementError.InvalidValue(field, it) }
        }
        (missing + unknown + invalid).toNonEmptyListOrNull()?.let { raise(it) }
        element
    }

    /** Valide [fieldOp] sur [element] ; renvoie l'opération acceptée, ou la raison du refus. */
    fun decide(element: Element, fieldOp: FieldOp): Either<ElementError, FieldOp> = either {
        ensure(element.kind == id) { ElementError.KindMismatch(expected = id, actual = element.kind) }
        val type = ensureNotNull(fields[fieldOp.field]) { ElementError.UnknownField(fieldOp.field) }
        val value = ensureNotNull(element.values[fieldOp.field]) { ElementError.MissingValue(fieldOp.field) }
        type.decide(value, fieldOp.op).mapLeft { ElementError.InvalidOperation(fieldOp.field, it) }.bind()
        fieldOp
    }

    /**
     * Applique [fieldOp] à [element] et renvoie le nouvel élément.
     * Ne valide rien : [fieldOp] doit avoir été acceptée par [decide] pour cet élément.
     */
    fun evolve(element: Element, fieldOp: FieldOp): Element {
        val type = fields.getValue(fieldOp.field)
        val value = element.values.getValue(fieldOp.field)
        return element.copy(values = element.values + (fieldOp.field to type.evolve(value, fieldOp.op)))
    }
}
