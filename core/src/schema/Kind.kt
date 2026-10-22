package com.estebandacosta.jdrassistant.core.schema

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.element.Element
import com.estebandacosta.jdrassistant.core.element.ElementError
import com.estebandacosta.jdrassistant.core.element.FieldOp
import com.estebandacosta.jdrassistant.core.value.ResourceType

/** Schéma d'un type d'élément déclaré par un module : ses champs et leur type. */
data class Kind(
    val id: KindId,
    val fields: Map<String, ResourceType>,
) {
    /** Valide [fieldOp] sur [element] ; renvoie l'opération acceptée, ou la raison du refus. */
    fun decide(element: Element, fieldOp: FieldOp): Either<ElementError, FieldOp> = either {
        ensure(element.kind == id) { ElementError.KindMismatch(expected = id, actual = element.kind) }
        val type = ensureNotNull(fields[fieldOp.field]) { ElementError.UnknownField(fieldOp.field) }
        ensure(fieldOp.field in element.values) { ElementError.MissingValue(fieldOp.field) }
        type.decide(fieldOp.op).mapLeft { ElementError.InvalidOperation(fieldOp.field, it) }.bind()
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
