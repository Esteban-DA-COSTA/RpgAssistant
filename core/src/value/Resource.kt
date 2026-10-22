package com.estebandacosta.jdrassistant.core.value

/**
 * Configuration d'un champ ressource, déclarée par le module et partagée par toutes les instances.
 *
 * @property min plancher de `current` ; peut être négatif.
 */
data class ResourceType(
    val min: Int = 0,
) {
    /** Applique [op] à [state] et renvoie le nouvel état. */
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
