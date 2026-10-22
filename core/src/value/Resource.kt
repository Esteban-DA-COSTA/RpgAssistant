package com.estebandacosta.jdrassistant.core.value

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

/** Applique [op] et renvoie le nouvel état. */
fun ResourceValue.apply(op: ResourceOp): ResourceValue = when (op) {
    is ResourceOp.Damage -> {
        val fromTemp = minOf(op.amount, temp)
        val fromCurrent = op.amount - fromTemp
        copy(
            current = (current - fromCurrent).coerceAtLeast(0),
            temp = temp - fromTemp,
        )
    }
    is ResourceOp.Heal -> copy(current = (current + op.amount).coerceAtMost(max))
    is ResourceOp.SetTemp -> copy(temp = op.amount)
    is ResourceOp.SetMax -> copy(max = op.max, current = current.coerceAtMost(op.max))
    ResourceOp.Reset -> copy(current = max, temp = 0)
}
