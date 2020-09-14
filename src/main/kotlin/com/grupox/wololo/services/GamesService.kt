package com.grupox.wololo.services

import arrow.core.getOrElse
import arrow.core.getOrHandle
import arrow.core.toOption
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.RepoUsers.getNormalUsers
import com.grupox.wololo.model.helpers.JwtSigner
import com.grupox.wololo.model.helpers.UserWithoutStats
import org.springframework.stereotype.Service

class GamesService {

    fun surrender(gameId: Int, participantsIds: List<Int> , userMail : String) : Int? {

        val userID : Int = RepoUsers.getUserByName(userMail).getOrElse {  throw CustomException.NotFoundException("User was not found")  }.id
        val game: Game = RepoGames.getGameById(gameId).getOrElse { throw CustomException.NotFoundException("Game was not found") }

        RepoUsers.getUserById(userID).getOrElse {  throw CustomException.NotFoundException("User was not found")}.updateGamesLostStats()

        if ((participantsIds.size) <= 2) {
            val winnerUserID : Int = participantsIds.find { it != userID }.toOption().getOrElse {throw CustomException.NotFoundException("Not enough participants from game")}
            game.status = Status.CANCELED
            RepoUsers.getUserById(winnerUserID).getOrElse {  throw CustomException.NotFoundException("User was not found")}.updateGamesWonStats()
        }
//lo cambio por option
        return getNormalUsers().find { it.mail == userMail }?.stats?.gamesLost
    }

    fun changeSpecialization(specialization: String, gameId: Int, townId: Int) {

        if (specialization == "PRODUCTION"){

            RepoGames.getGameById(gameId).getOrElse {throw CustomException.NotFoundException("Game was not found")}.changeTownSpecialization(townId, Production())

        } else if (specialization == "DEFENSE"){
            RepoGames.getGameById(gameId).getOrElse {throw CustomException.NotFoundException("Game was not found")}.changeTownSpecialization(townId, Defense())
        }
    }

}