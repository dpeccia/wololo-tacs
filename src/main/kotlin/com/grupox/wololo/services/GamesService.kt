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

        if ((participantsIds.size) <= 2) {
            RepoGames.changeGameStatus(gameId, Status.CANCELED)
            RepoUsers.updateUserGamesWon(participantsIds.find { it != userID }.toOption().getOrElse {throw CustomException.NotFoundException("Not enough participants from game")})
        }

        RepoUsers.updateUserGamesLost(userID)
//lo cambio por option
        return getNormalUsers().find { it.mail == userMail }?.stats?.gamesLost
    }

    fun changeSpecialization(specialization: String, gameId: Int, townId: Int) {

        if (specialization == "PRODUCTION"){
            RepoGames.changeGameTownSpecialization(gameId,townId, Production())
        } else{
            if (specialization == "DEFENSE"){
                RepoGames.changeGameTownSpecialization(gameId,townId, Defense())
            }
        }
    }

}