package com.grupox.wololo.model.helpers

import arrow.core.extensions.list.zip.zipWith
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Status
import org.bson.types.ObjectId

sealed class State {
    class GameState(val id: ObjectId, val turnUsername: String?, val status: Status, val towns: List<TownState>) : State() {
        override fun diff(other: State): Change.GameChange {
            if (other !is GameState || this.id != other.id) throw CustomException.InternalServer.DiffException()
            return Change.GameChange(
                    id = this.id.toHexString(),
                    deltaTurnUsername = this.differentiate(this.turnUsername, other.turnUsername),
                    deltaStatus = this.differentiate(this.status, other.status),
                    deltaTowns = this.towns.zipWith(other.towns) { t, ot -> t.diff(ot) }.filter { it.deltaGauchos != 0 || it.deltaOwnerUsername != null || it.deltaSpecialization != null }
            )
        }
    }

    class TownState(val name: String, val ownerUsername: String?, val gauchos: Int, val specialization: String) : State() {
        override fun diff(other: State): Change.TownChange {
            if (other !is TownState || this.name != other.name) throw CustomException.InternalServer.DiffException()
            return Change.TownChange(
                    townName = this.name,
                    deltaOwnerUsername = this.differentiate(this.ownerUsername, other.ownerUsername),
                    deltaSpecialization = this.differentiate(this.specialization, other.specialization),
                    deltaGauchos = this.gauchos - other.gauchos
            )
        }
    }

    abstract fun diff(other: State): Change

    protected fun <T>differentiate(final: T, initial: T) = if(final != initial) final else null
}