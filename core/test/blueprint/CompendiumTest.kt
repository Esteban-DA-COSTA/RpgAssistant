package com.estebandacosta.jdrassistant.core.blueprint

import arrow.core.left
import arrow.core.nonEmptyListOf
import com.estebandacosta.jdrassistant.core.BlueprintId
import com.estebandacosta.jdrassistant.core.FieldKey
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.blueprint.BlueprintDeclarationError.DuplicateBlueprint
import com.estebandacosta.jdrassistant.core.blueprint.BlueprintDeclarationError.Malformed
import com.estebandacosta.jdrassistant.core.blueprint.BlueprintDeclarationError.NonConforming
import com.estebandacosta.jdrassistant.core.blueprint.BlueprintDeclarationError.NotAList
import com.estebandacosta.jdrassistant.core.blueprint.BlueprintDeclarationError.UnknownKind
import com.estebandacosta.jdrassistant.core.element.ElementError.InvalidValue
import com.estebandacosta.jdrassistant.core.element.ElementError.UnknownField
import com.estebandacosta.jdrassistant.core.schema.Kind
import com.estebandacosta.jdrassistant.core.schema.KindRegistry
import com.estebandacosta.jdrassistant.core.value.ResourceError.CurrentOutOfRange
import com.estebandacosta.jdrassistant.core.value.ResourceType
import com.estebandacosta.jdrassistant.core.value.ResourceValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class CompendiumTest {

    private val npcId = KindId("test:npc")
    private val kinds = KindRegistry(listOf(Kind(npcId, mapOf(FieldKey("hp") to ResourceType()))))

    private val goblin = """{"id":"test:goblin","kind":"test:npc","values":{"hp":{"type":"resource","current":7,"max":7}}}"""
    private val orc = """{"id":"test:orc","kind":"test:npc","values":{"hp":{"type":"resource","current":15,"max":15}}}"""
    private val dragon = """{"id":"test:dragon","kind":"test:dragon","values":{}}"""
    private val ghoul =
        """{"id":"test:ghoul","kind":"test:npc","values":{"hp":{"type":"resource","current":9,"max":7},"mana":{"type":"flag","on":true}}}"""
    private val imp = """{"id":"test:imp","kind":"test:npc"}"""

    @Test
    fun `blueprints from every file are gathered by id`() {
        val compendium = assertNotNull(
            decodeCompendium(mapOf("goblins.json" to "[$goblin]", "orcs.json" to "[$orc]"), kinds).getOrNull(),
        )
        assertEquals(setOf(BlueprintId("test:goblin"), BlueprintId("test:orc")), compendium.keys)
        assertEquals(
            Blueprint(BlueprintId("test:goblin"), npcId, mapOf(FieldKey("hp") to ResourceValue(current = 7, max = 7))),
            compendium[BlueprintId("test:goblin")],
        )
    }

    @Test
    fun `a file that is not a JSON list is reported with its name`() {
        assertEquals(
            nonEmptyListOf(NotAList("monsters.json")).left(),
            decodeCompendium(mapOf("monsters.json" to goblin), kinds),
        )
    }

    @Test
    fun `a malformed blueprint is reported with its file and position`() {
        val errors = assertNotNull(decodeCompendium(mapOf("monsters.json" to "[$goblin,$imp]"), kinds).leftOrNull())
        assertEquals(Position("monsters.json", 1), assertIs<Malformed>(errors.single()).at)
    }

    @Test
    fun `a blueprint of an unknown kind is reported`() {
        assertEquals(
            nonEmptyListOf(UnknownKind(Position("monsters.json", 0), KindId("test:dragon"))).left(),
            decodeCompendium(mapOf("monsters.json" to "[$dragon]"), kinds),
        )
    }

    @Test
    fun `a non conforming blueprint is reported with every cause`() {
        val expected = NonConforming(
            Position("monsters.json", 0),
            nonEmptyListOf(
                UnknownField(FieldKey("mana")),
                InvalidValue(FieldKey("hp"), CurrentOutOfRange(current = 9, min = 0, max = 7)),
            ),
        )
        assertEquals(nonEmptyListOf(expected).left(), decodeCompendium(mapOf("monsters.json" to "[$ghoul]"), kinds))
    }

    @Test
    fun `a blueprint declared twice across files is reported with both positions`() {
        val files = mapOf("goblins.json" to "[$goblin]", "monsters.json" to "[$orc,$goblin]")
        val expected = DuplicateBlueprint(
            BlueprintId("test:goblin"),
            listOf(Position("goblins.json", 0), Position("monsters.json", 1)),
        )
        assertEquals(nonEmptyListOf(expected).left(), decodeCompendium(files, kinds))
    }

    @Test
    fun `errors from every file are reported together`() {
        val files = mapOf("broken.json" to "not json", "monsters.json" to "[$imp,$dragon]")
        val errors = assertNotNull(decodeCompendium(files, kinds).leftOrNull())

        assertEquals(3, errors.size)
        assertEquals(NotAList("broken.json"), errors[0])
        assertEquals(Position("monsters.json", 0), assertIs<Malformed>(errors[1]).at)
        assertEquals(UnknownKind(Position("monsters.json", 1), KindId("test:dragon")), errors[2])
    }
}
