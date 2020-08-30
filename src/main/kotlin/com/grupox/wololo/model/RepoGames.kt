package com.grupox.wololo.model

import java.util.*

object RepoGames {
    private val gamesInDB: ArrayList<Game> = arrayListOf(
        Game(1, Status.NEW),
        Game(2, Status.FINISHED)
    )

    fun getGames(): List<Game> = gamesInDB

    fun getGameById(id: Int): Game? = gamesInDB.find { it.getId() == id }

    fun insertGame(game: Game) {
        gamesInDB.add(game)
    }
}
