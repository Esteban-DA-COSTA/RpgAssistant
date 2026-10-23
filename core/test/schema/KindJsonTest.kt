package com.estebandacosta.jdrassistant.core.schema

import com.estebandacosta.jdrassistant.core.FieldKey
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.value.FlagType
import com.estebandacosta.jdrassistant.core.value.ResourceType
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KindJsonTest {

    @Test
    fun `a kind decodes from its module declaration`() {
        val json = """
            {
              "id": "dnd5e:pc",
              "fields": {
                "hp": { "type": "resource", "min": -10, "allowTemp": true },
                "inspired": { "type": "flag" }
              }
            }
        """
        assertEquals(
            Kind(KindId("dnd5e:pc"), mapOf(FieldKey("hp") to ResourceType(min = -10, allowTemp = true), FieldKey("inspired") to FlagType)),
            Json.decodeFromString<Kind>(json),
        )
    }

    @Test
    fun `a field type left unconfigured takes its defaults`() {
        val json = """{"id":"dnd5e:pc","fields":{"hp":{"type":"resource"}}}"""
        assertEquals(mapOf(FieldKey("hp") to ResourceType()), Json.decodeFromString<Kind>(json).fields)
    }

    @Test
    fun `an unknown field type is rejected when decoding`() {
        val json = """{"id":"dnd5e:pc","fields":{"mana":{"type":"mana"}}}"""
        assertFailsWith<SerializationException> { Json.decodeFromString<Kind>(json) }
    }

    @Test
    fun `a misspelled configuration key is rejected when decoding`() {
        val json = """{"id":"dnd5e:pc","fields":{"hp":{"type":"resource","allowtemp":true}}}"""
        assertFailsWith<SerializationException> { Json.decodeFromString<Kind>(json) }
    }
}
