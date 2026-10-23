package com.estebandacosta.jdrassistant.core.world

import arrow.core.nonEmptyListOf
import com.estebandacosta.jdrassistant.core.ElementId
import com.estebandacosta.jdrassistant.core.FieldKey
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.ModuleId
import com.estebandacosta.jdrassistant.core.element.Element
import com.estebandacosta.jdrassistant.core.element.ElementError.InvalidOperation
import com.estebandacosta.jdrassistant.core.element.ElementError.MissingValue
import com.estebandacosta.jdrassistant.core.element.ElementError.UnknownField
import com.estebandacosta.jdrassistant.core.element.FieldOp
import com.estebandacosta.jdrassistant.core.schema.Kind
import com.estebandacosta.jdrassistant.core.schema.KindRegistry
import com.estebandacosta.jdrassistant.core.value.FlagOp
import com.estebandacosta.jdrassistant.core.value.ResourceError.NegativeAmount
import com.estebandacosta.jdrassistant.core.value.ResourceOp
import com.estebandacosta.jdrassistant.core.value.ResourceType
import com.estebandacosta.jdrassistant.core.value.ResourceValue
import com.estebandacosta.jdrassistant.core.value.ValueOp
import com.estebandacosta.jdrassistant.core.world.OperationResult.Accepted
import com.estebandacosta.jdrassistant.core.world.OperationResult.Rejected
import com.estebandacosta.jdrassistant.core.world.WorldError.InvalidOnElement
import com.estebandacosta.jdrassistant.core.world.WorldError.NonConformingElement
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OperationJsonTest {

    private val thorin = ElementId("thorin")
    private val gob = ElementId("gob-1")
    private val goblin = Element(KindId("test:npc"), mapOf(FieldKey("hp") to ResourceValue(current = 7, max = 7)))
    private val damage: Operation = Operation.UpdateField(thorin, FieldOp(FieldKey("hp"), ResourceOp.Damage(5)))

    @Test
    fun `an operation encodes with stable type names`() {
        assertEquals(
            """{"type":"operation.updateField","target":"thorin","fieldOp":{"field":"hp","op":{"type":"resource.damage","amount":5}}}""",
            Json.encodeToString(damage),
        )
    }

    @Test
    fun `an operation without parameters encodes as its type alone`() {
        val reset: Operation = Operation.UpdateField(thorin, FieldOp(FieldKey("hp"), ResourceOp.Reset))
        assertEquals(
            """{"type":"operation.updateField","target":"thorin","fieldOp":{"field":"hp","op":{"type":"resource.reset"}}}""",
            Json.encodeToString(reset),
        )
    }

    @Test
    fun `every operation decodes back to itself`() {
        val ops = listOf(
            ResourceOp.Damage(5),
            ResourceOp.Heal(3),
            ResourceOp.SetTemp(4),
            ResourceOp.SetMax(25),
            ResourceOp.Reset,
            FlagOp.Set(true),
        ).map<ValueOp, Operation> { Operation.UpdateField(thorin, FieldOp(FieldKey("field"), it)) } +
            Operation.CreateElement(gob, goblin)

        ops.forEach { assertEquals(it, Json.decodeFromString<Operation>(Json.encodeToString(it))) }
    }

    @Test
    fun `a creation encodes with its complete element`() {
        val create: Operation = Operation.CreateElement(gob, goblin)
        assertEquals(
            """{"type":"operation.createElement","id":"gob-1",""" +
                """"element":{"kind":"test:npc","values":{"hp":{"type":"resource","current":7,"max":7}}}}""",
            Json.encodeToString(create),
        )
    }

    @Test
    fun `a non conforming creation is rejected with every cause`() {
        val result: OperationResult = Rejected(
            op = Operation.CreateElement(gob, goblin),
            reason = NonConformingElement(gob, nonEmptyListOf(MissingValue(FieldKey("fleeing")), UnknownField(FieldKey("mana")))),
        )
        val json = Json.encodeToString(result)

        assertContains(
            json,
            """"causes":[{"type":"element.missingValue","field":"fleeing"},{"type":"element.unknownField","field":"mana"}]""",
        )
        assertEquals(result, Json.decodeFromString<OperationResult>(json))
    }

    @Test
    fun `an unknown operation type is rejected when decoding`() {
        val json = """{"type":"operation.updateField","target":"thorin","fieldOp":{"field":"hp","op":{"type":"resource.explode"}}}"""
        assertFailsWith<SerializationException> { Json.decodeFromString<Operation>(json) }
    }

    @Test
    fun `a rejection carries the full reason`() {
        val result: OperationResult = Rejected(
            op = Operation.UpdateField(thorin, FieldOp(FieldKey("hp"), ResourceOp.Damage(-5))),
            reason = InvalidOnElement(thorin, InvalidOperation(FieldKey("hp"), NegativeAmount(-5))),
        )
        assertEquals(
            """{"type":"operation.rejected",""" +
                """"op":{"type":"operation.updateField","target":"thorin","fieldOp":{"field":"hp","op":{"type":"resource.damage","amount":-5}}},""" +
                """"reason":{"type":"world.invalidOnElement","element":"thorin",""" +
                """"cause":{"type":"element.invalidOperation","field":"hp",""" +
                """"cause":{"type":"resource.negativeAmount","amount":-5}}}}""",
            Json.encodeToString(result),
        )
    }

    @Test
    fun `a server decision reaches the client intact`() {
        val pc = KindId("test:pc")
        val registry = KindRegistry(listOf(Kind(pc, mapOf(FieldKey("hp") to ResourceType()))))
        val world = World(ModuleId("test"), mapOf(thorin to Element(pc, mapOf(FieldKey("hp") to ResourceValue(current = 12, max = 20)))))
        val incoming = listOf(damage, Operation.UpdateField(thorin, FieldOp(FieldKey("hp"), ResourceOp.Damage(-5))))

        val sent: List<OperationResult> = incoming.map { op ->
            registry.decide(world, op).fold({ Rejected(op, it) }, { Accepted(it) })
        }
        val received = sent.map { Json.decodeFromString<OperationResult>(Json.encodeToString(it)) }

        assertEquals(sent, received)
    }
}
