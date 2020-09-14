package com.grupox.wololo.model

import com.grupox.wololo.errors.CustomException
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

class Town(val id: Int, val name: String, val coordinates: Coordinates = Coordinates(0f,0f), val elevation: Double, var owner: User? = null){
    var isBlocked: Boolean = false
    var specialization : Specialization = Production()
    var gauchos = 0

    fun isFrom(userId: Int) = owner!!.id == userId

    fun multDefense(): Double = specialization.multDefense()

    fun addGauchos(maxAltitude: Double, minAltitude: Double) {
        gauchos += specialization.gauchos(elevation, maxAltitude, minAltitude)
    }

    fun giveGauchos(qty: Int) {
        if(qty > gauchos)
            throw CustomException.BadRequest.NotEnoughGauchosException(qty, gauchos)
        gauchos -= qty
    }

    fun receiveGauchos(qty: Int) {
        gauchos += qty
        this.isBlocked = true
    }

    fun attack(defender: Town, multDistance: Double, multAltitude: Double) {
        val gauchosAttackFinal = floor(gauchos * multDistance - defender.gauchos * multAltitude * this.multDefense()).toInt()
        defender.defend(this, multDistance, multAltitude)
        this.gauchos = max(gauchosAttackFinal, 0)
    }

    fun defend(attacker: Town, multDistance: Double, multAltitude: Double) {
        val gauchosDefenseFinal =
                ceil((gauchos * multAltitude * this.multDefense() - attacker.gauchos * multDistance) / (multAltitude * this.multDefense())).toInt()
        if(gauchosDefenseFinal <= 0)
            this.owner = attacker.owner
        this.gauchos = max(gauchosDefenseFinal, 0)
    }
}

