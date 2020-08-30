package com.grupox.wololo.model

import com.grupox.wololo.model.dtos.DTO
import com.grupox.wololo.model.dtos.Entity
import com.grupox.wololo.model.dtos.GameDTO
import java.util.*

class Game(val id: Int, private var status: Status) : Entity {
    override fun getDTO(): DTO = GameDTO(id, status)
}
