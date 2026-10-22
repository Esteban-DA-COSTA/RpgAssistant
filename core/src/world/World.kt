package com.estebandacosta.jdrassistant.core.world

import com.estebandacosta.jdrassistant.core.ElementId
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.element.Element
import com.estebandacosta.jdrassistant.core.element.ElementError
import com.estebandacosta.jdrassistant.core.element.FieldOp

/** État de la campagne : ses éléments, indexés par identifiant. */
data class World(
    val elements: Map<ElementId, Element> = emptyMap(),
)

/** Opération [fieldOp] ciblée sur l'élément [target] ; unité échangée entre clients et serveur, puis journalisée. */
data class Operation(
    val target: ElementId,
    val fieldOp: FieldOp,
)

/** Raisons pour lesquelles une [Operation] est refusée sur le monde. */
sealed interface WorldError {
    /** Aucun élément [element] dans le monde. */
    data class UnknownElement(val element: ElementId) : WorldError

    /** Le Kind [kind] de l'élément [element] n'est pas chargé. */
    data class UnknownKind(val element: ElementId, val kind: KindId) : WorldError

    /** L'élément [element] refuse l'opération, pour la raison [cause]. */
    data class InvalidOnElement(val element: ElementId, val cause: ElementError) : WorldError
}
