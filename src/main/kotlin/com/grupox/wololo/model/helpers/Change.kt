package com.grupox.wololo.model.helpers

import com.grupox.wololo.model.Status
import org.bson.types.ObjectId

sealed class Change {
    data class TownChange(
        val id: ObjectId,
        val deltaGauchos: Int,
        val deltaSpecialization: String?,
        val deltaOwnerId: ObjectId?
    ) : Change()

    data class GameChange(
        val id: ObjectId,
        val deltaTurnId: ObjectId?,
        val deltaStatus: Status?,
        val deltaTowns: List<TownChange>
    ) : Change()
}