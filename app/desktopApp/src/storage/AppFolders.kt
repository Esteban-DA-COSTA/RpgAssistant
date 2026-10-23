package com.estebandacosta.jdrassistant.storage

import java.nio.file.Path

/** Système d'exploitation du poste du MJ. */
enum class OperatingSystem {
    Windows,
    MacOs,
    Linux;

    companion object {
        /** Déduit le système de [osName], tel que le donne la propriété `os.name`. */
        fun fromName(osName: String): OperatingSystem = when {
            osName.startsWith("Windows") -> Windows
            osName.startsWith("Mac") -> MacOs
            else -> Linux
        }
    }
}

/** Emplacements des données de l'app sur le poste du MJ. */
object AppFolders {
    /** Dossier des modules de ce poste. */
    fun modules(): Path =
        modules(OperatingSystem.fromName(System.getProperty("os.name")), System::getenv, Path.of(System.getProperty("user.home")))

    /** Dossier des modules pour [os], d'après les variables d'environnement [env] et le dossier personnel [home]. */
    fun modules(os: OperatingSystem, env: (String) -> String?, home: Path): Path = appData(os, env, home).resolve("modules")

    /** Dossier de données de l'app pour [os], d'après les variables d'environnement [env] et le dossier personnel [home]. */
    fun appData(os: OperatingSystem, env: (String) -> String?, home: Path): Path {
        fun fromEnv(name: String): Path? = env(name)?.takeIf { it.isNotBlank() }?.let(Path::of)
        return when (os) {
            OperatingSystem.Windows -> (fromEnv("APPDATA") ?: home.resolve("AppData").resolve("Roaming")).resolve("JdrAssistant")
            OperatingSystem.MacOs -> home.resolve("Library").resolve("Application Support").resolve("JdrAssistant")
            OperatingSystem.Linux -> (fromEnv("XDG_DATA_HOME") ?: home.resolve(".local").resolve("share")).resolve("jdr-assistant")
        }
    }
}
