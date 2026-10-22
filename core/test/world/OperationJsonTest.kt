package com.estebandacosta.jdrassistant.core.world

import com.estebandacosta.jdrassistant.core.ElementId
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.element.Element
import com.estebandacosta.jdrassistant.core.element.ElementError.InvalidOperation
import com.estebandacosta.jdrassistant.core.element.FieldOp
import com.estebandacosta.jdrassistant.core.schema.Kind
import com.estebandacosta.jdrassistant.core.schema.KindRegistry
import com.estebandacosta.jdrassistant.core.value.FlagOp
import com.estebandacosta.jdrassistant.core.value.ResourceError.NegativeAmount
import com.estebandacosta.jdrassistant.core.value.ResourceOp
import com.estebandacosta.jdrassistant.core.value.ResourceType
import com.estebandacosta.jdrassistant.core.value.ResourceValue
import com.estebandacosta.jdrassistant.core.world.OperationResult.Accepted
import com.estebandacosta.jdrassistant.core.world.OperationResult.Rejected
import com.estebandacosta.jdrassistant.core.world.WorldError.InvalidOnElement
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OperationJsonTest {

    private val thorin = ElementId("thorin")
    private val damage = Operation(thorin, FieldOp("hp", ResourceOp.Damage(5)))

    @Test
    fun `an operation encodes with stable type names`() {
        assertEquals(
            """{"target":"thorin","fieldOp":{"field":"hp","op":{"type":"resource.damage","amount":5}}}""",
            Json.encodeToString(damage),
        )
    }

    @Test
    fun `an operation without parameters encodes as its type alone`() {
        val reset = Operation(thorin, FieldOp("hp", ResourceOp.Reset))
        assertEquals(
            """{"target":"thorin","fieldOp":{"field":"hp","op":{"type":"resource.reset"}}}""",
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
        ).map { Operation(thorin, FieldOp("field", it)) }

        ops.forEach { assertEquals(it, Json.decodeFromString<Operation>(Json.encodeToString(it))) }
    }

    @Test
    fun `an unknown operation type is rejected when decoding`() {
        val json = """{"target":"thorin","fieldOp":{"field":"hp","op":{"type":"resource.explode"}}}"""
        assertFailsWith<SerializationException> { Json.decodeFromString<Operation>(json) }
    }

    @Test
    fun `a rejection carries the full reason`() {
        val result: OperationResult = Rejected(
            op = Operation(thorin, FieldOp("hp", ResourceOp.Damage(-5))),
            reason = InvalidOnElement(thorin, InvalidOperation("hp", NegativeAmount(-5))),
        )
        assertEquals(
            """{"type":"operation.rejected",""" +
                """"op":{"target":"thorin","fieldOp":{"field":"hp","op":{"type":"resource.damage","amount":-5}}},""" +
                """"reason":{"type":"world.invalidOnElement","element":"thorin",""" +
                """"cause":{"type":"element.invalidOperation","field":"hp",""" +
                """"cause":{"type":"resource.negativeAmount","amount":-5}}}}""",
            Json.encodeToString(result),
        )
    }

    @Test
    fun `a server decision reaches the client intact`() {
        val pc = KindId("test:pc")
        val registry = KindRegistry(listOf(Kind(pc, mapOf("hp" to ResourceType()))))
        val world = World(mapOf(thorin to Element(pc, mapOf("hp" to ResourceValue(current = 12, max = 20)))))
        val incoming = listOf(damage, Operation(thorin, FieldOp("hp", ResourceOp.Damage(-5))))

        val sent: List<OperationResult> = incoming.map { op ->
            registry.decide(world, op).fold({ Rejected(op, it) }, { Accepted(it) })
        }
        val received = sent.map { Json.decodeFromString<OperationResult>(Json.encodeToString(it)) }

        assertEquals(sent, received)
    }
}
