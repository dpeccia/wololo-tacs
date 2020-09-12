package com.grupox.wololo.model

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.toOption
import com.grupox.wololo.errors.CustomException

class Game(val id: Int, val province: Province, status: Status = Status.NEW) {
    var status: Status = status
        private set

    fun changeStatus(newStatus: Status){
        status = newStatus
    }

    fun getTownById(idTown: Int): Option<Town> = province.getTownById(idTown).toOption()

    fun changeTownSpecialization(townId: Int, specialization: Specialization){
    this.getTownById(townId).getOrElse { throw CustomException.NotFoundException("Town was not found") }.changeSpecialization(specialization)
    }
}
