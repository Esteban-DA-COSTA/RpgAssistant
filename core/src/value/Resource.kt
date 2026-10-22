package com.estebandacosta.jdrassistant.core.value

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Configuration d'un champ ressource, déclarée par le module et partagée par toutes les instances.
 *
 * @property min plancher de `current` ; peut être négatif.
 * @property allowTemp le champ accepte une réserve temporaire.
 */
data class ResourceType(
    val min: Int = 0,
    val allowTemp: Boolean = false,
) : ValueType {
    override fun decide(value: FieldValue, op: ValueOp): Either<ValueError, ValueOp> = either {
        ensure(value is ResourceValue) { ValueError.ValueMismatch(value) }
        ensure(op is ResourceOp) { ValueError.OpMismatch(op) }
        when (op) {
            is ResourceOp.Damage -> ensure(op.amount >= 0) { ResourceError.NegativeAmount(op.amount) }
            is ResourceOp.Heal -> ensure(op.amount >= 0) { ResourceError.NegativeAmount(op.amount) }
            is ResourceOp.SetTemp -> {
                ensure(allowTemp) { ResourceError.TempNotAllowed }
                ensure(op.amount >= 0) { ResourceError.NegativeAmount(op.amount) }
            }
            is ResourceOp.SetMax -> ensure(op.max >= min) { ResourceError.MaxBelowMin(op.max, min) }
            ResourceOp.Reset -> {}
        }
        op
    }

    override fun evolve(value: FieldValue, op: ValueOp): FieldValue {
        check(value is ResourceValue && op is ResourceOp) { "Opération non validée : $op sur $value" }
        return when (op) {
            is ResourceOp.Damage -> {
                val fromTemp = minOf(op.amount, value.temp)
                val fromCurrent = op.amount - fromTemp
                value.copy(
                    current = (value.current - fromCurrent).coerceAtLeast(min),
                    temp = value.temp - fromTemp,
                )
            }
            is ResourceOp.Heal -> value.copy(current = (value.current + op.amount).coerceAtMost(value.max))
            is ResourceOp.SetTemp -> value.copy(temp = op.amount)
            is ResourceOp.SetMax -> value.copy(max = op.max, current = value.current.coerceAtMost(op.max))
            ResourceOp.Reset -> value.copy(current = value.max, temp = 0)
        }
    }
}

/**
 * État d'une ressource consommable (PV, charges, emplacements de sorts).
 * Immuable : chaque opération produit un nouvel état.
 */
@Serializable
@SerialName("resource")
data class ResourceValue(
    val current: Int,
    val max: Int,
    val temp: Int = 0,
) : FieldValue

/** Opérations possibles sur une [ResourceValue]. */
@Serializable
sealed interface ResourceOp : ValueOp {
    /** Retire [amount] points, en consommant d'abord le temporaire. */
    @Serializable
    @SerialName("resource.damage")
    data class Damage(val amount: Int) : ResourceOp

    /** Rend [amount] points, sans dépasser le max. */
    @Serializable
    @SerialName("resource.heal")
    data class Heal(val amount: Int) : ResourceOp

    /** Remplace la réserve temporaire par [amount]. */
    @Serializable
    @SerialName("resource.setTemp")
    data class SetTemp(val amount: Int) : ResourceOp

    /** Fixe le max à [max] ; le courant est ramené au max s'il le dépasse. */
    @Serializable
    @SerialName("resource.setMax")
    data class SetMax(val max: Int) : ResourceOp

    /** Remet le courant au max et vide le temporaire. */
    @Serializable
    @SerialName("resource.reset")
    data object Reset : ResourceOp
}

/** Raisons propres aux ressources pour lesquelles [ResourceType.decide] refuse une opération. */
@Serializable
sealed interface ResourceError : ValueError {
    /** Un montant doit être positif ou nul. */
    @Serializable
    @SerialName("resource.negativeAmount")
    data class NegativeAmount(val amount: Int) : ResourceError

    /** Le max demandé est sous le plancher du champ. */
    @Serializable
    @SerialName("resource.maxBelowMin")
    data class MaxBelowMin(val max: Int, val min: Int) : ResourceError

    /** Le champ n'accepte pas de réserve temporaire. */
    @Serializable
    @SerialName("resource.tempNotAllowed")
    data object TempNotAllowed : ResourceError
}
