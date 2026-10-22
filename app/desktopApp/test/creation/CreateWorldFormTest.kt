package com.estebandacosta.jdrassistant.creation

import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class CreateWorldFormTest {

    private val documents = Path.of("/home/mj/Documents")

    @Test
    fun `un formulaire vide signale à la fois le nom et l'emplacement manquants`() {
        assertEquals(
            nonEmptyListOf(CreateWorldError.BlankName, CreateWorldError.MissingLocation).left(),
            CreateWorldForm().validate(),
        )
    }

    @Test
    fun `un nom fait d'espaces compte comme vide`() {
        assertEquals(
            nonEmptyListOf(CreateWorldError.BlankName).left(),
            CreateWorldForm(name = "   ", location = documents).validate(),
        )
    }

    @Test
    fun `un nom sans emplacement ne signale que l'emplacement`() {
        assertEquals(
            nonEmptyListOf(CreateWorldError.MissingLocation).left(),
            CreateWorldForm(name = "Valdor").validate(),
        )
    }

    @Test
    fun `un nom contenant des caractères interdits dans un nom de fichier les signale tous`() {
        assertEquals(
            nonEmptyListOf(CreateWorldError.ForbiddenCharacters(setOf('/', ':', '?'))).left(),
            CreateWorldForm(name = "Valdor/Est : la suite ?", location = documents).validate(),
        )
    }

    @Test
    fun `un nom interdit et un emplacement manquant sont signalés ensemble`() {
        assertEquals(
            nonEmptyListOf(CreateWorldError.ForbiddenCharacters(setOf('*')), CreateWorldError.MissingLocation).left(),
            CreateWorldForm(name = "Valdor*").validate(),
        )
    }

    @Test
    fun `un formulaire complet donne un nouveau monde au nom débarrassé des espaces`() {
        assertEquals(
            NewWorld("Royaumes de Valdor", documents).right(),
            CreateWorldForm(name = "  Royaumes de Valdor ", location = documents).validate(),
        )
    }
}
