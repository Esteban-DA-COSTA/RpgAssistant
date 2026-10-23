package com.estebandacosta.jdrassistant.core.schema

import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
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
            "hp" to ResourceType(allowTemp = true),
            "hitDice" to ResourceType(),
            "inspiration" to FlagType,
        ),
    )
    private val thorin = Element(
        kind = pcId,
        values = mapOf(
            "hp" to ResourceValue(current = 12, max = 20),
            "hitDice" to ResourceValue(current = 3, max = 5),
            "inspiration" to FlagValue(false),
        ),
    )

    @Test
    fun `a conforming element is accepted`() {
        assertEquals(thorin.right(), pc.validate(thorin))
    }

    @Test
    fun `an element of another kind is refused on that ground alone`() {
        val sword = Element(kind = KindId("test:weapon"), values = mapOf("sharp" to FlagValue(true)))
        assertEquals(
            nonEmptyListOf(KindMismatch(expected = pcId, actual = KindId("test:weapon"))).left(),
            pc.validate(sword),
        )
    }

    @Test
    fun `each field is validated against its own configuration`() {
        val withTemp = ResourceValue(current = 3, max = 5, temp = 2)
        val element = thorin.copy(values = thorin.values + ("hp" to withTemp) + ("hitDice" to withTemp))
        assertEquals(nonEmptyListOf(InvalidValue("hitDice", TempNotAllowed)).left(), pc.validate(element))
    }

    @Test
    fun `missing, undeclared and invalid values are reported together`() {
        val broken = Element(
            kind = pcId,
            values = mapOf(
                "hp" to ResourceValue(current = 25, max = 20),
                "inspiration" to FlagValue(false),
                "mana" to FlagValue(true),
            ),
        )
        assertEquals(
            nonEmptyListOf(
                MissingValue("hitDice"),
                UnknownField("mana"),
                InvalidValue("hp", CurrentOutOfRange(current = 25, min = 0, max = 20)),
            ).left(),
            pc.validate(broken),
        )
    }
}
