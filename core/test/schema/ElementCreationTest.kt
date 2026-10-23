package com.estebandacosta.jdrassistant.core.schema

import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import com.estebandacosta.jdrassistant.core.ElementId
import com.estebandacosta.jdrassistant.core.FieldKey
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.element.Element
import com.estebandacosta.jdrassistant.core.element.ElementError.InvalidValue
import com.estebandacosta.jdrassistant.core.element.ElementError.MissingValue
import com.estebandacosta.jdrassistant.core.element.FieldOp
import com.estebandacosta.jdrassistant.core.value.FlagType
import com.estebandacosta.jdrassistant.core.value.FlagValue
import com.estebandacosta.jdrassistant.core.value.ResourceError.CurrentOutOfRange
import com.estebandacosta.jdrassistant.core.value.ResourceOp.Damage
import com.estebandacosta.jdrassistant.core.value.ResourceType
import com.estebandacosta.jdrassistant.core.value.ResourceValue
import com.estebandacosta.jdrassistant.core.world.Operation
import com.estebandacosta.jdrassistant.core.world.World
import com.estebandacosta.jdrassistant.core.world.WorldError.ElementAlreadyExists
import com.estebandacosta.jdrassistant.core.world.WorldError.NonConformingElement
import com.estebandacosta.jdrassistant.core.world.WorldError.UnknownKind
import kotlin.test.Test
import kotlin.test.assertEquals

class ElementCreationTest {

    private val npcId = KindId("test:npc")
    private val registry = KindRegistry(
        listOf(Kind(npcId, mapOf(FieldKey("hp") to ResourceType(), FieldKey("fleeing") to FlagType))),
    )

    private val gob = ElementId("gob-1")
    private val goblin = Element(
        npcId,
        mapOf(FieldKey("hp") to ResourceValue(current = 7, max = 7), FieldKey("fleeing") to FlagValue(false)),
    )
    private val create = Operation.CreateElement(gob, goblin)

    @Test
    fun `a conforming element is created under its id`() {
        assertEquals(create.right(), registry.decide(World(), create))
        assertEquals(World(mapOf(gob to goblin)), registry.evolve(World(), create))
    }

    @Test
    fun `an id already taken is refused`() {
        val world = World(mapOf(gob to goblin))
        assertEquals(ElementAlreadyExists(gob).left(), registry.decide(world, create))
    }

    @Test
    fun `an element of an unknown kind is refused`() {
        val dragon = Operation.CreateElement(ElementId("smaug"), Element(KindId("test:dragon"), emptyMap()))
        assertEquals(UnknownKind(ElementId("smaug"), KindId("test:dragon")).left(), registry.decide(World(), dragon))
    }

    @Test
    fun `a non conforming element is refused with every cause`() {
        val broken = Element(npcId, mapOf(FieldKey("hp") to ResourceValue(current = 9, max = 7)))
        val expected = NonConformingElement(
            gob,
            nonEmptyListOf(
                MissingValue(FieldKey("fleeing")),
                InvalidValue(FieldKey("hp"), CurrentOutOfRange(current = 9, min = 0, max = 7)),
            ),
        )
        assertEquals(expected.left(), registry.decide(World(), Operation.CreateElement(gob, broken)))
    }

    @Test
    fun `a journal that creates then damages an element replays with fold`() {
        val journal = listOf(create, Operation.UpdateField(gob, FieldOp(FieldKey("hp"), Damage(3))))
        val after = journal.fold(World(), registry::evolve)
        assertEquals(ResourceValue(current = 4, max = 7), after.elements.getValue(gob).values[FieldKey("hp")])
    }
}
