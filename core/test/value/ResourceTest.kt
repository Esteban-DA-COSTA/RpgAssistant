package com.estebandacosta.jdrassistant.core.value

import com.estebandacosta.jdrassistant.core.value.ResourceOp.Damage
import com.estebandacosta.jdrassistant.core.value.ResourceOp.Heal
import com.estebandacosta.jdrassistant.core.value.ResourceOp.Reset
import com.estebandacosta.jdrassistant.core.value.ResourceOp.SetMax
import com.estebandacosta.jdrassistant.core.value.ResourceOp.SetTemp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ResourceTest {

    private val type = ResourceType()
    private val hp = ResourceValue(current = 12, max = 20)

    @Test
    fun `damage reduces current`() {
        assertEquals(ResourceValue(current = 7, max = 20), type.evolve(hp, Damage(5)))
    }

    @Test
    fun `damage consumes temp before current`() {
        val withTemp = hp.copy(temp = 8)
        assertEquals(ResourceValue(current = 12, max = 20, temp = 3), type.evolve(withTemp, Damage(5)))
    }

    @Test
    fun `damage larger than temp spills into current`() {
        val withTemp = hp.copy(temp = 3)
        assertEquals(ResourceValue(current = 10, max = 20, temp = 0), type.evolve(withTemp, Damage(5)))
    }

    @Test
    fun `damage stops at zero by default`() {
        assertEquals(ResourceValue(current = 0, max = 20), type.evolve(hp, Damage(50)))
    }

    @Test
    fun `negative min lets current go below zero`() {
        val belowZero = ResourceType(min = -10)
        assertEquals(ResourceValue(current = -3, max = 20), belowZero.evolve(hp, Damage(15)))
    }

    @Test
    fun `damage stops at the configured min`() {
        val belowZero = ResourceType(min = -10)
        assertEquals(ResourceValue(current = -10, max = 20), belowZero.evolve(hp, Damage(50)))
    }

    @Test
    fun `heal is capped at max`() {
        assertEquals(ResourceValue(current = 20, max = 20), type.evolve(hp, Heal(50)))
    }

    @Test
    fun `setTemp replaces the temporary pool`() {
        assertEquals(ResourceValue(current = 12, max = 20, temp = 6), type.evolve(hp.copy(temp = 2), SetTemp(6)))
    }

    @Test
    fun `lowering max pulls current down with it`() {
        assertEquals(ResourceValue(current = 10, max = 10), type.evolve(hp, SetMax(10)))
    }

    @Test
    fun `raising max leaves current untouched`() {
        assertEquals(ResourceValue(current = 12, max = 30), type.evolve(hp, SetMax(30)))
    }

    @Test
    fun `reset restores current to max and clears temp`() {
        assertEquals(ResourceValue(current = 20, max = 20), type.evolve(hp.copy(current = 3, temp = 4), Reset))
    }

    @Test
    fun `a history of operations replays with fold`() {
        val history = listOf(Damage(10), Heal(4), SetTemp(5), Damage(7))
        assertEquals(ResourceValue(current = 4, max = 20, temp = 0), history.fold(hp, type::evolve))
    }

    @Test
    fun `evolve rejects an operation that decide would have refused`() {
        assertFailsWith<IllegalStateException> { type.evolve(hp, FlagOp.Set(true)) }
    }
}
