package com.grupox.wololo.model

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.toOption
import com.grupox.wololo.errors.CustomException

class Game(val id: Int , val players: List<User> = listOf(), val province: Province, var status: Status = Status.NEW) {
    //val id: Int = 0 // TODO: Autogenerada
    fun getTownById(idTown: Int): Option<Town> = province.getTownById(idTown).toOption()

    fun changeTownSpecialization(townId: Int, specialization: Specialization) {
        this.getTownById(townId).getOrElse { throw CustomException.NotFoundException("Town was not found") }.specialization = specialization


    }
}