package com.estebandacosta.jdrassistant.core.schema

import arrow.core.left
import arrow.core.right
import com.estebandacosta.jdrassistant.core.FieldKey
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.element.Element
import com.estebandacosta.jdrassistant.core.element.ElementError.InvalidOperation
import com.estebandacosta.jdrassistant.core.element.ElementError.KindMismatch
import com.estebandacosta.jdrassistant.core.element.ElementError.MissingValue
import com.estebandacosta.jdrassistant.core.element.ElementError.UnknownField
import com.estebandacosta.jdrassistant.core.element.FieldOp
import com.estebandacosta.jdrassistant.core.value.FlagOp
import com.estebandacosta.jdrassistant.core.value.FlagType
import com.estebandacosta.jdrassistant.core.value.FlagValue
import com.estebandacosta.jdrassistant.core.value.ResourceError.NegativeAmount
import com.estebandacosta.jdrassistant.core.value.ResourceError.TempNotAllowed
import com.estebandacosta.jdrassistant.core.value.ResourceOp.Damage
import com.estebandacosta.jdrassistant.core.value.ResourceOp.Heal
import com.estebandacosta.jdrassistant.core.value.ResourceOp.SetTemp
import com.estebandacosta.jdrassistant.core.value.ResourceType
import com.estebandacosta.jdrassistant.core.value.ResourceValue
import com.estebandacosta.jdrassistant.core.value.ValueError.OpMismatch
import com.estebandacosta.jdrassistant.core.value.ValueError.ValueMismatch
import kotlin.test.Test
import kotlin.test.assertEquals

class KindTest {

    private val pcId = KindId("test:pc")
    private val pc = Kind(
        id = pcId,
        fields = mapOf(
            FieldKey("hp") to ResourceType(allowTemp = true),
            FieldKey("hitDice") to ResourceType(),
            FieldKey("inspiration") to FlagType,
        ),
    )
    private val thorin = Element(
        kind = pcId,
        values = mapOf(
            FieldKey("hp") to ResourceValue(current = 12, max = 20),
            FieldKey("hitDice") to ResourceValue(current = 3, max = 5),
            FieldKey("inspiration") to FlagValue(false),
        ),
    )

    @Test
    fun `an operation on a declared field is accepted`() {
        val op = FieldOp(FieldKey("hp"), Damage(5))
        assertEquals(op.right(), pc.decide(thorin, op))
    }

    @Test
    fun `evolve changes only the targeted field`() {
        val after = pc.evolve(thorin, FieldOp(FieldKey("hp"), Damage(5)))
        assertEquals(ResourceValue(current = 7, max = 20), after.values[FieldKey("hp")])
        assertEquals(thorin.values[FieldKey("hitDice")], after.values[FieldKey("hitDice")])
    }

    @Test
    fun `an unknown field is refused`() {
        assertEquals(UnknownField(FieldKey("mana")).left(), pc.decide(thorin, FieldOp(FieldKey("mana"), Damage(1))))
    }

    @Test
    fun `an element of another kind is refused`() {
        val sword = Element(kind = KindId("test:weapon"), values = emptyMap())
        val expected = KindMismatch(expected = pcId, actual = KindId("test:weapon"))
        assertEquals(expected.left(), pc.decide(sword, FieldOp(FieldKey("hp"), Damage(1))))
    }

    @Test
    fun `a declared field without value is refused`() {
        val incomplete = thorin.copy(values = thorin.values - FieldKey("hitDice"))
        assertEquals(MissingValue(FieldKey("hitDice")).left(), pc.decide(incomplete, FieldOp(FieldKey("hitDice"), Heal(1))))
    }

    @Test
    fun `a refusal from the value type is reported with its field`() {
        val expected = InvalidOperation(FieldKey("hp"), NegativeAmount(-5))
        assertEquals(expected.left(), pc.decide(thorin, FieldOp(FieldKey("hp"), Damage(-5))))
    }

    @Test
    fun `each field applies its own configuration`() {
        assertEquals(FieldOp(FieldKey("hp"), SetTemp(5)).right(), pc.decide(thorin, FieldOp(FieldKey("hp"), SetTemp(5))))
        val expected = InvalidOperation(FieldKey("hitDice"), TempNotAllowed)
        assertEquals(expected.left(), pc.decide(thorin, FieldOp(FieldKey("hitDice"), SetTemp(5))))
    }

    @Test
    fun `a refused operation leaves the element untouched`() {
        val result = pc.decide(thorin, FieldOp(FieldKey("hp"), Damage(-5))).map { pc.evolve(thorin, it) }
        assertEquals(InvalidOperation(FieldKey("hp"), NegativeAmount(-5)).left(), result)
    }

    @Test
    fun `a history of field operations replays with fold`() {
        val history = listOf(FieldOp(FieldKey("hp"), Damage(10)), FieldOp(FieldKey("hitDice"), Heal(1)), FieldOp(FieldKey("hp"), Heal(3)))
        val after = history.fold(thorin, pc::evolve)
        assertEquals(ResourceValue(current = 5, max = 20), after.values[FieldKey("hp")])
        assertEquals(ResourceValue(current = 4, max = 5), after.values[FieldKey("hitDice")])
    }

    @Test
    fun `a flag field evolves through its own type`() {
        val op = FieldOp(FieldKey("inspiration"), FlagOp.Set(true))
        val after = pc.decide(thorin, op).map { pc.evolve(thorin, it) }
        assertEquals(FlagValue(true), after.getOrNull()?.values?.get(FieldKey("inspiration")))
    }

    @Test
    fun `an operation of the wrong type for the field is refused`() {
        val expected = InvalidOperation(FieldKey("hp"), OpMismatch(FlagOp.Set(true)))
        assertEquals(expected.left(), pc.decide(thorin, FieldOp(FieldKey("hp"), FlagOp.Set(true))))
    }

    @Test
    fun `a value stored under the wrong type is refused`() {
        val corrupted = thorin.copy(values = thorin.values + (FieldKey("hitDice") to FlagValue(true)))
        val expected = InvalidOperation(FieldKey("hitDice"), ValueMismatch(FlagValue(true)))
        assertEquals(expected.left(), pc.decide(corrupted, FieldOp(FieldKey("hitDice"), Heal(1))))
    }
}
