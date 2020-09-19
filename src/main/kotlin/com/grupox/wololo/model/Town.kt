package com.grupox.wololo.model

import com.grupox.wololo.errors.CustomException
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

class Town(val id: Int, val name: String, val coordinates: Coordinates = Coordinates(0f,0f), val elevation: Double, val townImage: String = ""){
    var owner: User? = null

    var isLocked: Boolean = false
    var specialization : Specialization = Production()
    var gauchos = 0

    fun isFrom(user: User) = owner == user

    fun unlock() {
        isLocked = false
    }

    fun multDefense(): Double = specialization.multDefense()

    fun addGauchos(maxAltitude: Double, minAltitude: Double) {
        gauchos += specialization.gauchos(elevation, maxAltitude, minAltitude)
    }

    fun giveGauchos(qty: Int) {
        if (qty <= 0) throw CustomException.BadRequest.IllegalGauchosQtyException()
        if (qty > gauchos) throw CustomException.BadRequest.NotEnoughGauchosException(qty, gauchos)
        gauchos -= qty
    }

    fun receiveGauchos(qty: Int) {
        if (qty <= 0) throw CustomException.BadRequest.IllegalGauchosQtyException()
        gauchos += qty
        isLocked = true
    }

    fun attack(defenderQty: Int, multDistance: Double, multAltitude: Double) {
        val gauchosAttackFinal = floor(gauchos * multDistance - defenderQty * multAltitude * this.multDefense()).toInt()
        this.gauchos = max(gauchosAttackFinal, 0)
    }

    fun defend(attackerOwner: User, attackerQty: Int, multDistance: Double, multAltitude: Double) {
        val gauchosDefenseFinal =
                ceil((gauchos * multAltitude * this.multDefense() - attackerQty * multDistance) / (multAltitude * this.multDefense())).toInt()
        if(gauchosDefenseFinal <= 0)
            this.owner = attackerOwner
        this.gauchos = max(gauchosDefenseFinal, 0)
    }
}

