package com.estebandacosta.jdrassistant.core.world

import com.estebandacosta.jdrassistant.core.ElementId
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.element.Element
import com.estebandacosta.jdrassistant.core.value.FlagValue
import com.estebandacosta.jdrassistant.core.value.ResourceValue
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WorldJsonTest {

    private val pc = KindId("test:pc")
    private val thorin = Element(pc, mapOf("hp" to ResourceValue(current = 12, max = 20)))

    @Test
    fun `an element encodes its kind and its typed values`() {
        val element = Element(
            pc,
            mapOf("hp" to ResourceValue(current = 12, max = 20, temp = 3), "inspired" to FlagValue(true)),
        )
        assertEquals(
            """{"kind":"test:pc","values":{""" +
                """"hp":{"type":"resource","current":12,"max":20,"temp":3},""" +
                """"inspired":{"type":"flag","on":true}}}""",
            Json.encodeToString(element),
        )
    }

    @Test
    fun `a world encodes its elements keyed by their id`() {
        val world = World(mapOf(ElementId("thorin") to thorin))
        assertEquals(
            """{"elements":{"thorin":{"kind":"test:pc","values":{"hp":{"type":"resource","current":12,"max":20}}}}}""",
            Json.encodeToString(world),
        )
    }

    @Test
    fun `a world decodes back to itself`() {
        val world = World(
            mapOf(
                ElementId("thorin") to thorin,
                ElementId("gob-1") to Element(
                    KindId("test:npc"),
                    mapOf("hp" to ResourceValue(current = 0, max = 7, temp = 2), "fleeing" to FlagValue(false)),
                ),
            ),
        )
        assertEquals(world, Json.decodeFromString<World>(Json.encodeToString(world)))
    }

    @Test
    fun `a value of unknown type is rejected when decoding`() {
        val json = """{"elements":{"thorin":{"kind":"test:pc","values":{"hp":{"type":"mana","current":3}}}}}"""
        assertFailsWith<SerializationException> { Json.decodeFromString<World>(json) }
    }
}
