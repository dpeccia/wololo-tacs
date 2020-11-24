package com.grupox.wololo.model

import arrow.core.Either
import arrow.core.extensions.list.functorFilter.filter
import arrow.core.rightIfNotNull
import com.grupox.wololo.WololoApplication
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.helpers.*
import org.bson.types.ObjectId
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.mail.MailSender
import java.time.Instant.now
import java.util.*
import kotlin.concurrent.schedule

@Document(collection = "Games")
class Game(@DBRef var players: List<User>, val province: Province, val gameMode: GameMode, @Indexed var status: Status) : Requestable {
    @Id
    var id: ObjectId = ObjectId.get()

    @Transient
    lateinit var mailTask: TimerTask

    val townsAmount: Int get() = province.towns.size

    val playerAmount: Int get() = players.size

    private var turnManager: TurnManager<ObjectId> = TurnManager(this.players.map { it.id }.shuffled())

    var turn: User
        get() = this.players.find { it.id == turnManager.current }!!
        set(value) { this.turnManager.current = value.id }

    @Indexed
    var date: Date = Date.from(now())

    companion object {
        fun new(_players: List<User>, _province: Province, _mode: GameMode, _sender: MailService, _status: Status = Status.NEW): Game {
            val newGame = Game(_players, _province, _mode, _status)
            newGame.checkIfIllegal()
            newGame.assignTowns()
            newGame.startGame(_sender)
            return newGame
        }
    }

    private fun checkIfIllegal(){
        if (playerAmount < 2 || playerAmount > 4) throw CustomException.BadRequest.IllegalGameException("There is not enough players. Actual: $playerAmount, but expected an amount between 2 (inclusive) and 4 (inclusive)")
        if (townsAmount < playerAmount) throw CustomException.BadRequest.IllegalGameException("There is not enough towns for the given players")
    }
    
    private fun assignTowns() {  // Este metodo puede modificarse para hacer algun algoritmo mas copado.
        val townGroups = province.towns.shuffled().chunked(townsAmount / playerAmount)
        townGroups.zip(players).forEach { (townGroup, player) -> townGroup.forEach { it.owner = player } }
    }

    private fun startGame(mailSender: MailService) {
        mailTask = MailManager(mailSender).setTimerForUser(this.turn.mail)
        province.addGauchosToAllTowns(gameMode)
        status = Status.ONGOING
    }

    private fun changeTurn(mailSender: MailService) {
        val mailManager = MailManager(mailSender)
        //mailManager.cancel(mailTask)
        this.turnManager.changeTurn()
        province.addGauchosToAllTownsFrom(this.turn, gameMode)
        mailTask = mailManager.setTimerForUser(this.turn.mail)
    }

    private fun checkForbiddenAction(user: User) {
        if (status == Status.FINISHED || status == Status.CANCELED) throw CustomException.Forbidden.FinishedGameException()
        if (!isParticipating(user)) throw CustomException.Forbidden.NotAMemberException()
        if (!isTurnOf(user)) throw CustomException.Forbidden.NotYourTurnException()
    }

    private fun userWon(user: User): Boolean = province.allOccupiedTownsAreFrom(user)

    private fun updateStats(winner: User) {
        status = Status.FINISHED
        winner.updateGamesWonStats()
        players.filter { it.id != winner.id }.forEach { it.updateGamesLostStats() }
    }

    fun isParticipating(user: User): Boolean = players.map { it.id.toString() }.contains(user.id.toString())

    private fun isTurnOf(user: User): Boolean = turn.id.toString() == user.id.toString()

    fun finishTurn(user: User, mailSender: MailService) {
        checkForbiddenAction(user)
        province.unlockAllTownsFrom(user)
        if(userWon(user)) {
            updateStats(user)
            MailManager(mailSender).cancel(mailTask) }
        else{
            changeTurn(mailSender)}
    }

    fun changeTownSpecialization(user: User, townId: Int, specialization: Specialization) {
        checkForbiddenAction(user)
        val town = province.getTownById(townId).getOrThrow()
        if(!town.isFrom(user)) throw CustomException.Forbidden.NotYourTownException()
        town.specialization = specialization
    }

    fun moveGauchosBetweenTowns(user: User, movementForm: MovementForm) {
        checkForbiddenAction(user)
        province.moveGauchosBetweenTowns(user, movementForm)
    }

    fun attackTown(user: User, attackForm: AttackForm) {
        checkForbiddenAction(user)
        province.attackTown(user, attackForm, gameMode)
    }

    fun surrender(user: User) {
        if (status == Status.FINISHED || status == Status.CANCELED) throw CustomException.Forbidden.FinishedGameException()
        if (!isParticipating(user)) throw CustomException.Forbidden.NotAMemberException()

        user.updateGamesLostStats()
        if (this.playerAmount == 2) {
            this.players.filter { it.id != user.id }.map { it.updateGamesWonStats() }
            this.status = Status.CANCELED
        } else {
            province.townsFrom(user).forEach { it.neutralize() }
            this.players = this.players.filter { it.id != user.id }
            this.turnManager.removeParticipant(user.id)
        }
    }

    override fun dto(): DTO.GameDTO =
        DTO.GameDTO(
            id = id.toString(),
            status = status,
            date = date,
            turnId = turn.id.toString(),
            playerIds = players.map { it.dto() },
            province = province.dto()
        )

    fun state(): State.GameState =
        State.GameState(
                id = this.id,
                status = this.status,
                turnUsername = this.turn.username,
                towns = this.province.towns.map { it.state() }
        )

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Game) {
            return false
        }
        return id == other.id
    }
}