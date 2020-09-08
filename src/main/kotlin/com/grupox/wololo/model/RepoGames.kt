package com.grupox.wololo.model

import java.util.*

object RepoGames {

    private val gamesInDB: ArrayList<Game> = arrayListOf(
            Game(1, province = Province(1, "Santiago del Estero",arrayListOf(Town(1, "Termas de Río Hondo", Coordinates(0f,0f),Defense(), TownStats()), Town(2, "La Banda", Coordinates(0f,0f),Defense(), TownStats())))),
            Game(2, province = Province(3, "Córdoba",arrayListOf(Town(3, "Cipolletti", Coordinates(0f,0f), Defense(), TownStats()))), status = Status.FINISHED)
    )

    fun getGames(): List<Game> = gamesInDB

    fun getGameById(id: Int): Game? = gamesInDB.find { it.id == id }

    fun changeGameStatus(id: Int, status: String){
      gamesInDB.find { it.id == id }?.changeStatus(Status.valueOf(status))
    }

    fun insertGame(game: Game) {
        gamesInDB.add(game)
    }
}
