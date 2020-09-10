package com.grupox.wololo.model

class Province(val id: Int, val name: String, val coordinates: Coordinates = Coordinates(0f, 0f), val towns: ArrayList<Town>) {
    fun getTownById(id: Int): Town? = towns.find { it.id == id }
}
