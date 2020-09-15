package com.grupox.wololo.model

import arrow.core.*
import java.time.Instant
import java.util.Date
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.helpers.AttackForm
import com.grupox.wololo.model.helpers.MovementForm


class Game(val id: Int , val players: List<User>, val province: Province, var status: Status = Status.NEW) {
    val townsAmount: Int
        get() = province.towns.size

    val playerAmount: Int
        get() = players.size

    //val id: Int = 0 // TODO: Autogenerada

    lateinit var turno: User
    
    var date: Date = Date.from(Instant.now())

    init {
        assignTowns()
    }

    fun getTownById(idTown: Int): Either<CustomException.NotFound, Town> = province.towns.find { it.id == idTown }.rightIfNotNull { CustomException.NotFound.TownNotFoundException() }

    fun getMember(userId: Int): Either<CustomException.NotFound, User> = players.find { it.id == userId }.rightIfNotNull { CustomException.NotFound.MemberNotFoundException() }

    fun changeTownSpecialization(townId: Int, specialization: Specialization) {
        getTownById(townId).getOrHandle { throw it }.specialization = specialization
    }

    fun isParticipating(user: User): Boolean = players.contains(user)

    fun isParticipating(userId: Int): Boolean = players.any { it.id == userId }

    private fun assignTowns() {  // Este metodo puede modificarse para hacer algun algoritmo mas copado.
        if (townsAmount < playerAmount) throw CustomException.BadRequest.IllegalGameException("There is not enough towns for the given players")
        else if (players.isEmpty()) throw CustomException.BadRequest.IllegalGameException("There is not enough players")

        val townGroups = province.towns.shuffled().chunked(townsAmount / playerAmount)
        townGroups.zip(players).forEach { (townGroup, player) -> townGroup.forEach { it.owner = player } }
    }

    //cuando empieza el turno desbloquear todos mis towns y agregar gauchos a todos mis towns

    fun moveGauchosBetweenTowns(userId: Int, movementForm: MovementForm) {
        if (turno.id != userId) throw CustomException.Forbidden.NotYourTurnException()
        province.moveGauchosBetweenTowns(userId, movementForm)
    }

    fun attackTown(userId: Int, attackForm: AttackForm) {
        if (turno.id != userId) throw CustomException.Forbidden.NotYourTurnException()
        province.attackTown(userId, attackForm)
    }
}
