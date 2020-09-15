package com.grupox.wololo.model

import arrow.core.*
import java.util.Date
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.helpers.AttackForm
import com.grupox.wololo.model.helpers.MovementForm
import java.time.Instant


class Game(val id: Int , val players: List<User>, val province: Province, var status: Status = Status.NEW) {

    //val id: Int = 0 // TODO: Autogenerada

    val townsAmount: Int
        get() = province.towns.size

    val playerAmount: Int
        get() = players.size

    lateinit var turn: User

    var date: Date = Date.from(Instant.now())

    init {
        assignTowns()
        startTurn()
    }

    fun getTownById(idTown: Int): Either<CustomException.NotFound, Town> = province.towns.find { it.id == idTown }.rightIfNotNull { CustomException.NotFound.TownNotFoundException() }

    fun getMember(userId: Int): Either<CustomException.NotFound, User> = players.find { it.id == userId }.rightIfNotNull { CustomException.NotFound.MemberNotFoundException() }

    fun isParticipating(user: User): Boolean = players.contains(user)

    private fun assignTowns() {  // Este metodo puede modificarse para hacer algun algoritmo mas copado.
        if (townsAmount < playerAmount) throw CustomException.BadRequest.IllegalGameException("There is not enough towns for the given players")
        else if (players.isEmpty()) throw CustomException.BadRequest.IllegalGameException("There is not enough players")

        val townGroups = province.towns.shuffled().chunked(townsAmount / playerAmount)
        townGroups.zip(players).forEach { (townGroup, player) -> townGroup.forEach { it.owner = player } }
    }

    //cuando termina el turno desbloquear todos mis towns y agregar gauchos a todos mis towns

    private fun startTurn() {
        turn = players.shuffled().first()
        province.addGauchosToAllTowns()
    }

    fun finishTurn(user: User) {
        checkForbiddenAction(user)
        province.unlockAllTownsFrom(user)
        changeTurn()
    }

    fun changeTurn() {
        val playersIterator = players.listIterator(players.indexOf(turn) + 1)
        turn = if(playersIterator.hasNext())
            playersIterator.next()
        else
            players.first()
        province.addGauchosToAllTownsFrom(turn)
    }

    fun changeTownSpecialization(user: User, townId: Int, specialization: Specialization) {
        checkForbiddenAction(user)
        val town = getTownById(townId).getOrHandle { throw it }
        if(town.owner != user) throw CustomException.Forbidden.NotYourTownException()
        town.specialization = specialization
    }

    fun moveGauchosBetweenTowns(user: User, movementForm: MovementForm) {
        checkForbiddenAction(user)
        province.moveGauchosBetweenTowns(user, movementForm)
    }

    fun attackTown(user: User, attackForm: AttackForm) {
        checkForbiddenAction(user)
        province.attackTown(user, attackForm)
    }

    private fun checkForbiddenAction(user: User) {
        if (!isParticipating(user)) throw CustomException.Forbidden.NotAMemberException()
        if (turn != user) throw CustomException.Forbidden.NotYourTurnException()
    }
}