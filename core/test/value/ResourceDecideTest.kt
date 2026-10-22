package com.estebandacosta.jdrassistant.core.value

import arrow.core.left
import arrow.core.right
import com.estebandacosta.jdrassistant.core.value.ResourceError.MaxBelowMin
import com.estebandacosta.jdrassistant.core.value.ResourceError.NegativeAmount
import com.estebandacosta.jdrassistant.core.value.ResourceError.TempNotAllowed
import com.estebandacosta.jdrassistant.core.value.ResourceOp.Damage
import com.estebandacosta.jdrassistant.core.value.ResourceOp.Heal
import com.estebandacosta.jdrassistant.core.value.ResourceOp.Reset
import com.estebandacosta.jdrassistant.core.value.ResourceOp.SetMax
import com.estebandacosta.jdrassistant.core.value.ResourceOp.SetTemp
import com.estebandacosta.jdrassistant.core.value.ValueError.OpMismatch
import com.estebandacosta.jdrassistant.core.value.ValueError.ValueMismatch
import kotlin.test.Test
import kotlin.test.assertEquals

class ResourceDecideTest {

    private val type = ResourceType()
    private val withTemp = ResourceType(allowTemp = true)
    private val hp = ResourceValue(current = 12, max = 20)

    @Test
    fun `a valid operation is accepted as is`() {
        assertEquals(Damage(5).right(), type.decide(hp, Damage(5)))
    }

    @Test
    fun `negative damage is refused`() {
        assertEquals(NegativeAmount(-5).left(), type.decide(hp, Damage(-5)))
    }

    @Test
    fun `negative heal is refused`() {
        assertEquals(NegativeAmount(-3).left(), type.decide(hp, Heal(-3)))
    }

    @Test
    fun `setTemp is refused when the field has no temporary pool`() {
        assertEquals(TempNotAllowed.left(), type.decide(hp, SetTemp(5)))
    }

    @Test
    fun `setTemp is accepted when the field allows temp`() {
        assertEquals(SetTemp(5).right(), withTemp.decide(hp, SetTemp(5)))
    }

    @Test
    fun `negative setTemp is refused`() {
        assertEquals(NegativeAmount(-2).left(), withTemp.decide(hp, SetTemp(-2)))
    }

    @Test
    fun `the first failed check is the one reported`() {
        assertEquals(TempNotAllowed.left(), type.decide(hp, SetTemp(-2)))
    }

    @Test
    fun `setMax below min is refused`() {
        assertEquals(MaxBelowMin(max = -5, min = 0).left(), type.decide(hp, SetMax(-5)))
    }

    @Test
    fun `setMax below zero is accepted when min allows it`() {
        assertEquals(SetMax(-5).right(), ResourceType(min = -10).decide(hp, SetMax(-5)))
    }

    @Test
    fun `reset is always accepted`() {
        assertEquals(Reset.right(), type.decide(hp, Reset))
    }

    @Test
    fun `an accepted operation flows into evolve`() {
        val result = type.decide(hp, Damage(5)).map { type.evolve(hp, it) }
        assertEquals(ResourceValue(current = 7, max = 20).right(), result)
    }

    @Test
    fun `a refused operation never reaches evolve`() {
        val result = type.decide(hp, Damage(-5)).map { type.evolve(hp, it) }
        assertEquals(NegativeAmount(-5).left(), result)
    }

    @Test
    fun `an operation of another type is refused`() {
        assertEquals(OpMismatch(FlagOp.Set(true)).left(), type.decide(hp, FlagOp.Set(true)))
    }

    @Test
    fun `a value of another type is refused`() {
        assertEquals(ValueMismatch(FlagValue(true)).left(), type.decide(FlagValue(true), Damage(1)))
    }
}
