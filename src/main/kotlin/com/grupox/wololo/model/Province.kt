package com.grupox.wololo.model

import com.grupox.wololo.model.dtos.DTO
import java.util.ArrayList
import com.grupox.wololo.model.dtos.Entity
import com.grupox.wololo.model.dtos.ProvinceDTO

class Province(var id: Int, var name: String, var towns: ArrayList<Town>) : Entity {
    override fun getDTO(): DTO = ProvinceDTO(id, name, towns)
    fun getTownById(id: Int): Town? = towns.find { it.id == id }

}
