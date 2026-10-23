package com.estebandacosta.jdrassistant.core.value

import arrow.core.left
import arrow.core.right
import com.estebandacosta.jdrassistant.core.value.ResourceOp.Damage
import com.estebandacosta.jdrassistant.core.value.ValueError.OpMismatch
import com.estebandacosta.jdrassistant.core.value.ValueError.ValueMismatch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FlagTest {

    private val off = FlagValue(false)

    @Test
    fun `set switches the flag on`() {
        assertEquals(FlagValue(true), FlagType.evolve(off, FlagOp.Set(true)))
    }

    @Test
    fun `set switches the flag off`() {
        assertEquals(FlagValue(false), FlagType.evolve(FlagValue(true), FlagOp.Set(false)))
    }

    @Test
    fun `a flag operation is accepted`() {
        assertEquals(FlagOp.Set(true).right(), FlagType.decide(off, FlagOp.Set(true)))
    }

    @Test
    fun `an operation of another type is refused`() {
        assertEquals(OpMismatch(Damage(1)).left(), FlagType.decide(off, Damage(1)))
    }

    @Test
    fun `a value of another type is refused`() {
        val hp = ResourceValue(current = 12, max = 20)
        assertEquals(ValueMismatch(hp).left(), FlagType.decide(hp, FlagOp.Set(true)))
    }

    @Test
    fun `validation accepts a flag value`() {
        assertEquals(off.right(), FlagType.validate(off))
    }

    @Test
    fun `validation refuses a value of another type`() {
        val hp = ResourceValue(current = 12, max = 20)
        assertEquals(ValueMismatch(hp).left(), FlagType.validate(hp))
    }

    @Test
    fun `evolve rejects an operation that decide would have refused`() {
        assertFailsWith<IllegalStateException> { FlagType.evolve(off, Damage(1)) }
    }
}
