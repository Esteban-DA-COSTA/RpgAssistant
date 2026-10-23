package com.estebandacosta.jdrassistant.core.schema

import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import com.estebandacosta.jdrassistant.core.FieldKey
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.element.Element
import com.estebandacosta.jdrassistant.core.element.ElementError.InvalidValue
import com.estebandacosta.jdrassistant.core.element.ElementError.KindMismatch
import com.estebandacosta.jdrassistant.core.element.ElementError.MissingValue
import com.estebandacosta.jdrassistant.core.element.ElementError.UnknownField
import com.estebandacosta.jdrassistant.core.value.FlagType
import com.estebandacosta.jdrassistant.core.value.FlagValue
import com.estebandacosta.jdrassistant.core.value.ResourceError.CurrentOutOfRange
import com.estebandacosta.jdrassistant.core.value.ResourceError.TempNotAllowed
import com.estebandacosta.jdrassistant.core.value.ResourceType
import com.estebandacosta.jdrassistant.core.value.ResourceValue
import kotlin.test.Test
import kotlin.test.assertEquals

class KindValidateTest {

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
    fun `a conforming element is accepted`() {
        assertEquals(thorin.right(), pc.validate(thorin))
    }

    @Test
    fun `an element of another kind is refused on that ground alone`() {
        val sword = Element(kind = KindId("test:weapon"), values = mapOf(FieldKey("sharp") to FlagValue(true)))
        assertEquals(
            nonEmptyListOf(KindMismatch(expected = pcId, actual = KindId("test:weapon"))).left(),
            pc.validate(sword),
        )
    }

    @Test
    fun `each field is validated against its own configuration`() {
        val withTemp = ResourceValue(current = 3, max = 5, temp = 2)
        val element = thorin.copy(values = thorin.values + (FieldKey("hp") to withTemp) + (FieldKey("hitDice") to withTemp))
        assertEquals(nonEmptyListOf(InvalidValue(FieldKey("hitDice"), TempNotAllowed)).left(), pc.validate(element))
    }

    @Test
    fun `missing, undeclared and invalid values are reported together`() {
        val broken = Element(
            kind = pcId,
            values = mapOf(
                FieldKey("hp") to ResourceValue(current = 25, max = 20),
                FieldKey("inspiration") to FlagValue(false),
                FieldKey("mana") to FlagValue(true),
            ),
        )
        assertEquals(
            nonEmptyListOf(
                MissingValue(FieldKey("hitDice")),
                UnknownField(FieldKey("mana")),
                InvalidValue(FieldKey("hp"), CurrentOutOfRange(current = 25, min = 0, max = 20)),
            ).left(),
            pc.validate(broken),
        )
    }
}
