package com.grupox.wololo.model

import arrow.core.*
import java.time.Instant
import java.util.Date
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.helpers.AttackForm
import com.grupox.wololo.model.helpers.MovementForm
import kotlin.random.Random.Default.nextInt


class Game(val id: Int , val players: List<User>, val province: Province, var status: Status = Status.NEW) {
    val townsAmount: Int
        get() = province.towns.size

    val playerAmount: Int
        get() = players.size

    //val id: Int = 0 // TODO: Autogenerada

    lateinit var turn: User // No sacar el "lateinit"
    
    var date: Date = Date.from(Instant.now())

    init {
        assignTowns()
        turn = players[nextInt(from = 0, until = playerAmount)] // IMPORTANTE: va despues de la asignacion. La asignacion chequea que el game sea valido.
    }

    fun getTownById(idTown: Int): Either<CustomException.NotFound, Town> = province.towns.find { it.id == idTown }.rightIfNotNull { CustomException.NotFound.TownNotFoundException() }

    fun getMember(userId: Int): Either<CustomException.NotFound, User> = players.find { it.id == userId }.rightIfNotNull { CustomException.NotFound.MemberNotFoundException() }

    fun isParticipating(user: User): Boolean = players.contains(user)

    fun isParticipating(userId: Int): Boolean = players.any { it.id == userId }

    private fun assignTowns() {  // Este metodo puede modificarse para hacer algun algoritmo mas copado.
        if (townsAmount < playerAmount) throw CustomException.BadRequest.IllegalGameException("There is not enough towns for the given players")
        else if (players.isEmpty()) throw CustomException.BadRequest.IllegalGameException("There is not enough players")

        val townGroups = province.towns.shuffled().chunked(townsAmount / playerAmount)
        townGroups.zip(players).forEach { (townGroup, player) -> townGroup.forEach { it.owner = player } }
    }

    //cuando empieza el turno desbloquear todos mis towns y agregar gauchos a todos mis towns

    /* ACTIONS */
    fun changeTownSpecialization(userId: Int, townId: Int, specialization: Specialization) {
        checkForbiddenAction(userId)
        val town = getTownById(townId).getOrHandle { throw it }

        if(town.owner?.id != userId) throw CustomException.Forbidden.NotYourTownException()

        town.specialization = specialization
    }

    fun moveGauchosBetweenTowns(userId: Int, movementForm: MovementForm) {
        checkForbiddenAction(userId)
        province.moveGauchosBetweenTowns(userId, movementForm)
    }

    fun attackTown(userId: Int, attackForm: AttackForm) {
        checkForbiddenAction(userId)
        province.attackTown(userId, attackForm)
    }
    /* ACTIONS-END */

    private fun checkForbiddenAction(userId: Int){
        if(!isParticipating(userId)) throw CustomException.Forbidden.NotAMemberException()
        if (turn.id != userId) throw CustomException.Forbidden.NotYourTurnException()
    }
}
