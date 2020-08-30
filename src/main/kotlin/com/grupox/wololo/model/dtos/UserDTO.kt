package com.grupox.wololo.model.dtos

data class UserDTO(val id: Int,
                   val nombre: String,
                   val mail: String,
                   val esAdmin: Boolean): DTO