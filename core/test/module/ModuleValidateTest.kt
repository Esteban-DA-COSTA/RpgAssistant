package com.estebandacosta.jdrassistant.core.module

import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import com.estebandacosta.jdrassistant.core.ElementId
import com.estebandacosta.jdrassistant.core.FieldKey
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.ModuleId
import com.estebandacosta.jdrassistant.core.element.Element
import com.estebandacosta.jdrassistant.core.element.ElementError.InvalidValue
import com.estebandacosta.jdrassistant.core.module.WorldOpeningError.InvalidElements
import com.estebandacosta.jdrassistant.core.module.WorldOpeningError.ModuleMismatch
import com.estebandacosta.jdrassistant.core.schema.Kind
import com.estebandacosta.jdrassistant.core.schema.KindRegistry
import com.estebandacosta.jdrassistant.core.value.ResourceError.CurrentOutOfRange
import com.estebandacosta.jdrassistant.core.value.ResourceType
import com.estebandacosta.jdrassistant.core.value.ResourceValue
import com.estebandacosta.jdrassistant.core.world.World
import com.estebandacosta.jdrassistant.core.world.WorldError.NonConformingElement
import com.estebandacosta.jdrassistant.core.world.WorldError.UnknownKind
import kotlin.test.Test
import kotlin.test.assertEquals

class ModuleValidateTest {

    private val moduleId = ModuleId("test")
    private val npcId = KindId("test:npc")
    private val module = Module(
        manifest = Manifest(moduleId, "Module de test"),
        kinds = KindRegistry(listOf(Kind(npcId, mapOf(FieldKey("hp") to ResourceType())))),
        compendium = emptyMap(),
    )

    private val gob = ElementId("gob-1")
    private val goblin = Element(npcId, mapOf(FieldKey("hp") to ResourceValue(current = 7, max = 7)))

    @Test
    fun `a world of this module with conforming elements is accepted`() {
        val world = World(moduleId, mapOf(gob to goblin))
        assertEquals(world.right(), module.validate(world))
    }

    @Test
    fun `a world of another module is refused on that ground alone`() {
        val world = World(ModuleId("pf2e"), mapOf(gob to Element(KindId("pf2e:npc"), emptyMap())))
        assertEquals(ModuleMismatch(expected = ModuleId("pf2e"), actual = moduleId).left(), module.validate(world))
    }

    @Test
    fun `every inconsistent element is reported`() {
        val smaug = ElementId("smaug")
        val world = World(
            moduleId,
            mapOf(
                gob to Element(npcId, mapOf(FieldKey("hp") to ResourceValue(current = 9, max = 7))),
                smaug to Element(KindId("test:dragon"), emptyMap()),
            ),
        )
        val expected = InvalidElements(
            nonEmptyListOf(
                NonConformingElement(gob, nonEmptyListOf(InvalidValue(FieldKey("hp"), CurrentOutOfRange(current = 9, min = 0, max = 7)))),
                UnknownKind(smaug, KindId("test:dragon")),
            ),
        )
        assertEquals(expected.left(), module.validate(world))
    }
}
