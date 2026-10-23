package com.estebandacosta.jdrassistant.core.module

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import arrow.core.toEitherNel
import arrow.core.toNonEmptyListOrNull
import com.estebandacosta.jdrassistant.core.BlueprintId
import com.estebandacosta.jdrassistant.core.ModuleId
import com.estebandacosta.jdrassistant.core.blueprint.Blueprint
import com.estebandacosta.jdrassistant.core.blueprint.BlueprintDeclarationError
import com.estebandacosta.jdrassistant.core.blueprint.decodeCompendium
import com.estebandacosta.jdrassistant.core.schema.KindDeclarationError
import com.estebandacosta.jdrassistant.core.schema.KindRegistry
import com.estebandacosta.jdrassistant.core.schema.decodeKinds
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Contenu de `module.json` : l'identité du module. */
@Serializable
data class Manifest(
    val id: ModuleId,
    val title: String,
)

/** Module chargé et vérifié : son manifeste, ses Kinds et ses Blueprints. */
data class Module(
    val manifest: Manifest,
    val kinds: KindRegistry,
    val compendium: Map<BlueprintId, Blueprint>,
)

/** Raisons pour lesquelles un module est refusé. */
sealed interface ModuleError {
    /** `module.json` est illisible ou mal formé, pour la raison [reason]. */
    data class InvalidManifest(val reason: String) : ModuleError

    /** `kinds.json` est refusé, pour la raison [error]. */
    data class InKinds(val error: KindDeclarationError) : ModuleError

    /** Le compendium est refusé, pour la raison [error]. */
    data class InCompendium(val error: BlueprintDeclarationError) : ModuleError

    /** L'identifiant déclaré [id] ne commence pas par le préfixe du module [module]. */
    data class WrongPrefix(val id: String, val module: ModuleId) : ModuleError
}

/**
 * Charge un module à partir du contenu de `module.json`, de `kinds.json` et des fichiers du compendium,
 * indexés par leur chemin. Renvoie le module, ou toutes les erreurs trouvées.
 */
fun decodeModule(
    manifest: String,
    kinds: String,
    compendium: Map<String, String>,
): Either<NonEmptyList<ModuleError>, Module> =
    Either.zipOrAccumulate(decodeManifest(manifest).toEitherNel(), decodeContent(kinds, compendium)) { header, content ->
        Module(header, content.first, content.second)
    }.flatMap(::checkPrefixes)

private fun decodeManifest(text: String): Either<ModuleError, Manifest> =
    Either.catchOrThrow<SerializationException, Manifest> { Json.decodeFromString<Manifest>(text) }
        .mapLeft { ModuleError.InvalidManifest(it.message.orEmpty()) }

private fun decodeContent(
    kinds: String,
    compendium: Map<String, String>,
): Either<NonEmptyList<ModuleError>, Pair<KindRegistry, Map<BlueprintId, Blueprint>>> =
    decodeKinds(kinds).mapLeft { errors -> errors.map(ModuleError::InKinds) }.flatMap { registry ->
        decodeCompendium(compendium, registry)
            .mapLeft { errors -> errors.map(ModuleError::InCompendium) }
            .map { blueprints -> registry to blueprints }
    }

private fun checkPrefixes(module: Module): Either<NonEmptyList<ModuleError>, Module> {
    val prefix = "${module.manifest.id.value}:"
    val declared = module.kinds.ids.map { it.value } + module.compendium.keys.map { it.value }
    val wrong = declared.filterNot { it.startsWith(prefix) }.map { ModuleError.WrongPrefix(it, module.manifest.id) }
    return wrong.toNonEmptyListOrNull()?.left() ?: module.right()
}
