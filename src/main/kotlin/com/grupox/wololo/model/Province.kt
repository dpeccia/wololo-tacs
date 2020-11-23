package com.grupox.wololo.model

import arrow.core.Either
import arrow.core.rightIfNotNull
import com.grupox.wololo.errors.CustomException.Forbidden.IllegalAttack
import com.grupox.wololo.errors.CustomException.Forbidden.IllegalGauchoMovement
import com.grupox.wololo.errors.CustomException.NotFound
import com.grupox.wololo.errors.CustomException.NotFound.TownNotFoundException
import com.grupox.wololo.model.helpers.*
import kotlin.math.*

class Province(val name: String, val towns: ArrayList<Town>) : Requestable {

    fun getTownById(id: Int): Either<NotFound, Town> = towns.find { it.id == id }.rightIfNotNull { TownNotFoundException() }

    private fun altitudes() = towns.map { it.elevation }

    private fun distances() = towns.map { town1 -> towns.map { town2 -> distanceBetween(town1, town2) } }.flatten().toSet().filter { it != 0.0 }

    var maxAltitude = altitudes().max()

    var minAltitude = altitudes().min()

    var maxDistance = distances().max()

    var minDistance = distances().min()

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
            1 - ((distanceBetween(attacker, defender) - minDistance!!) / (2 * (maxDistance!! - minDistance!!)))

    private fun multAltitude(defender: Town): Double =
            1 + ((defender.elevation - minAltitude!!) / (2 * (maxAltitude!! - minAltitude!!)))

    fun townsFrom(user: User): List<Town> = towns.filter { it.isFrom(user) }

    fun allOccupiedTownsAreFrom(user: User): Boolean =
            towns.filter { it.owner != null }.all { it.isFrom(user) }

    fun addGauchosToAllTowns() {
        towns.forEach { it.addGauchos(maxAltitude!!, minAltitude!!) }
    }

    fun addGauchosToAllTownsFrom(user: User) {
        townsFrom(user).forEach { it.addGauchos(maxAltitude!!, minAltitude!!) }
    }

    fun unlockAllTownsFrom(user: User) {
        townsFrom(user).forEach { it.unlock() }
    }

    private fun areNotBorderingTowns(town: Town, otherTown: Town) = !town.borderingTowns.contains(otherTown.name)

    private fun checkIllegalMovements(user: User, fromTown: Town, toTown: Town) {
        when {
            toTown.id == fromTown.id -> throw IllegalGauchoMovement("You can´t move gauchos from a town to itself")
            !fromTown.isFrom(user) || !toTown.isFrom(user) -> throw IllegalGauchoMovement("You only can move gauchos between your current towns")
            areNotBorderingTowns(fromTown, toTown) -> throw IllegalGauchoMovement("You only can move gauchos between towns that are bordering")
            toTown.isLocked -> throw IllegalGauchoMovement("You only can move gauchos to a town that is unlocked")
        }
    }

    private fun checkIllegalAttacks(user: User, attacker: Town, defender: Town) {
        when {
            !attacker.isFrom(user) || defender.isFrom(user) -> throw IllegalAttack("You only can attack from your town to an enemy town")
            areNotBorderingTowns(attacker, defender) -> throw IllegalAttack("You only can attack a bordering town")
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
        checkIllegalAttacks(user, attacker, defender)
        val attackerQtyBeforeAttack = attacker.gauchos
        val multDist = multDistance(attacker, defender)
        val multAlt = multAltitude(defender)
        attacker.attack(defender.gauchos, multDist, multAlt)
        defender.defend(attacker.owner!!, attackerQtyBeforeAttack, multDist, multAlt)
    }

    private fun calculateCentroid(): Coordinates {
        val townAmount = towns.size
        val averageLon: Float = towns.map { it.coordinates.longitude }.sum() / townAmount
        val averageLat: Float = towns.map { it.coordinates.latitude }.sum() / townAmount
        return Coordinates(longitude = averageLon, latitude = averageLat)
    }

    override fun dto(): DTO.ProvinceDTO =
        DTO.ProvinceDTO(
            name = name,
            centroid = calculateCentroid(),
            towns = towns.map { it.dto() }
        )
}