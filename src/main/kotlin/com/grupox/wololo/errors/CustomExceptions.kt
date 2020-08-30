package com.grupox.wololo.errors

import com.grupox.wololo.model.dtos.DTO
import com.grupox.wololo.model.dtos.Entity
import com.grupox.wololo.model.dtos.ExceptionDTO

class NotFoundException(message: String) : Exception(message), Entity {
    override fun getDTO(): DTO = ExceptionDTO(message)
}
