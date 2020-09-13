package com.grupox.wololo.model

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.toOption
import java.time.Duration
import java.time.Instant
import java.util.Date
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.helpers.MovementForm


class Game(val id: Int , val date: Date, val players: List<User>, val province: Province, var status: Status = Status.NEW) {

    //val id: Int = 0 // TODO: Autogenerada

    val townsAmount: Int
        get() = province.towns.size

    val playerAmount: Int
        get() = players.size

    lateinit var turno: User

    init {
        assignTowns()
    }

    fun getTownById(idTown: Int): Option<Town> = province.towns.find { it.id == idTown }.toOption()

    fun getMember(userId: Int): Option<User> = players.find { it.id == userId }.toOption()

    fun changeTownSpecialization(townId: Int, specialization: Specialization) {
        this.getTownById(townId).getOrElse { throw CustomException.NotFoundException("Town was not found") }.specialization = specialization
    }

    private fun assignTowns() {  // Este metodo puede modificarse para hacer algun algoritmo mas copado.
        if (townsAmount < playerAmount) throw CustomException.ModelException.IlegalGameException("There is not enough towns for the given players")
        else if (players.isEmpty()) throw CustomException.ModelException.IlegalGameException("There is not enough players")

        val townGroups = province.towns.shuffled().chunked(townsAmount / playerAmount)
        townGroups.zip(players).forEach { (townGroup, player) -> townGroup.forEach { it.owner = player } }
    }

    fun moveGauchosBetweenTowns(userId: Int, movementForm: MovementForm) {
        if(turno.id != userId)
            throw CustomException.ForbiddenException("It´s not your Turn to play")
        province.moveGauchosBetweenTowns(userId, movementForm)
    }
}
