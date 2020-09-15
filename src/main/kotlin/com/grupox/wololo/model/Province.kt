package com.grupox.wololo.model

import arrow.core.*
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.helpers.AttackForm
import com.grupox.wololo.model.helpers.MovementForm

class Province(id: Int, val name: String, val towns: ArrayList<Town>){
    fun getTownById(id: Int): Either<CustomException.NotFound, Town> = towns.find { it.id == id }.rightIfNotNull { CustomException.NotFound.TownNotFoundException() }

    fun maxAltitude(): Double = towns.map { it.elevation }.max()!!

    fun minAltitude(): Double = towns.map { it.elevation }.min()!!

    fun multDistance(): Double = TODO("logica multDist")

    fun multAltitude(): Double = TODO("logica multAlt")

    fun townsFrom(user: User): List<Town> = towns.filter { it.isFrom(user) }

    fun addGauchosToAllTowns() {
        towns.forEach { it.addGauchos(maxAltitude(), minAltitude()) }
    }

    fun addGauchosToAllTownsFrom(user: User) {
        townsFrom(user).forEach { it.addGauchos(maxAltitude(), minAltitude()) }
    }

    fun unlockAllTownsFrom(user: User) {
        townsFrom(user).forEach { it.unlock() }
    }

    fun moveGauchosBetweenTowns(user: User, movementForm: MovementForm) {
        val fromTown = this.getTownById(movementForm.from).getOrHandle { throw it }
        val toTown = this.getTownById(movementForm.to).getOrHandle { throw it }
        if(!fromTown.isFrom(user) || !toTown.isFrom(user))
            throw CustomException.Forbidden.IllegalGauchoMovement("You only can move gauchos between your current towns")
        if(toTown.isLocked)
            throw CustomException.Forbidden.IllegalGauchoMovement("You only can move gauchos to a town that is unlocked")
        fromTown.giveGauchos(movementForm.gauchosQty)
        toTown.receiveGauchos(movementForm.gauchosQty)
    }

    fun attackTown(user: User, attackForm: AttackForm) {
        val attacker = this.getTownById(attackForm.from).getOrHandle { throw it }
        val defender = this.getTownById(attackForm.to).getOrHandle { throw it }
        if(!attacker.isFrom(user) || defender.isFrom(user))
            throw CustomException.Forbidden.IllegalAttack("You only can attack from your town to an enemy town")
        attacker.attack(defender, multDistance(), multAltitude())
    }
}