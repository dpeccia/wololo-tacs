package com.grupox.wololo.model.helpers

import arrow.core.extensions.list.zip.zipWith
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Status
import org.bson.types.ObjectId

sealed class State(val id: ObjectId) {
    class GameState(id: ObjectId, val turnId: ObjectId, val status: Status, val towns: List<TownState>) : State(id) {
        override fun diff(other: State): Change.GameChange {
            if (other !is GameState || this.id != other.id) throw CustomException.InternalServer.DiffException()
            return Change.GameChange(
                    id = this.id,
                    deltaTurnId = this.differentiate(this.turnId, other.turnId),
                    deltaStatus = this.differentiate(this.status, other.status),
                    deltaTowns = this.towns.zipWith(other.towns) { t, ot -> t.diff(ot) }.filter { it.deltaGauchos != 0 || it.deltaOwnerId != null || it.deltaSpecialization != null }
            )
        }
    }

    class TownState(id: ObjectId, val ownerId: ObjectId?, val gauchos: Int, val specialization: String) : State(id) {
        override fun diff(other: State): Change.TownChange {
            if (other !is TownState || this.id != other.id) throw CustomException.InternalServer.DiffException()
            return Change.TownChange(
                    id = this.id,
                    deltaOwnerId = this.differentiate(this.ownerId, other.ownerId),
                    deltaSpecialization = this.differentiate(this.specialization, other.specialization),
                    deltaGauchos = this.gauchos - other.gauchos
            )
        }
    }

    abstract fun diff(other: State): Change

    protected fun <T>differentiate(final: T, initial: T) = if(final != initial) final else null
}