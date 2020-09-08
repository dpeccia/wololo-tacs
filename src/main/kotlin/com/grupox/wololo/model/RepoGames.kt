package com.grupox.wololo.model

import java.util.*

object RepoGames {

    private val gamesInDB: ArrayList<Game> = arrayListOf(
            Game(
                id = 1,
                province = Province(
                    id = 1,
                    name = "Santiago del Estero",
                    towns = arrayListOf(Town(1, "Termas de Río Hondo", Coordinates(0f,0f), Production(), TownStats()), Town(2, "La Banda",Coordinates(0f,0f), Defense(), TownStats()))
                ),
                status=Status.NEW
            ),
            Game(
                id= 2,
                province = Province(
                    id = 3,
                    name = "Córdoba",
                    towns = arrayListOf(Town(3, "Cipolletti", Coordinates(0f,0f), Production(), TownStats()))
                ),
                status = Status.FINISHED
            )
    )

    fun getGames(): List<Game> = gamesInDB

    fun getGameById(id: Int): Game? = gamesInDB.find { it.id == id }

    fun insertGame(game: Game) {
        gamesInDB.add(game)
    }
}
