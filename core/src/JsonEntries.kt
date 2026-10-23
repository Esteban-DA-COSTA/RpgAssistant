package com.estebandacosta.jdrassistant.core

import arrow.core.Either
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray

/**
 * Décode séparément chaque entrée du tableau JSON [text] : la valeur, ou la raison de son échec.
 * Renvoie `null` si [text] n'est pas un tableau JSON lisible.
 */
internal fun <T> decodeEntries(text: String, deserializer: DeserializationStrategy<T>): List<Either<String, T>>? {
    val array = try {
        Json.parseToJsonElement(text) as? JsonArray
    } catch (_: SerializationException) {
        null
    }
    return array?.map { entry ->
        Either.catchOrThrow<SerializationException, T> { Json.decodeFromJsonElement(deserializer, entry) }
            .mapLeft { it.message.orEmpty() }
    }
}
