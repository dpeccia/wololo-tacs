package com.grupox.wololo.model

import arrow.core.Either
import arrow.core.rightIfNotNull
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.helpers.AttackForm
import com.grupox.wololo.model.helpers.MovementForm
import com.grupox.wololo.model.helpers.getOrThrow

class Province(id: Int, val name: String, val towns: ArrayList<Town>, val imageUrl: String = ""){
    fun getTownById(id: Int): Either<CustomException.NotFound, Town> = towns.find { it.id == id }.rightIfNotNull { CustomException.NotFound.TownNotFoundException() }

    fun maxAltitude(): Double = towns.map { it.elevation }.max()!!

    fun minAltitude(): Double = towns.map { it.elevation }.min()!!

    fun multDistance(): Double = TODO("logica multDist")

    fun multAltitude(): Double = TODO("logica multAlt")

    fun townsFrom(user: User): List<Town> = towns.filter { it.isFrom(user) }

    fun allOccupiedTownsAreFrom(user: User): Boolean = towns.filter { it.owner != null }.stream().allMatch { it.owner == user }

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
        val fromTown = this.getTownById(movementForm.from).getOrThrow()
        val toTown = this.getTownById(movementForm.to).getOrThrow()
        if(!fromTown.isFrom(user) || !toTown.isFrom(user))
            throw CustomException.Forbidden.IllegalGauchoMovement("You only can move gauchos between your current towns")
        if(toTown.isLocked)
            throw CustomException.Forbidden.IllegalGauchoMovement("You only can move gauchos to a town that is unlocked")
        fromTown.giveGauchos(movementForm.gauchosQty)
        toTown.receiveGauchos(movementForm.gauchosQty)
    }

    fun attackTown(user: User, attackForm: AttackForm) {
        val attacker = this.getTownById(attackForm.from).getOrThrow()
        val defender = this.getTownById(attackForm.to).getOrThrow()
        if(!attacker.isFrom(user) || defender.isFrom(user))
            throw CustomException.Forbidden.IllegalAttack("You only can attack from your town to an enemy town")
        attacker.attack(defender, multDistance(), multAltitude())
    }
}