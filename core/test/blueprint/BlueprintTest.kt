package com.estebandacosta.jdrassistant.core.blueprint

import arrow.core.right
import com.estebandacosta.jdrassistant.core.BlueprintId
import com.estebandacosta.jdrassistant.core.ElementId
import com.estebandacosta.jdrassistant.core.FieldKey
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.ModuleId
import com.estebandacosta.jdrassistant.core.element.Element
import com.estebandacosta.jdrassistant.core.element.FieldOp
import com.estebandacosta.jdrassistant.core.schema.Kind
import com.estebandacosta.jdrassistant.core.schema.KindRegistry
import com.estebandacosta.jdrassistant.core.value.FlagType
import com.estebandacosta.jdrassistant.core.value.FlagValue
import com.estebandacosta.jdrassistant.core.value.ResourceOp.Damage
import com.estebandacosta.jdrassistant.core.value.ResourceType
import com.estebandacosta.jdrassistant.core.value.ResourceValue
import com.estebandacosta.jdrassistant.core.world.Operation
import com.estebandacosta.jdrassistant.core.world.World
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class BlueprintTest {

    private val npcId = KindId("test:npc")
    private val emptyWorld = World(ModuleId("test"))
    private val registry = KindRegistry(
        listOf(Kind(npcId, mapOf(FieldKey("hp") to ResourceType(), FieldKey("fleeing") to FlagType))),
    )
    private val goblin = Blueprint(
        id = BlueprintId("test:goblin"),
        kind = npcId,
        values = mapOf(FieldKey("hp") to ResourceValue(current = 7, max = 7), FieldKey("fleeing") to FlagValue(false)),
    )

    @Test
    fun `a blueprint decodes from its compendium declaration`() {
        val json = """
            {
              "id": "test:goblin",
              "kind": "test:npc",
              "values": {
                "hp": { "type": "resource", "current": 7, "max": 7 },
                "fleeing": { "type": "flag", "on": false }
              }
            }
        """
        assertEquals(goblin, Json.decodeFromString<Blueprint>(json))
    }

    @Test
    fun `a blueprint instantiates into an element of its kind with its values`() {
        assertEquals(Element(npcId, goblin.values), goblin.toElement())
    }

    @Test
    fun `each instance evolves independently of the others`() {
        val first = ElementId("gob-1")
        val second = ElementId("gob-2")
        val journal = listOf(
            Operation.CreateElement(first, goblin.toElement()),
            Operation.CreateElement(second, goblin.toElement()),
            Operation.UpdateField(first, FieldOp(FieldKey("hp"), Damage(5))),
        )

        val world = journal.fold(emptyWorld, registry::evolve)

        assertEquals(ResourceValue(current = 2, max = 7), world.elements.getValue(first).values[FieldKey("hp")])
        assertEquals(ResourceValue(current = 7, max = 7), world.elements.getValue(second).values[FieldKey("hp")])
        assertEquals(ResourceValue(current = 7, max = 7), goblin.values[FieldKey("hp")])
    }

    @Test
    fun `an instantiated element is created by the existing operation`() {
        val create = Operation.CreateElement(ElementId("gob-1"), goblin.toElement())
        assertEquals(create.right(), registry.decide(emptyWorld, create))
    }
}
