package com.estebandacosta.jdrassistant.core.element

import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.value.FieldValue
import com.estebandacosta.jdrassistant.core.value.ValueError
import com.estebandacosta.jdrassistant.core.value.ValueOp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Instance vivante : l'état de chacun de ses champs, conforme au Kind [kind]. */
@Serializable
data class Element(
    val kind: KindId,
    val values: Map<String, FieldValue>,
)

/** Opération [op] ciblée sur le champ [field] d'un élément. */
@Serializable
data class FieldOp(
    val field: String,
    val op: ValueOp,
)

/** Raisons pour lesquelles une [FieldOp] est refusée sur un élément. */
@Serializable
sealed interface ElementError {
    /** Le Kind fourni n'est pas celui de l'élément. */
    @Serializable
    @SerialName("element.kindMismatch")
    data class KindMismatch(val expected: KindId, val actual: KindId) : ElementError

    /** Le Kind ne déclare pas de champ [field]. */
    @Serializable
    @SerialName("element.unknownField")
    data class UnknownField(val field: String) : ElementError

    /** L'élément n'a pas de valeur pour le champ déclaré [field]. */
    @Serializable
    @SerialName("element.missingValue")
    data class MissingValue(val field: String) : ElementError

    /** Le type du champ [field] refuse l'opération, pour la raison [cause]. */
    @Serializable
    @SerialName("element.invalidOperation")
    data class InvalidOperation(val field: String, val cause: ValueError) : ElementError

    /** Le type du champ [field] refuse sa valeur, pour la raison [cause]. */
    @Serializable
    @SerialName("element.invalidValue")
    data class InvalidValue(val field: String, val cause: ValueError) : ElementError
}
