package com.grupox.wololo.model.repos

import arrow.core.Either
import arrow.core.rightIfNotNull
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import java.util.*

object RepoGames : Repository<Game> {

    private val gamesInDB: ArrayList<Game> = arrayListOf(
            Game(
                    players = listOf(
                            User("user","mail", "password", stats = Stats(1,1)),
                            User("user2","mail2", "password2")
                    ),
                    province = Province(
                            name = "Santiago del Estero",
                            towns = arrayListOf(
                                Town("Termas de Río Hondo", Coordinates(2.2.toFloat(),3.3.toFloat()), 3.2),
                                Town("La Banda", Coordinates(1.1.toFloat(),4.4.toFloat()), 5.4)
                            )
                    ),
                    status= Status.NEW
            ),
            Game(
                    players = listOf(User("user", "mail", "password"), User("user2","mail2", "password2")),
                    province = Province(
                            name = "Córdoba",

                            towns = arrayListOf(
                                Town("Cipolletti", Coordinates(7.42.toFloat(),6.4.toFloat()), 7.8),
                                Town("Termas de Río Hondo", Coordinates(2.2.toFloat(),3.3.toFloat()), 3.2),
                                Town("La Banda", Coordinates(1.1.toFloat(),4.4.toFloat()), 5.4)
                            )
                    ),
                    status = Status.FINISHED
            )
    )

    override fun getAll(): List<Game> = gamesInDB

    override fun getById(id: Int): Either<CustomException.NotFound, Game> = getAll().find { it.id == id }.rightIfNotNull { CustomException.NotFound.GameNotFoundException() }

    override fun filter(predicate: (game: Game) -> Boolean) = getAll().filter { predicate(it) }

    override fun insert(obj: Game) {
        gamesInDB.add(obj)
    }
}
