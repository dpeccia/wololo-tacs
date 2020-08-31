package com.grupox.wololo.model

import com.grupox.wololo.model.dtos.DTO
import com.grupox.wololo.model.dtos.Entity
import com.grupox.wololo.model.dtos.ProvinceDTO

class Province(val id: Int, private var name: String, private var towns: ArrayList<Town>) : Entity {
    override fun getDTO(): DTO = ProvinceDTO(id, name, towns.map { it.getDTO() })
    fun getTownById(id: Int): Town? = towns.find { it.id == id }
}
