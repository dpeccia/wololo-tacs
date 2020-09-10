package com.grupox.wololo.model

class Town(
        val id: Int,
        val name: String,
        val coordinates: Coordinates = Coordinates(0f,0f), specialization : Specialization, val stats : TownStats){

    var specialization : Specialization = specialization
    private set

    fun changeSpecialization(specialization: Specialization){
     this.specialization = specialization
    }

}

