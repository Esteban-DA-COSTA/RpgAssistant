package com.estebandacosta.jdrassistant.core.module

import arrow.core.left
import arrow.core.nonEmptyListOf
import com.estebandacosta.jdrassistant.core.BlueprintId
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.ModuleId
import com.estebandacosta.jdrassistant.core.blueprint.BlueprintDeclarationError
import com.estebandacosta.jdrassistant.core.module.ModuleError.InCompendium
import com.estebandacosta.jdrassistant.core.module.ModuleError.InKinds
import com.estebandacosta.jdrassistant.core.module.ModuleError.InvalidManifest
import com.estebandacosta.jdrassistant.core.module.ModuleError.WrongPrefix
import com.estebandacosta.jdrassistant.core.schema.KindDeclarationError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class ModuleTest {

    private val manifest = """{"id":"test","title":"Module de test"}"""
    private val kinds = """[{"id":"test:npc","fields":{"hp":{"type":"resource"}}}]"""
    private val goblin = """{"id":"test:goblin","kind":"test:npc","values":{"hp":{"type":"resource","current":7,"max":7}}}"""
    private val compendium = mapOf("monsters.json" to "[$goblin]")

    @Test
    fun `a complete module loads its manifest, kinds and compendium`() {
        val module = assertNotNull(decodeModule(manifest, kinds, compendium).getOrNull())

        assertEquals(Manifest(ModuleId("test"), "Module de test"), module.manifest)
        assertEquals(setOf(KindId("test:npc")), module.kinds.ids)
        assertEquals(setOf(BlueprintId("test:goblin")), module.compendium.keys)
    }

    @Test
    fun `an unreadable manifest is reported`() {
        val errors = assertNotNull(decodeModule("""{"id":"test"}""", kinds, compendium).leftOrNull())
        assertIs<InvalidManifest>(errors.single())
    }

    @Test
    fun `manifest and kinds errors are reported together`() {
        val errors = assertNotNull(decodeModule("not json", "{}", compendium).leftOrNull())

        assertEquals(2, errors.size)
        assertIs<InvalidManifest>(errors[0])
        assertEquals(InKinds(KindDeclarationError.NotAList), errors[1])
    }

    @Test
    fun `the compendium is not checked against invalid kinds`() {
        assertEquals(
            nonEmptyListOf(InKinds(KindDeclarationError.NotAList)).left(),
            decodeModule(manifest, "{}", mapOf("monsters.json" to "not json")),
        )
    }

    @Test
    fun `compendium errors are reported once kinds are valid`() {
        assertEquals(
            nonEmptyListOf(InCompendium(BlueprintDeclarationError.NotAList("monsters.json"))).left(),
            decodeModule(manifest, kinds, mapOf("monsters.json" to "not json")),
        )
    }

    @Test
    fun `a declared id without the module prefix is reported`() {
        val unprefixedKinds = """[{"id":"npc","fields":{"hp":{"type":"resource"}}}]"""
        val unprefixedGoblin = """{"id":"goblin","kind":"npc","values":{"hp":{"type":"resource","current":7,"max":7}}}"""

        assertEquals(
            nonEmptyListOf(WrongPrefix("npc", ModuleId("test")), WrongPrefix("goblin", ModuleId("test"))).left(),
            decodeModule(manifest, unprefixedKinds, mapOf("monsters.json" to "[$unprefixedGoblin]")),
        )
    }
}
