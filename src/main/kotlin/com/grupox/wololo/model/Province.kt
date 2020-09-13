package com.grupox.wololo.model

import arrow.core.getOrElse
import arrow.core.toOption
import com.grupox.wololo.errors.CustomException

class Province(id: Int, val name: String, val towns: ArrayList<Town>){
    fun getTownById(id: Int): Town = towns.find { it.id == id }.toOption().getOrElse { throw CustomException.NotFoundException("Town was not found") }

}
