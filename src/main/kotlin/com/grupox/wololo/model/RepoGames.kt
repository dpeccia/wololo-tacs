package com.grupox.wololo.model

import java.util.*

object RepoGames {

    private val gamesInDB: ArrayList<Game> = arrayListOf(
            Game(1, Status.NEW, province = Province(1, "Santiago del Estero",arrayListOf(Town(1, "Termas de Río Hondo"), Town(2, "La Banda")))),
            Game(2, Status.FINISHED, province = Province(3, "Córdoba",arrayListOf(Town(3, "Cipolletti"))))
    )


    fun getGames(): List<Game> = gamesInDB

    fun getGameById(id: Int): Game? = gamesInDB.find { it.id == id }

    fun insertGame(game: Game) {
        gamesInDB.add(game)
    }
}
