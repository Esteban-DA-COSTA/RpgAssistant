package com.estebandacosta.jdrassistant.core.value

import arrow.core.left
import arrow.core.right
import com.estebandacosta.jdrassistant.core.value.ResourceError.CurrentOutOfRange
import com.estebandacosta.jdrassistant.core.value.ResourceError.MaxBelowMin
import com.estebandacosta.jdrassistant.core.value.ResourceError.NegativeTemp
import com.estebandacosta.jdrassistant.core.value.ResourceError.TempNotAllowed
import com.estebandacosta.jdrassistant.core.value.ValueError.ValueMismatch
import kotlin.test.Test
import kotlin.test.assertEquals

class ResourceValidateTest {

    private val type = ResourceType()
    private val withTemp = ResourceType(allowTemp = true)
    private val hp = ResourceValue(current = 12, max = 20)

    @Test
    fun `a resource within its bounds is accepted`() {
        assertEquals(hp.right(), type.validate(hp))
    }

    @Test
    fun `current above max is refused`() {
        assertEquals(CurrentOutOfRange(current = 25, min = 0, max = 20).left(), type.validate(hp.copy(current = 25)))
    }

    @Test
    fun `current below min is refused`() {
        val type = ResourceType(min = -10)
        assertEquals(CurrentOutOfRange(current = -11, min = -10, max = 20).left(), type.validate(hp.copy(current = -11)))
    }

    @Test
    fun `max below min is refused`() {
        val type = ResourceType(min = 5)
        assertEquals(MaxBelowMin(max = 3, min = 5).left(), type.validate(ResourceValue(current = 5, max = 3)))
    }

    @Test
    fun `temp is accepted on a field that allows it`() {
        assertEquals(hp.copy(temp = 4).right(), withTemp.validate(hp.copy(temp = 4)))
    }

    @Test
    fun `negative temp is refused`() {
        assertEquals(NegativeTemp(-1).left(), withTemp.validate(hp.copy(temp = -1)))
    }

    @Test
    fun `temp on a field without temp is refused`() {
        assertEquals(TempNotAllowed.left(), type.validate(hp.copy(temp = 3)))
    }

    @Test
    fun `a value of another type is refused`() {
        assertEquals(ValueMismatch(FlagValue(true)).left(), type.validate(FlagValue(true)))
    }
}
