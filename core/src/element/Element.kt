package com.estebandacosta.jdrassistant.core.element

import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.value.ResourceError
import com.estebandacosta.jdrassistant.core.value.ResourceOp
import com.estebandacosta.jdrassistant.core.value.ResourceValue

/** Instance vivante : l'état de chacun de ses champs, conforme au Kind [kind]. */
data class Element(
    val kind: KindId,
    val values: Map<String, ResourceValue>,
)

/** Opération [op] ciblée sur le champ [field] d'un élément. */
data class FieldOp(
    val field: String,
    val op: ResourceOp,
)

/** Raisons pour lesquelles une [FieldOp] est refusée sur un élément. */
sealed interface ElementError {
    /** Le Kind fourni n'est pas celui de l'élément. */
    data class KindMismatch(val expected: KindId, val actual: KindId) : ElementError

    /** Le Kind ne déclare pas de champ [field]. */
    data class UnknownField(val field: String) : ElementError

    /** L'élément n'a pas de valeur pour le champ déclaré [field]. */
    data class MissingValue(val field: String) : ElementError

    /** Le type du champ [field] refuse l'opération, pour la raison [cause]. */
    data class InvalidOperation(val field: String, val cause: ResourceError) : ElementError
}
