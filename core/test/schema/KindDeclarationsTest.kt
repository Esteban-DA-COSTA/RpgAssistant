package com.estebandacosta.jdrassistant.core.schema

import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import com.estebandacosta.jdrassistant.core.ElementId
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.element.Element
import com.estebandacosta.jdrassistant.core.element.FieldOp
import com.estebandacosta.jdrassistant.core.schema.KindDeclarationError.DuplicateKind
import com.estebandacosta.jdrassistant.core.schema.KindDeclarationError.Malformed
import com.estebandacosta.jdrassistant.core.schema.KindDeclarationError.NotAList
import com.estebandacosta.jdrassistant.core.value.FlagOp
import com.estebandacosta.jdrassistant.core.value.FlagValue
import com.estebandacosta.jdrassistant.core.value.ResourceOp
import com.estebandacosta.jdrassistant.core.value.ResourceValue
import com.estebandacosta.jdrassistant.core.world.Operation
import com.estebandacosta.jdrassistant.core.world.World
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class KindDeclarationsTest {

    private val pc = """{"id":"test:pc","fields":{"hp":{"type":"resource"}}}"""
    private val npc = """{"id":"test:npc","fields":{"fleeing":{"type":"flag"}}}"""
    private val unknownType = """{"id":"test:mage","fields":{"mana":{"type":"mana"}}}"""
    private val missingId = """{"fields":{}}"""

    @Test
    fun `a list of valid kinds builds a registry that knows every kind`() {
        val registry = assertNotNull(decodeKinds("[$pc,$npc]").getOrNull())
        val thorin = ElementId("thorin")
        val gob = ElementId("gob-1")
        val world = World(
            mapOf(
                thorin to Element(KindId("test:pc"), mapOf("hp" to ResourceValue(current = 12, max = 20))),
                gob to Element(KindId("test:npc"), mapOf("fleeing" to FlagValue(false))),
            ),
        )
        val ops = listOf(
            Operation(thorin, FieldOp("hp", ResourceOp.Damage(5))),
            Operation(gob, FieldOp("fleeing", FlagOp.Set(true))),
        )

        ops.forEach { assertEquals(it.right(), registry.decide(world, it)) }
    }

    @Test
    fun `a content that is not a JSON list is reported as such`() {
        listOf(pc, "[$pc", "").forEach {
            assertEquals(nonEmptyListOf(NotAList).left(), decodeKinds(it))
        }
    }

    @Test
    fun `a malformed kind is reported with its position`() {
        val errors = assertNotNull(decodeKinds("[$pc,$unknownType]").leftOrNull())
        assertEquals(1, assertIs<Malformed>(errors.single()).index)
    }

    @Test
    fun `every malformed kind is reported, not just the first`() {
        val errors = assertNotNull(decodeKinds("[$unknownType,$pc,$missingId]").leftOrNull())
        assertEquals(listOf(0, 2), errors.map { assertIs<Malformed>(it).index })
    }

    @Test
    fun `a kind declared twice is reported with both positions`() {
        assertEquals(
            nonEmptyListOf(DuplicateKind(KindId("test:pc"), listOf(0, 2))).left(),
            decodeKinds("[$pc,$npc,$pc]"),
        )
    }

    @Test
    fun `malformed kinds and duplicates are reported together`() {
        val errors = assertNotNull(decodeKinds("[$pc,$unknownType,$pc]").leftOrNull())
        assertEquals(1, assertIs<Malformed>(errors[0]).index)
        assertEquals(DuplicateKind(KindId("test:pc"), listOf(0, 2)), errors[1])
    }
}
