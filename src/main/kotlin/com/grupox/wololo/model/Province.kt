package com.grupox.wololo.model

class Province(val id: Int, val name: String, val towns: ArrayList<Town>, val coordinates: Coordinates = Coordinates(0f, 0f)) {
    fun getTownById(id: Int): Town? = towns.find { it.id == id }
}
