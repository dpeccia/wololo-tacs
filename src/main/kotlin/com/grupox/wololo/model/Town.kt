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
    //Esto podría ir en Town Stats si queremos hacerlo similar a User Stats, por ahora lo dejo acá para ir testeando al menos
    var gauchosGeneratedByDefense = 0
    var gauchosGeneratedByProduction = 0

    fun isFrom(user: User) = owner == user

    fun unlock() {
        isLocked = false
    }

    fun multDefense(): Double = specialization.multDefense()

    fun updateGauchosByDefense(gauchosAmount: Int){
        this.gauchosGeneratedByDefense = gauchosAmount + gauchosGeneratedByDefense
        }

    fun updateGauchosByProduction(gauchosAmount: Int){
        this.gauchosGeneratedByProduction = gauchosAmount + gauchosGeneratedByProduction
    }

    fun addGauchos(maxAltitude: Double, minAltitude: Double) {
        val gauchosAmount: Int = specialization.gauchos(elevation, maxAltitude, minAltitude)
        gauchos += gauchosAmount
        specialization.updateStats(elevation, maxAltitude, minAltitude, this)
    }

    fun giveGauchos(qty: Int) {
        if(qty > gauchos)
            throw CustomException.BadRequest.NotEnoughGauchosException(qty, gauchos)
        gauchos -= qty
    }

    fun receiveGauchos(qty: Int) {
        gauchos += qty
        isLocked = true
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

