package com.grupox.wololo.model

import com.grupox.wololo.model.dtos.DTO
import com.grupox.wololo.model.dtos.Entity
import com.grupox.wololo.model.dtos.TownDTO

class Town(val id: Int, private var name: String) : Entity {
    override fun getDTO(): DTO = TownDTO(id, name)
}
