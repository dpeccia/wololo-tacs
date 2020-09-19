package com.grupox.wololo.model

import arrow.core.Either
import arrow.core.rightIfNotNull
import com.grupox.wololo.errors.CustomException.*
import com.grupox.wololo.errors.CustomException.Forbidden.*
import com.grupox.wololo.errors.CustomException.NotFound.*
import com.grupox.wololo.model.helpers.AttackForm
import com.grupox.wololo.model.helpers.MovementForm
import com.grupox.wololo.model.helpers.getOrThrow

class Province(id: Int, val name: String, val towns: ArrayList<Town>, val imageUrl: String = ""){
    fun getTownById(id: Int): Either<NotFound, Town> = towns.find { it.id == id }.rightIfNotNull { TownNotFoundException() }

    private fun maxAltitude(): Double = towns.map { it.elevation }.max()!!

    private fun minAltitude(): Double = towns.map { it.elevation }.min()!!

    private fun distanceBetween(attacker: Town, defender: Town): Double = TODO("implement")

    private fun maxDistance(): Double = TODO("implement")

    private fun minDistance(): Double = TODO("implement")

    private fun multDistance(attacker: Town, defender: Town): Double =
            1 - ((distanceBetween(attacker, defender) - minDistance()) / (2 * (maxDistance() - minDistance())))

    private fun multAltitude(defender: Town): Double =
            1 + ((defender.elevation - minAltitude()) / (2 * (maxAltitude() - minAltitude())))

    private fun townsFrom(user: User): List<Town> = towns.filter { it.isFrom(user) }

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

    private fun checkIllegalMovements(user: User, fromTown: Town, toTown: Town) {
        when {
            toTown.id == fromTown.id -> throw IllegalGauchoMovement("You can´t move gauchos from a town to itself")
            !fromTown.isFrom(user) || !toTown.isFrom(user) -> throw IllegalGauchoMovement("You only can move gauchos between your current towns")
            toTown.isLocked -> throw IllegalGauchoMovement("You only can move gauchos to a town that is unlocked")
        }
    }

    fun moveGauchosBetweenTowns(user: User, movementForm: MovementForm) {
        val fromTown = this.getTownById(movementForm.from).getOrThrow()
        val toTown = this.getTownById(movementForm.to).getOrThrow()
        checkIllegalMovements(user, fromTown, toTown)
        fromTown.giveGauchos(movementForm.gauchosQty)
        toTown.receiveGauchos(movementForm.gauchosQty)
    }

    fun attackTown(user: User, attackForm: AttackForm) {
        val attacker = this.getTownById(attackForm.from).getOrThrow()
        val defender = this.getTownById(attackForm.to).getOrThrow()
        if(!attacker.isFrom(user) || defender.isFrom(user))
            throw IllegalAttack("You only can attack from your town to an enemy town")
        val attackerQtyBeforeAttack = attacker.gauchos
        val multDist = multDistance(attacker, defender)
        val multAlt = multAltitude(defender)
        attacker.attack(defender.gauchos, multDist, multAlt)
        defender.defend(attacker.owner!!, attackerQtyBeforeAttack, multDist, multAlt)
    }
}