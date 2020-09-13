package com.grupox.wololo.model

import arrow.core.extensions.list.zip.zipWith


class Game(val players: List<User> = listOf(), val province: Province, var status: Status = Status.NEW) {
    val id: Int = 0 // TODO: Autogenerada

    val townsAmount: Int
        get() = province.towns.size

    val playerAmount: Int
        get() = players.size

    init {
        assignTowns()
    }

    fun getTownById(idTown: Int): Town? = province.towns.find { it.id == idTown }

    fun changeTownSpecialization(townId: Int, specialization: Specialization){
        this.getTownById(townId)?.specialization = specialization
    }

    private fun assignTowns() {  // Este metodo puede modificarse para hacer algun algoritmo mas copado.
        val townGroups = province.towns.shuffled().chunked(townsAmount / playerAmount)

        townGroups.zip(players).forEach { (townGroup, player) -> townGroup.forEach { it.owner = player } }
    }
}
