package com.grupox.wololo.model.helpers

import com.grupox.wololo.model.Status

sealed class Change {
    data class TownChange(
        val townName: String,
        val deltaGauchos: Int,
        val deltaSpecialization: String?,
        val deltaOwnerUsername: String?
    ) : Change()

    data class GameChange(
        val id: String,
        val deltaTurnUsername: String?,
        val deltaStatus: Status?,
        val deltaTowns: List<TownChange>
    ) : Change()
}