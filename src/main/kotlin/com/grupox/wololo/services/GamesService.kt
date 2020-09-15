package com.grupox.wololo.services

import arrow.core.getOrElse
import arrow.core.getOrHandle
import arrow.core.toOption
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.AttackForm
import com.grupox.wololo.model.helpers.MovementForm
import org.springframework.stereotype.Service
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers

@Service
class GamesService {

    fun surrender(gameId: Int, userId : String) : Int? {

        val game: Game = RepoGames.getById(gameId).getOrHandle { throw it }
        val user: User = game.getMember(userId.toInt()).getOrHandle { throw it }
        val loserUserId: Int = user.id

        val participantsIds: List<Int> = game.players.map{it.id}

        val loserUser: User = RepoUsers.getById(loserUserId).getOrHandle {throw it }

        loserUser.updateGamesLostStats()

        if (participantsIds.size <= 2) {
        val winnerUserID : Int = participantsIds.find { it != userId.toInt() }.toOption().getOrElse { throw CustomException.BadRequest.IllegalGameException("Not enough participants from game") }
        game.status = Status.CANCELED
        RepoUsers.getById(winnerUserID).getOrHandle { throw it }.updateGamesWonStats()
    }
        return loserUser.stats.gamesLost
    }

    fun changeSpecialization(specialization: String, gameId: Int, townId: Int) {

        if (specialization == "PRODUCTION"){
            RepoGames.getById(gameId).getOrHandle {throw it }.changeTownSpecialization(townId, Production())
        } else if (specialization == "DEFENSE"){
            RepoGames.getById(gameId).getOrHandle {throw it }.changeTownSpecialization(townId, Defense())
        }
    }

    fun moveGauchosBetweenTowns(userId: Int, gameId: Int, movementData: MovementForm) {
        val game = RepoGames.getById(gameId).getOrHandle { throw it }
        game.moveGauchosBetweenTowns(userId, movementData)
    }

    fun attackTown(userId: Int, gameId: Int, attackData: AttackForm) {
        val game = RepoGames.getById(gameId).getOrHandle { throw it }
        game.attackTown(userId, attackData)
    }
}