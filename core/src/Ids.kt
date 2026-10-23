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

/** Clé d'un champ, unique à l'intérieur de son Kind seulement (ex. `hp`). */
@Serializable
@JvmInline
value class FieldKey(val value: String)

/** Identifiant stable d'un Blueprint, préfixé par son module (ex. `dnd5e:goblin`). */
@Serializable
@JvmInline
value class BlueprintId(val value: String)

/** Identifiant stable d'un module (ex. `dnd5e`), préfixe des ids qu'il déclare. */
@Serializable
@JvmInline
value class ModuleId(val value: String)
