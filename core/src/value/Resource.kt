package com.estebandacosta.jdrassistant.core.value

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure

/**
 * Configuration d'un champ ressource, déclarée par le module et partagée par toutes les instances.
 *
 * @property min plancher de `current` ; peut être négatif.
 * @property allowTemp le champ accepte une réserve temporaire.
 */
data class ResourceType(
    val min: Int = 0,
    val allowTemp: Boolean = false,
) {
    /** Valide [op] ; renvoie l'opération acceptée, ou la raison du refus. */
    fun decide(op: ResourceOp): Either<ResourceError, ResourceOp> = either {
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

    /**
     * Applique [op] à [state] et renvoie le nouvel état.
     * Ne valide rien : [op] doit avoir été acceptée par [decide].
     */
    fun evolve(state: ResourceValue, op: ResourceOp): ResourceValue = when (op) {
        is ResourceOp.Damage -> {
            val fromTemp = minOf(op.amount, state.temp)
            val fromCurrent = op.amount - fromTemp
            state.copy(
                current = (state.current - fromCurrent).coerceAtLeast(min),
                temp = state.temp - fromTemp,
            )
        }
        is ResourceOp.Heal -> state.copy(current = (state.current + op.amount).coerceAtMost(state.max))
        is ResourceOp.SetTemp -> state.copy(temp = op.amount)
        is ResourceOp.SetMax -> state.copy(max = op.max, current = state.current.coerceAtMost(op.max))
        ResourceOp.Reset -> state.copy(current = state.max, temp = 0)
    }
}

/**
 * État d'une ressource consommable (PV, charges, emplacements de sorts).
 * Immuable : chaque opération produit un nouvel état.
 */
data class ResourceValue(
    val current: Int,
    val max: Int,
    val temp: Int = 0,
)

/** Opérations possibles sur une [ResourceValue]. */
sealed interface ResourceOp {
    /** Retire [amount] points, en consommant d'abord le temporaire. */
    data class Damage(val amount: Int) : ResourceOp

    /** Rend [amount] points, sans dépasser le max. */
    data class Heal(val amount: Int) : ResourceOp

    /** Remplace la réserve temporaire par [amount]. */
    data class SetTemp(val amount: Int) : ResourceOp

    /** Fixe le max à [max] ; le courant est ramené au max s'il le dépasse. */
    data class SetMax(val max: Int) : ResourceOp

    /** Remet le courant au max et vide le temporaire. */
    data object Reset : ResourceOp
}

/** Raisons pour lesquelles [ResourceType.decide] refuse une opération. */
sealed interface ResourceError {
    /** Un montant doit être positif ou nul. */
    data class NegativeAmount(val amount: Int) : ResourceError

    /** Le max demandé est sous le plancher du champ. */
    data class MaxBelowMin(val max: Int, val min: Int) : ResourceError

    /** Le champ n'accepte pas de réserve temporaire. */
    data object TempNotAllowed : ResourceError
}
