package com.grupox.wololo.model.dtos

import com.grupox.wololo.model.Status

sealed class DTO

data class GameDTO(val id: Int, val status: Status) : DTO()

data class ExceptionDTO(val message: String?) : DTO()

data class UserDTO(val id: Int,
                   val nombre: String,
                   val mail: String,
                   val esAdmin: Boolean): DTO()