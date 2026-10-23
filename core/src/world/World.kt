package com.estebandacosta.jdrassistant.core.world

import com.estebandacosta.jdrassistant.core.ElementId
import com.estebandacosta.jdrassistant.core.element.Element
import kotlinx.serialization.Serializable

/** État de la campagne : ses éléments, indexés par identifiant. */
@Serializable
data class World(
    val elements: Map<ElementId, Element> = emptyMap(),
)
