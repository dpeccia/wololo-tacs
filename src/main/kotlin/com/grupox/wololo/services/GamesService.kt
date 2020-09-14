package com.grupox.wololo.services

import arrow.core.getOrElse
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

        val game: Game = RepoGames.getById(gameId).getOrElse { throw CustomException.NotFound.GameNotFoundException() }
        val user: User = game.getMember(userId.toInt()).getOrElse { throw CustomException.NotFound.UserNotFoundException() }
        val loserUserId: Int = user.id

        val participantsIds: List<Int> = game.players.map{it.id}

        val loserUser: User = RepoUsers.getById(loserUserId).getOrElse {  throw CustomException.NotFound.UserNotFoundException() }

        loserUser.updateGamesLostStats()

        if ((participantsIds.size) <= 2) {
        val winnerUserID : Int = participantsIds.find { it != userId.toInt() }.toOption().getOrElse { throw CustomException.BadRequest.IllegalGameException("Not enough participants from game") }
        game.status = Status.CANCELED
        RepoUsers.getById(winnerUserID).getOrElse {  throw CustomException.NotFound.UserNotFoundException() }.updateGamesWonStats()
    }
        return loserUser.stats.gamesLost
    }

    fun changeSpecialization(specialization: String, gameId: Int, townId: Int) {

        if (specialization == "PRODUCTION"){
            RepoGames.getById(gameId).getOrElse {throw CustomException.NotFound.GameNotFoundException() }.changeTownSpecialization(townId, Production())
        } else if (specialization == "DEFENSE"){
            RepoGames.getById(gameId).getOrElse {throw CustomException.NotFound.GameNotFoundException() }.changeTownSpecialization(townId, Defense())
        }
    }

    fun moveGauchosBetweenTowns(userId: Int, gameId: Int, movementData: MovementForm) {
        val game = RepoGames.getGameByIdAndUser(gameId, userId)
        game.moveGauchosBetweenTowns(userId, movementData)
    }

    fun attackTown(userId: Int, gameId: Int, attackData: AttackForm) {
        val game = RepoGames.getGameByIdAndUser(gameId, userId)
        game.attackTown(userId, attackData)
    }
}