package com.estebandacosta.jdrassistant.storage

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class AppFoldersTest {

    private val home: Path = Path.of("home", "mj")

    private fun env(vararg variables: Pair<String, String>): (String) -> String? = variables.toMap()::get

    @Test
    fun `reconnaît le système d'après le nom donné par la JVM`() {
        assertEquals(
            listOf(OperatingSystem.Windows, OperatingSystem.MacOs, OperatingSystem.Linux),
            listOf("Windows 11", "Mac OS X", "Linux").map(OperatingSystem::fromName),
        )
    }

    @Test
    fun `sous Windows, les données vont dans APPDATA`() {
        val appData = Path.of("roaming")

        assertEquals(
            appData.resolve("JdrAssistant"),
            AppFolders.appData(OperatingSystem.Windows, env("APPDATA" to appData.toString()), home),
        )
    }

    @Test
    fun `sous Windows sans APPDATA, les données vont dans AppData Roaming du dossier personnel`() {
        assertEquals(
            home.resolve("AppData").resolve("Roaming").resolve("JdrAssistant"),
            AppFolders.appData(OperatingSystem.Windows, env(), home),
        )
    }

    @Test
    fun `sous macOS, les données vont dans Application Support du dossier personnel`() {
        assertEquals(
            home.resolve("Library").resolve("Application Support").resolve("JdrAssistant"),
            AppFolders.appData(OperatingSystem.MacOs, env(), home),
        )
    }

    @Test
    fun `sous Linux, les données vont dans XDG_DATA_HOME`() {
        val dataHome = Path.of("xdg")

        assertEquals(
            dataHome.resolve("jdr-assistant"),
            AppFolders.appData(OperatingSystem.Linux, env("XDG_DATA_HOME" to dataHome.toString()), home),
        )
    }

    @Test
    fun `sous Linux avec XDG_DATA_HOME vide, les données vont dans local share du dossier personnel`() {
        assertEquals(
            home.resolve(".local").resolve("share").resolve("jdr-assistant"),
            AppFolders.appData(OperatingSystem.Linux, env("XDG_DATA_HOME" to ""), home),
        )
    }

    @Test
    fun `le dossier des modules est le sous-dossier modules des données de l'app`() {
        assertEquals(
            AppFolders.appData(OperatingSystem.Linux, env(), home).resolve("modules"),
            AppFolders.modules(OperatingSystem.Linux, env(), home),
        )
    }
}
