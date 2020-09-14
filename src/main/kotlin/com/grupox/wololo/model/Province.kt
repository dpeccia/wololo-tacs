package com.grupox.wololo.model

import arrow.core.getOrElse
import arrow.core.toOption
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.helpers.AttackForm
import com.grupox.wololo.model.helpers.MovementForm

class Province(id: Int, val name: String, val towns: ArrayList<Town>){
    fun getTownById(id: Int): Town = towns.find { it.id == id }.toOption().getOrElse { throw CustomException.NotFoundException("Town was not found") }

    fun maxAltitude(): Double = towns.map { it.elevation }.max()!!

    fun minAltitude(): Double = towns.map { it.elevation }.min()!!

    fun multDistance(): Double = TODO("logica multDist")

    fun multAltitude(): Double = TODO("logica multAlt")

    // addGauchosToAllMyTowns

    // desbloquearTodosMisTowns

    fun moveGauchosBetweenTowns(userId: Int, movementForm: MovementForm) {
        val fromTown = this.getTownById(movementForm.from)
        val toTown = this.getTownById(movementForm.to)
        if(!fromTown.isFrom(userId) || !toTown.isFrom(userId))
            throw CustomException.ForbiddenException("You only can move gauchos between your current towns")
        if(toTown.isBlocked)
            throw CustomException.ForbiddenException("You only can move gauchos to a town that is not blocked")
        fromTown.giveGauchos(movementForm.gauchosQty)
        toTown.receiveGauchos(movementForm.gauchosQty)
    }

    fun attackTown(userId: Int, attackForm: AttackForm) {
        val attacker = this.getTownById(attackForm.from)
        val defender = this.getTownById(attackForm.to)
        if(!attacker.isFrom(userId) || defender.isFrom(userId))
            throw CustomException.ForbiddenException("You only can attack from your town to an enemy town")
        attacker.attack(defender, multDistance(), multAltitude())
    }
}