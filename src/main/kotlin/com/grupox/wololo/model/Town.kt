package com.grupox.wololo.model

class Town(
        val id: Int,
        val name: String,
        val coordinates: Coordinates = Coordinates(0f,0f),
        val elevation: Float,
        var owner: User? = null
){
    var specialization : Specialization = Production()

}

