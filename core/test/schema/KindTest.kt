package com.estebandacosta.jdrassistant.core.schema

import arrow.core.left
import arrow.core.right
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.element.Element
import com.estebandacosta.jdrassistant.core.element.ElementError.InvalidOperation
import com.estebandacosta.jdrassistant.core.element.ElementError.KindMismatch
import com.estebandacosta.jdrassistant.core.element.ElementError.MissingValue
import com.estebandacosta.jdrassistant.core.element.ElementError.UnknownField
import com.estebandacosta.jdrassistant.core.element.FieldOp
import com.estebandacosta.jdrassistant.core.value.ResourceError.NegativeAmount
import com.estebandacosta.jdrassistant.core.value.ResourceError.TempNotAllowed
import com.estebandacosta.jdrassistant.core.value.ResourceOp.Damage
import com.estebandacosta.jdrassistant.core.value.ResourceOp.Heal
import com.estebandacosta.jdrassistant.core.value.ResourceOp.SetTemp
import com.estebandacosta.jdrassistant.core.value.ResourceType
import com.estebandacosta.jdrassistant.core.value.ResourceValue
import kotlin.test.Test
import kotlin.test.assertEquals

class KindTest {

    private val pcId = KindId("test:pc")
    private val pc = Kind(
        id = pcId,
        fields = mapOf(
            "hp" to ResourceType(allowTemp = true),
            "hitDice" to ResourceType(),
        ),
    )
    private val thorin = Element(
        kind = pcId,
        values = mapOf(
            "hp" to ResourceValue(current = 12, max = 20),
            "hitDice" to ResourceValue(current = 3, max = 5),
        ),
    )

    @Test
    fun `an operation on a declared field is accepted`() {
        val op = FieldOp("hp", Damage(5))
        assertEquals(op.right(), pc.decide(thorin, op))
    }

    @Test
    fun `evolve changes only the targeted field`() {
        val after = pc.evolve(thorin, FieldOp("hp", Damage(5)))
        assertEquals(ResourceValue(current = 7, max = 20), after.values["hp"])
        assertEquals(thorin.values["hitDice"], after.values["hitDice"])
    }

    @Test
    fun `an unknown field is refused`() {
        assertEquals(UnknownField("mana").left(), pc.decide(thorin, FieldOp("mana", Damage(1))))
    }

    @Test
    fun `an element of another kind is refused`() {
        val sword = Element(kind = KindId("test:weapon"), values = emptyMap())
        val expected = KindMismatch(expected = pcId, actual = KindId("test:weapon"))
        assertEquals(expected.left(), pc.decide(sword, FieldOp("hp", Damage(1))))
    }

    @Test
    fun `a declared field without value is refused`() {
        val incomplete = thorin.copy(values = thorin.values - "hitDice")
        assertEquals(MissingValue("hitDice").left(), pc.decide(incomplete, FieldOp("hitDice", Heal(1))))
    }

    @Test
    fun `a refusal from the value type is reported with its field`() {
        val expected = InvalidOperation("hp", NegativeAmount(-5))
        assertEquals(expected.left(), pc.decide(thorin, FieldOp("hp", Damage(-5))))
    }

    @Test
    fun `each field applies its own configuration`() {
        assertEquals(FieldOp("hp", SetTemp(5)).right(), pc.decide(thorin, FieldOp("hp", SetTemp(5))))
        val expected = InvalidOperation("hitDice", TempNotAllowed)
        assertEquals(expected.left(), pc.decide(thorin, FieldOp("hitDice", SetTemp(5))))
    }

    @Test
    fun `a refused operation leaves the element untouched`() {
        val result = pc.decide(thorin, FieldOp("hp", Damage(-5))).map { pc.evolve(thorin, it) }
        assertEquals(InvalidOperation("hp", NegativeAmount(-5)).left(), result)
    }

    @Test
    fun `a history of field operations replays with fold`() {
        val history = listOf(FieldOp("hp", Damage(10)), FieldOp("hitDice", Heal(1)), FieldOp("hp", Heal(3)))
        val after = history.fold(thorin, pc::evolve)
        assertEquals(ResourceValue(current = 5, max = 20), after.values["hp"])
        assertEquals(ResourceValue(current = 4, max = 5), after.values["hitDice"])
    }
}
