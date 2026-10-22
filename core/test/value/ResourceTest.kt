package com.estebandacosta.jdrassistant.core.value

import com.estebandacosta.jdrassistant.core.value.ResourceOp.Damage
import com.estebandacosta.jdrassistant.core.value.ResourceOp.Heal
import com.estebandacosta.jdrassistant.core.value.ResourceOp.Reset
import com.estebandacosta.jdrassistant.core.value.ResourceOp.SetMax
import com.estebandacosta.jdrassistant.core.value.ResourceOp.SetTemp
import kotlin.test.Test
import kotlin.test.assertEquals

class ResourceTest {

    private val hp = ResourceValue(current = 12, max = 20)

    @Test
    fun `damage reduces current`() {
        assertEquals(ResourceValue(current = 7, max = 20), hp.apply(Damage(5)))
    }

    @Test
    fun `damage consumes temp before current`() {
        val withTemp = hp.copy(temp = 8)
        assertEquals(ResourceValue(current = 12, max = 20, temp = 3), withTemp.apply(Damage(5)))
    }

    @Test
    fun `damage larger than temp spills into current`() {
        val withTemp = hp.copy(temp = 3)
        assertEquals(ResourceValue(current = 10, max = 20, temp = 0), withTemp.apply(Damage(5)))
    }

    @Test
    fun `damage never drives current below zero`() {
        assertEquals(ResourceValue(current = 0, max = 20), hp.apply(Damage(50)))
    }

    @Test
    fun `heal is capped at max`() {
        assertEquals(ResourceValue(current = 20, max = 20), hp.apply(Heal(50)))
    }

    @Test
    fun `setTemp replaces the temporary pool`() {
        assertEquals(ResourceValue(current = 12, max = 20, temp = 6), hp.copy(temp = 2).apply(SetTemp(6)))
    }

    @Test
    fun `lowering max pulls current down with it`() {
        assertEquals(ResourceValue(current = 10, max = 10), hp.apply(SetMax(10)))
    }

    @Test
    fun `raising max leaves current untouched`() {
        assertEquals(ResourceValue(current = 12, max = 30), hp.apply(SetMax(30)))
    }

    @Test
    fun `reset restores current to max and clears temp`() {
        assertEquals(ResourceValue(current = 20, max = 20), hp.copy(current = 3, temp = 4).apply(Reset))
    }

    @Test
    fun `operations chain into a readable history`() {
        val end = hp.apply(Damage(10)).apply(Heal(4)).apply(SetTemp(5)).apply(Damage(7))
        assertEquals(ResourceValue(current = 4, max = 20, temp = 0), end)
    }
}
