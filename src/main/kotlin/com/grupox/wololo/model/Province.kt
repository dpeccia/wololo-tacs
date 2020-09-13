package com.grupox.wololo.model

import arrow.core.getOrElse
import arrow.core.toOption
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.helpers.MovementForm

class Province(id: Int, val name: String, val towns: ArrayList<Town>){
    fun getTownById(id: Int): Town = towns.find { it.id == id }.toOption().getOrElse { throw CustomException.NotFoundException("Town was not found") }

    fun moveGauchosBetweenTowns(userId: Int, movementForm: MovementForm) {
        val fromTown = this.getTownById(movementForm.from)
        val toTown = this.getTownById(movementForm.to)
        if(!fromTown.isFrom(userId) || !toTown.isFrom(userId))
            throw CustomException.ForbiddenException("You only can move gauchos between your current towns")
        fromTown.giveGauchos(movementForm.gauchosQty)
        toTown.receiveGauchos(movementForm.gauchosQty)
    }
}