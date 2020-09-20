package com.grupox.wololo.model

import arrow.core.Either
import arrow.core.extensions.list.foldable.combineAll
import arrow.core.extensions.list.semigroupK.combineK
import arrow.core.rightIfNotNull
import com.google.common.collect.Sets
import com.grupox.wololo.errors.CustomException.Forbidden.IllegalAttack
import com.grupox.wololo.errors.CustomException.Forbidden.IllegalGauchoMovement
import com.grupox.wololo.errors.CustomException.NotFound
import com.grupox.wololo.errors.CustomException.NotFound.TownNotFoundException
import com.grupox.wololo.model.helpers.AttackForm
import com.grupox.wololo.model.helpers.MovementForm
import com.grupox.wololo.model.helpers.getOrThrow
import kotlin.math.*

class Province(id: Int, val name: String, val towns: ArrayList<Town>, val imageUrl: String = ""){
    fun getTownById(id: Int): Either<NotFound, Town> = towns.find { it.id == id }.rightIfNotNull { TownNotFoundException() }

    private val altitudes = towns.map { it.elevation }

    private val distances =
            towns.map { town1 -> towns.map { town2 -> distanceBetween(town1, town2) } }.flatten().toSet().filter { it != 0.0 }

    val maxAltitude = altitudes.max()

    val minAltitude = altitudes.min()

    val maxDistance = distances.max()

    val minDistance = distances.min()

    // implementation of the Haversine method which also takes into account elevation differences between two towns
    fun distanceBetween(town1: Town, town2: Town): Double {
        val R = 6371 // Radius of the earth
        val lat1 = town1.coordinates.latitude.toDouble()
        val lon1 = town1.coordinates.longitude.toDouble()
        val lat2 = town2.coordinates.latitude.toDouble()
        val lon2 = town2.coordinates.longitude.toDouble()

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = (sin(latDistance / 2) * sin(latDistance / 2)
                + (cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2))
                * sin(lonDistance / 2) * sin(lonDistance / 2)))
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        var distance = R * c * 1000 // convert to meters
        val height: Double = town1.elevation - town2.elevation
        distance = distance.pow(2.0) + height.pow(2.0)
        return sqrt(distance) // meters
    }

    private fun multDistance(attacker: Town, defender: Town): Double =
            1 - ((distanceBetween(attacker, defender) - minDistance!!) / (2 * (maxDistance!! - minDistance)))

    private fun multAltitude(defender: Town): Double =
            1 + ((defender.elevation - minAltitude!!) / (2 * (maxAltitude!! - minAltitude)))

    private fun townsFrom(user: User): List<Town> = towns.filter { it.isFrom(user) }

    fun allOccupiedTownsAreFrom(user: User): Boolean = towns.filter { it.owner != null }.stream().allMatch { it.owner == user }

    fun addGauchosToAllTowns() {
        towns.forEach { it.addGauchos(maxAltitude!!, minAltitude!!) }
    }

    fun addGauchosToAllTownsFrom(user: User) {
        townsFrom(user).forEach { it.addGauchos(maxAltitude!!, minAltitude!!) }
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