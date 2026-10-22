package com.estebandacosta.jdrassistant.core.world

import com.estebandacosta.jdrassistant.core.ElementId
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.element.Element
import com.estebandacosta.jdrassistant.core.element.ElementError
import com.estebandacosta.jdrassistant.core.element.FieldOp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** État de la campagne : ses éléments, indexés par identifiant. */
data class World(
    val elements: Map<ElementId, Element> = emptyMap(),
)

/** Opération [fieldOp] ciblée sur l'élément [target] ; unité échangée entre clients et serveur, puis journalisée. */
@Serializable
data class Operation(
    val target: ElementId,
    val fieldOp: FieldOp,
)

/** Raisons pour lesquelles une [Operation] est refusée sur le monde. */
@Serializable
sealed interface WorldError {
    /** Aucun élément [element] dans le monde. */
    @Serializable
    @SerialName("world.unknownElement")
    data class UnknownElement(val element: ElementId) : WorldError

    /** Le Kind [kind] de l'élément [element] n'est pas chargé. */
    @Serializable
    @SerialName("world.unknownKind")
    data class UnknownKind(val element: ElementId, val kind: KindId) : WorldError

    /** L'élément [element] refuse l'opération, pour la raison [cause]. */
    @Serializable
    @SerialName("world.invalidOnElement")
    data class InvalidOnElement(val element: ElementId, val cause: ElementError) : WorldError
}

/** Réponse du serveur à une [Operation] reçue. */
@Serializable
sealed interface OperationResult {
    /** L'opération [op] est acceptée et appliquée. */
    @Serializable
    @SerialName("operation.accepted")
    data class Accepted(val op: Operation) : OperationResult

    /** L'opération [op] est refusée, pour la raison [reason]. */
    @Serializable
    @SerialName("operation.rejected")
    data class Rejected(val op: Operation, val reason: WorldError) : OperationResult
}
