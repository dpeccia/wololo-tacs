package com.grupox.wololo.model

import com.grupox.wololo.model.dtos.DTO
import com.grupox.wololo.model.dtos.Entity
import com.grupox.wololo.model.dtos.GameDTO

class Game(val id: Int, private var status: Status, var province: Province) : Entity {
    override fun getDTO(): DTO = GameDTO(id, status, province)

    fun getTownById(idTown: Int): Town ?= province.getTownById(idTown)
}
