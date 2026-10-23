package com.estebandacosta.jdrassistant.core.world

import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
import com.estebandacosta.jdrassistant.core.ElementId
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.element.Element
import com.estebandacosta.jdrassistant.core.element.ElementError
import com.estebandacosta.jdrassistant.core.element.FieldOp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Modification du monde ; unité échangée entre clients et serveur, puis journalisée. */
@Serializable
sealed interface Operation {
    /** Applique [fieldOp] à l'élément [target]. */
    @Serializable
    @SerialName("operation.updateField")
    data class UpdateField(val target: ElementId, val fieldOp: FieldOp) : Operation

    /** Ajoute [element], avec toutes ses valeurs, sous l'identifiant [id] choisi par l'émetteur. */
    @Serializable
    @SerialName("operation.createElement")
    data class CreateElement(val id: ElementId, val element: Element) : Operation
}

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

    /** L'identifiant [element] est déjà pris dans le monde. */
    @Serializable
    @SerialName("world.elementAlreadyExists")
    data class ElementAlreadyExists(val element: ElementId) : WorldError

    /** L'élément [element] à créer n'est pas conforme à son Kind, pour les raisons [causes]. */
    @Serializable
    @SerialName("world.nonConformingElement")
    data class NonConformingElement(
        val element: ElementId,
        @Serializable(with = NonEmptyListSerializer::class) val causes: NonEmptyList<ElementError>,
    ) : WorldError
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
