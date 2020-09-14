package com.grupox.wololo.model

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.toOption
import java.time.Instant
import java.util.Date
import com.grupox.wololo.errors.CustomException


class Game(val id: Int , val players: List<User>, val province: Province, var status: Status = Status.NEW) {

    //val id: Int = 0 // TODO: Autogenerada

    val townsAmount: Int
        get() = province.towns.size

    val playerAmount: Int
        get() = players.size

    var date: Date = Date()

    init {
        this.date = Date.from(Instant.now())
        assignTowns()
    }

    fun getTownById(idTown: Int): Option<Town> = province.towns.find { it.id == idTown }.toOption()

    fun changeTownSpecialization(townId: Int, specialization: Specialization) {
        this.getTownById(townId).getOrElse { throw CustomException.NotFoundException("Town was not found") }.specialization = specialization
    }

    fun isParticipating(user: User): Boolean = players.contains(user)

    private fun assignTowns() {  // Este metodo puede modificarse para hacer algun algoritmo mas copado.
        if (townsAmount < playerAmount) throw CustomException.ModelException.IllegalGameException("There is not enough towns for the given players")
        else if (players.isEmpty()) throw CustomException.ModelException.IllegalGameException("There is not enough players")

        val townGroups = province.towns.shuffled().chunked(townsAmount / playerAmount)
        townGroups.zip(players).forEach { (townGroup, player) -> townGroup.forEach { it.owner = player } }
    }
}
