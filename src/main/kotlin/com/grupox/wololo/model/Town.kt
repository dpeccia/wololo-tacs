package com.grupox.wololo.model

import com.grupox.wololo.errors.CustomException

class Town(val id: Int, val name: String, val coordinates: Coordinates = Coordinates(0f,0f), val elevation: Float, var owner: User? = null){
    var specialization : Specialization = Production()
    var gauchos = 0

    fun isFrom(userId: Int) = owner!!.id == userId

    fun giveGauchos(qty: Int) {
        if(qty > gauchos)
            throw CustomException.BadRequestException("You want to move $qty gauchos, when there are only $gauchos in this Town")
        gauchos -= qty
    }

    fun receiveGauchos(qty: Int) {
        gauchos += qty
    }

}

