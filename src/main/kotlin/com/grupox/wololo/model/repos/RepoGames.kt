package com.grupox.wololo.model.repos

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.toOption
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import java.util.*

object RepoGames : Repository<Game> {

    private val gamesInDB: ArrayList<Game> = arrayListOf(
            Game(
                    id = 1,
                    players = listOf(User(5, "mail", "password", false, Stats(1,1)),  User(6, "mail2", "password2", false)),
                    province = Province( id = 1,
                            name = "Santiago del Estero",
                            towns = arrayListOf(Town(1, "Termas de Río Hondo", Coordinates(0f,0f), 0.0, null), Town(2, "La Banda", Coordinates(0f,0f), 0.0, null))
                    ),
                    status= Status.NEW
            ),
            Game(
                    id= 2,
                    players = listOf(User(5, "mail", "password", false)),
                    province = Province( id = 2,
                            name = "Córdoba",
                            towns = arrayListOf(Town(3, "Cipolletti", Coordinates(0f,0f), 0.0, null))
                    ),
                    status = Status.FINISHED
            )
    )

    override fun getAll(): List<Game> = gamesInDB

    override fun getById(id: Int): Option<Game> = gamesInDB.find { it.id == id }.toOption()

    fun getGameByIdAndUser(gameId: Int, userId: Int): Game {
        val game = this.getById(gameId).getOrElse {throw CustomException.NotFoundException("Game was not found")}
        game.getMember(userId).getOrElse { throw CustomException.ForbiddenException("You are not a member of this Game") }
        return game
    }

    override fun filter(predicate: (game: Game) -> Boolean) = gamesInDB.filter { predicate(it) }

    fun changeGameStatus(id: Int, status: Status){
        getById(id).getOrElse {throw CustomException.NotFoundException("Game was not found")}.status = status
    }

    fun changeGameTownSpecialization(gameId: Int, townId: Int, specialization: Specialization){
        getById(gameId).getOrElse {throw CustomException.NotFoundException("Game was not found")}.changeTownSpecialization(townId, specialization)
    }

    override fun insert(obj: Game) {
        gamesInDB.add(obj)
    }
}
