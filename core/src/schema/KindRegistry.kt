package com.estebandacosta.jdrassistant.core.schema

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.estebandacosta.jdrassistant.core.KindId
import com.estebandacosta.jdrassistant.core.world.Operation
import com.estebandacosta.jdrassistant.core.world.World
import com.estebandacosta.jdrassistant.core.world.WorldError

/**
 * Kinds chargés pour une campagne, indexés par [Kind.id].
 * En cas d'identifiants en double, le dernier Kind de [kinds] l'emporte.
 */
class KindRegistry(kinds: Iterable<Kind>) {
    private val byId: Map<KindId, Kind> = kinds.associateBy { it.id }

    /** Kind d'identifiant [id], ou `null` s'il n'est pas chargé. */
    operator fun get(id: KindId): Kind? = byId[id]

    /** Valide [op] sur [world] ; renvoie l'opération acceptée, ou la raison du refus. */
    fun decide(world: World, op: Operation): Either<WorldError, Operation> = either {
        when (op) {
            is Operation.UpdateField -> {
                val element = ensureNotNull(world.elements[op.target]) { WorldError.UnknownElement(op.target) }
                val kind = ensureNotNull(byId[element.kind]) { WorldError.UnknownKind(op.target, element.kind) }
                kind.decide(element, op.fieldOp).mapLeft { WorldError.InvalidOnElement(op.target, it) }.bind()
            }
            is Operation.CreateElement -> {
                ensure(op.id !in world.elements) { WorldError.ElementAlreadyExists(op.id) }
                val kind = ensureNotNull(byId[op.element.kind]) { WorldError.UnknownKind(op.id, op.element.kind) }
                kind.validate(op.element).mapLeft { WorldError.NonConformingElement(op.id, it) }.bind()
            }
        }
        op
    }

    /**
     * Applique [op] à [world] et renvoie le nouveau monde.
     * Ne valide rien : [op] doit avoir été acceptée par [decide] pour ce monde.
     */
    fun evolve(world: World, op: Operation): World = when (op) {
        is Operation.UpdateField -> {
            val element = world.elements.getValue(op.target)
            val kind = byId.getValue(element.kind)
            world.copy(elements = world.elements + (op.target to kind.evolve(element, op.fieldOp)))
        }
        is Operation.CreateElement -> world.copy(elements = world.elements + (op.id to op.element))
    }
}
