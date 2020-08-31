package com.grupox.wololo.model.dtos

import com.grupox.wololo.model.Status

sealed class DTO

data class GameDTO(val id: Int, val status: Status, val province: DTO) : DTO()

data class ExceptionDTO(val message: String?) : DTO()

data class UserDTO(
    val id: Int,
    val nombre: String,
    val mail: String,
    val esAdmin: Boolean
) : DTO()

data class ProvinceDTO(
    val id: Int,
    val name: String,
    val towns: List<DTO>
) : DTO()

data class TownDTO(val id: Int, val name: String) : DTO()
