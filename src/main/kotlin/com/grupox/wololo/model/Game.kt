package com.grupox.wololo.model

import arrow.core.Either
import arrow.core.rightIfNotNull
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.helpers.*
import java.time.Instant
import java.util.*


class Game(val players: List<User>, val province: Province, var status: Status = Status.NEW) : Requestable {
    val id: UUID = UUID.randomUUID()

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

    // TODO falta chequear que nunca se cree un juego con un (MaxAltitude - MinAltitude) = 0 || (MaxDist - MinDist) = 0
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

    private fun updateStats(winner: User) {
        status = Status.FINISHED
        winner.updateGamesWonStats()
        players.filter { it != winner }.forEach { it.updateGamesLostStats() }
    }

    fun getMember(userId: UUID): Either<CustomException.NotFound, User> = players.find { it.id == userId }.rightIfNotNull { CustomException.NotFound.MemberNotFoundException() }

    fun isParticipating(user: User): Boolean = players.contains(user)

    fun finishTurn(user: User) {
        checkForbiddenAction(user)
        province.unlockAllTownsFrom(user)
        if(userWon(user)) updateStats(user) else changeTurn()
    }

    fun changeTownSpecialization(user: User, townId: UUID, specialization: Specialization) {
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

    override fun dto(): DTO.GameDTO =
        DTO.GameDTO(
            id = id,
            status = status,
            date = date,
            turnId = turn.id,
            playerIds = players.map { it.dto() },
            province = province.dto()
        )
}