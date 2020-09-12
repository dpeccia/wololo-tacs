package com.grupox.wololo.model


class Game(val players: List<User> = listOf(), val province: Province, var status: Status = Status.NEW) {
    val id: Int = 0 // TODO: Autogenerada
    fun getTownById(idTown: Int): Town? = province.towns.find { it.id == idTown }

    fun changeTownSpecialization(townId: Int, specialization: Specialization){
        this.getTownById(townId)?.specialization = specialization
    }
}
