package com.estebandacosta.jdrassistant.core.blueprint

import com.estebandacosta.jdrassistant.core.BlueprintId
import com.estebandacosta.jdrassistant.core.FieldKey
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.element.Element
import com.estebandacosta.jdrassistant.core.value.FieldValue
import kotlinx.serialization.Serializable

/** Définition réutilisable d'un élément du Kind [kind] : les valeurs complètes de chacun de ses champs. */
@Serializable
data class Blueprint(
    val id: BlueprintId,
    val kind: KindId,
    val values: Map<FieldKey, FieldValue>,
) {
    /** Nouvel élément, copie des valeurs de ce Blueprint, à créer par `Operation.CreateElement`. */
    fun toElement(): Element = Element(kind, values)
}
