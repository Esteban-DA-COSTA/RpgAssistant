package com.estebandacosta.jdrassistant.core

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

/** Identifiant stable d'un Kind, préfixé par son module (ex. `dnd5e:pc`). */
@Serializable
@JvmInline
value class KindId(val value: String)

/** Identifiant stable d'un élément dans la campagne. */
@Serializable
@JvmInline
value class ElementId(val value: String)
