package com.grupox.wololo.model

import java.util.*

object RepoGames {

    private val gamesInDB: ArrayList<Game> = arrayListOf(
            Game (
                province = Province(
                    name = "Santiago del Estero",
                    towns = arrayListOf(Town("Termas de Río Hondo", Coordinates(0f,0f), elevation = 3f), Town("La Banda",Coordinates(0f,0f), elevation = 3f))
                ),
                status=Status.NEW
            ),
            Game (
                province = Province(
                    name = "Córdoba",
                    towns = arrayListOf(Town("Cipolletti", Coordinates(0f,0f), elevation = 3f))
                ),
                status = Status.FINISHED
            )
    )

    fun getGames(): List<Game> = gamesInDB

    fun getGameById(id: Int): Game? = gamesInDB.find { it.id == id }

    fun changeGameStatus(id: Int, status: String){
      gamesInDB.find { it.id == id }?.status = Status.valueOf(status)
    }

    fun insertGame(game: Game) {
        gamesInDB.add(game)
    }
}
