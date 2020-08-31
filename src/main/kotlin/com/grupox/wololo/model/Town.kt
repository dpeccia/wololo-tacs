package com.grupox.wololo.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.grupox.wololo.model.dtos.DTO
import com.grupox.wololo.model.dtos.TownDTO
import com.grupox.wololo.model.dtos.Entity

class Town(var id: Int, var name: String) : Entity {
    override fun getDTO(): DTO = TownDTO(id, name)
}