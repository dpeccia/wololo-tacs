package com.grupox.wololo.services

import arrow.core.extensions.option.foldable.firstOrNone
import arrow.core.extensions.option.foldable.get
import arrow.core.getOrElse
import arrow.core.toOption
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.AttackForm
import com.grupox.wololo.model.helpers.JwtSigner
import com.grupox.wololo.model.helpers.MovementForm
import com.grupox.wololo.model.helpers.UserWithoutStats
import org.springframework.stereotype.Service
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers
import com.grupox.wololo.model.repos.RepoUsers.getNormalUsers

@Service
class GamesService {

    fun surrender(gameId: Int, participantsIds: List<Int> , userMail : String) : Int? {
        val userID : Int = RepoUsers.getUserByName(userMail).getOrElse {  throw CustomException.NotFoundException("User was not found")  }.id
        val game: Game = RepoGames.getById(gameId).getOrElse { throw CustomException.NotFoundException("Game was not found") }

        RepoUsers.getById(userID).getOrElse {  throw CustomException.NotFoundException("User was not found")}.updateGamesLostStats()

        if ((participantsIds.size) <= 2) {
            val winnerUserID : Int = participantsIds.find { it != userID }.toOption().getOrElse {throw CustomException.NotFoundException("Not enough participants from game")}
            game.status = Status.CANCELED
            RepoUsers.getById(winnerUserID).getOrElse {  throw CustomException.NotFoundException("User was not found")}.updateGamesWonStats()
        }
//lo cambio por option
        return getNormalUsers().find { it.mail == userMail }?.stats?.gamesLost
    }

    fun changeSpecialization(specialization: String, gameId: Int, townId: Int) {

        if (specialization == "PRODUCTION"){

            RepoGames.getById(gameId).getOrElse {throw CustomException.NotFoundException("Game was not found")}.changeTownSpecialization(townId, Production())

        } else if (specialization == "DEFENSE"){
            RepoGames.getById(gameId).getOrElse {throw CustomException.NotFoundException("Game was not found")}.changeTownSpecialization(townId, Defense())
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