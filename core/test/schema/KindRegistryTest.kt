package com.estebandacosta.jdrassistant.core.schema

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.estebandacosta.jdrassistant.core.ElementId
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.element.Element
import com.estebandacosta.jdrassistant.core.element.ElementError.InvalidOperation
import com.estebandacosta.jdrassistant.core.element.FieldOp
import com.estebandacosta.jdrassistant.core.value.ResourceError.NegativeAmount
import com.estebandacosta.jdrassistant.core.value.ResourceOp.Damage
import com.estebandacosta.jdrassistant.core.value.ResourceOp.Heal
import com.estebandacosta.jdrassistant.core.value.ResourceType
import com.estebandacosta.jdrassistant.core.value.ResourceValue
import com.estebandacosta.jdrassistant.core.world.Operation
import com.estebandacosta.jdrassistant.core.world.World
import com.estebandacosta.jdrassistant.core.world.WorldError.InvalidOnElement
import com.estebandacosta.jdrassistant.core.world.WorldError.UnknownElement
import com.estebandacosta.jdrassistant.core.world.WorldError.UnknownKind
import kotlin.test.Test
import kotlin.test.assertEquals

class KindRegistryTest {

    private val pcId = KindId("test:pc")
    private val itemId = KindId("test:magic-item")
    private val registry = KindRegistry(
        listOf(
            Kind(pcId, mapOf("hp" to ResourceType(allowTemp = true))),
            Kind(itemId, mapOf("charges" to ResourceType())),
        ),
    )

    private val thorin = ElementId("thorin")
    private val wand = ElementId("wand")
    private val world = World(
        mapOf(
            thorin to Element(pcId, mapOf("hp" to ResourceValue(current = 12, max = 20))),
            wand to Element(itemId, mapOf("charges" to ResourceValue(current = 5, max = 7))),
        ),
    )

    @Test
    fun `an operation on a known element is accepted`() {
        val op = Operation(thorin, FieldOp("hp", Damage(5)))
        assertEquals(op.right(), registry.decide(world, op))
    }

    @Test
    fun `evolve changes only the targeted element`() {
        val after = registry.evolve(world, Operation(wand, FieldOp("charges", Damage(1))))
        assertEquals(ResourceValue(current = 4, max = 7), after.elements.getValue(wand).values["charges"])
        assertEquals(world.elements[thorin], after.elements[thorin])
    }

    @Test
    fun `an unknown element is refused`() {
        val ghost = ElementId("ghost")
        assertEquals(UnknownElement(ghost).left(), registry.decide(world, Operation(ghost, FieldOp("hp", Damage(1)))))
    }

    @Test
    fun `an element whose kind is not loaded is refused`() {
        val orc = ElementId("orc")
        val withOrc = World(world.elements + (orc to Element(KindId("test:monster"), emptyMap())))
        val expected = UnknownKind(orc, KindId("test:monster"))
        assertEquals(expected.left(), registry.decide(withOrc, Operation(orc, FieldOp("hp", Damage(1)))))
    }

    @Test
    fun `a refusal is reported along the full path`() {
        val expected = InvalidOnElement(thorin, InvalidOperation("hp", NegativeAmount(-5)))
        assertEquals(expected.left(), registry.decide(world, Operation(thorin, FieldOp("hp", Damage(-5)))))
    }

    @Test
    fun `replaying the journal of accepted operations rebuilds the same world`() {
        val incoming = listOf(
            Operation(thorin, FieldOp("hp", Damage(8))),
            Operation(wand, FieldOp("charges", Damage(-1))),
            Operation(wand, FieldOp("charges", Damage(2))),
            Operation(thorin, FieldOp("hp", Heal(3))),
        )

        var live = world
        val journal = mutableListOf<Operation>()
        for (op in incoming) {
            when (val decision = registry.decide(live, op)) {
                is Either.Right -> {
                    live = registry.evolve(live, decision.value)
                    journal += decision.value
                }
                is Either.Left -> {}
            }
        }

        assertEquals(3, journal.size)
        assertEquals(live, journal.fold(world, registry::evolve))
        assertEquals(ResourceValue(current = 7, max = 20), live.elements.getValue(thorin).values["hp"])
        assertEquals(ResourceValue(current = 3, max = 7), live.elements.getValue(wand).values["charges"])
    }
}
