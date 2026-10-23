package com.estebandacosta.jdrassistant.core.value

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Configuration d'un champ booléen (inspiration, objet harmonisé) ; sans paramètre. */
@Serializable
@SerialName("flag")
data object FlagType : ValueType {
    override fun decide(value: FieldValue, op: ValueOp): Either<ValueError, ValueOp> = either {
        ensure(value is FlagValue) { ValueError.ValueMismatch(value) }
        ensure(op is FlagOp) { ValueError.OpMismatch(op) }
        op
    }

    override fun evolve(value: FieldValue, op: ValueOp): FieldValue {
        check(value is FlagValue && op is FlagOp) { "Opération non validée : $op sur $value" }
        return when (op) {
            is FlagOp.Set -> FlagValue(op.on)
        }
    }
}

/** État d'un champ booléen. */
@Serializable
@SerialName("flag")
data class FlagValue(val on: Boolean) : FieldValue

/** Opérations possibles sur une [FlagValue]. */
@Serializable
sealed interface FlagOp : ValueOp {
    /** Fixe la valeur à [on]. */
    @Serializable
    @SerialName("flag.set")
    data class Set(val on: Boolean) : FlagOp
}
