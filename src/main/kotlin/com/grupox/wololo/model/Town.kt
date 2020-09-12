package com.grupox.wololo.model

class Town(
        val name: String,
        val coordinates: Coordinates = Coordinates(0f,0f),
        val elevation: Float,
        var owner: User? = null
){
    val id: Int = 0
    var specialization : Specialization = Production()

}

