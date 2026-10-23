package com.estebandacosta.jdrassistant.core.value

import arrow.core.Either
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Configuration d'un champ, déclarée par le module. Le catalogue est fermé et défini par le core. */
@Serializable
sealed interface ValueType {
    /** Vérifie que [value] appartient à ce type et respecte ses invariants ; renvoie la première raison de refus. */
    fun validate(value: FieldValue): Either<ValueError, FieldValue>

    /** Valide [op] sur [value] ; refuse si l'une ou l'autre n'appartient pas à ce type. */
    fun decide(value: FieldValue, op: ValueOp): Either<ValueError, ValueOp>

    /**
     * Applique [op] à [value] et renvoie le nouvel état.
     * [op] doit avoir été acceptée par [decide] pour [value] ; sinon lève `IllegalStateException`.
     */
    fun evolve(value: FieldValue, op: ValueOp): FieldValue
}

/** État immuable d'un champ dans une instance. */
@Serializable
sealed interface FieldValue

/** Opération sur l'état d'un champ. */
@Serializable
sealed interface ValueOp

/** Raisons pour lesquelles un [ValueType] refuse une opération. */
@Serializable
sealed interface ValueError {
    /** La valeur n'appartient pas au type du champ. */
    @Serializable
    @SerialName("value.valueMismatch")
    data class ValueMismatch(val value: FieldValue) : ValueError

    /** L'opération n'appartient pas au type du champ. */
    @Serializable
    @SerialName("value.opMismatch")
    data class OpMismatch(val op: ValueOp) : ValueError
}
