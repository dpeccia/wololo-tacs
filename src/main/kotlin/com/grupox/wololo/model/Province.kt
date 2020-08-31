package com.grupox.wololo.model

class Province(val id: Int, val name: String, val towns: ArrayList<Town>) {
    fun getTownById(id: Int): Town? = towns.find { it.id == id }
}
