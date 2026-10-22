package com.estebandacosta.jdrassistant.core.value

import arrow.core.Either

/** Configuration d'un champ, déclarée par le module. Le catalogue est fermé et défini par le core. */
sealed interface ValueType {
    /** Valide [op] sur [value] ; refuse si l'une ou l'autre n'appartient pas à ce type. */
    fun decide(value: FieldValue, op: ValueOp): Either<ValueError, ValueOp>

    /**
     * Applique [op] à [value] et renvoie le nouvel état.
     * [op] doit avoir été acceptée par [decide] pour [value] ; sinon lève `IllegalStateException`.
     */
    fun evolve(value: FieldValue, op: ValueOp): FieldValue
}

/** État immuable d'un champ dans une instance. */
sealed interface FieldValue

/** Opération sur l'état d'un champ. */
sealed interface ValueOp

/** Raisons pour lesquelles un [ValueType] refuse une opération. */
sealed interface ValueError {
    /** La valeur n'appartient pas au type du champ. */
    data class ValueMismatch(val value: FieldValue) : ValueError

    /** L'opération n'appartient pas au type du champ. */
    data class OpMismatch(val op: ValueOp) : ValueError
}
