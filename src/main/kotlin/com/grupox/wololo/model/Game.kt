package com.grupox.wololo.model

import arrow.core.*
import java.util.Date
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.helpers.AttackForm
import com.grupox.wololo.model.helpers.MovementForm
import com.grupox.wololo.model.helpers.getOrThrow
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
        startGame()
    }

    private fun assignTowns() {  // Este metodo puede modificarse para hacer algun algoritmo mas copado.
        if (townsAmount < playerAmount) throw CustomException.BadRequest.IllegalGameException("There is not enough towns for the given players")
        else if (players.isEmpty()) throw CustomException.BadRequest.IllegalGameException("There is not enough players")

        val townGroups = province.towns.shuffled().chunked(townsAmount / playerAmount)
        townGroups.zip(players).forEach { (townGroup, player) -> townGroup.forEach { it.owner = player } }
    }

    private fun startGame() {
        turn = players.shuffled().first()
        province.addGauchosToAllTowns()
        status = Status.ONGOING
    }

    private fun changeTurn() {
        val playersIterator = players.listIterator(players.indexOf(turn) + 1)
        turn = if(playersIterator.hasNext())
            playersIterator.next()
        else
            players.first()
        province.addGauchosToAllTownsFrom(turn)
    }

    private fun checkForbiddenAction(user: User) {
        if (status == Status.FINISHED || status == Status.CANCELED) throw CustomException.Forbidden.FinishedGameException()
        if (!isParticipating(user)) throw CustomException.Forbidden.NotAMemberException()
        if (turn != user) throw CustomException.Forbidden.NotYourTurnException()
    }

    private fun userWon(user: User): Boolean = province.allOccupiedTownsAreFrom(user)

    fun getMember(userId: Int): Either<CustomException.NotFound, User> = players.find { it.id == userId }.rightIfNotNull { CustomException.NotFound.MemberNotFoundException() }

    fun isParticipating(user: User): Boolean = players.contains(user)

    fun finishTurn(user: User) {
        checkForbiddenAction(user)
        province.unlockAllTownsFrom(user)
        if(userWon(user)) {
            status = Status.FINISHED
            // TODO update user and game stats
        }
        else
            changeTurn()
    }

    fun changeTownSpecialization(user: User, townId: Int, specialization: Specialization) {
        checkForbiddenAction(user)
        val town = province.getTownById(townId).getOrThrow()
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
}