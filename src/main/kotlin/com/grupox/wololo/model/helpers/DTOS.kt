package com.grupox.wololo.model.helpers

import com.grupox.wololo.model.Coordinates
import com.grupox.wololo.model.Stats
import com.grupox.wololo.model.Status
import java.util.*

sealed class DTO {
    data class ExceptionDTO(val message: String?) : DTO()

    data class TownDTO(
        val id: Int,
        val name: String,
        val coordinates: Coordinates,
        val elevation: Double,
        val imageUrl: String,
        val ownerId: Int?,
        val specialization: String,
        val gauchos: Int,
        val isLocked: Boolean,
        val gauchosGeneratedByDefense : Int,
        val gauchosGeneratedByProduction : Int
    ) : DTO()

    data class ProvinceDTO(
        val name: String,
        val imageUrl: String,
        val towns: List<TownDTO>
    ) : DTO()

    data class GameDTO(
        val id: Int,
        val status: Status,
        val date: Date,
        val turnId: Int,
        val playerIds: List<Int>,
        val province: ProvinceDTO
    ) : DTO()

    data class UserDTO(
        val id: Int,
        val username: String,
        val stats: Stats
    ) : DTO()
}