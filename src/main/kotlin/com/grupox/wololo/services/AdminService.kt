package com.grupox.wololo.services

import arrow.core.extensions.list.functorFilter.filter
import arrow.core.getOrElse
import arrow.core.toOption
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.repos.RepoUsers.getNormalUsers
import com.grupox.wololo.model.helpers.GameStats
import com.grupox.wololo.model.helpers.UserStats
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers
import java.util.*

class AdminService {

    fun getScoreBoard(): List<UserStats> {
        return RepoUsers.getUsersStats()
    }

    fun getGamesStats(from: Date, to: Date): GameStats {
        val games: List<Game> = RepoGames.getAll().filter { it.date > from && it.date <to }

        fun numberOfGames(status : String) : Int {
            return games.map { it.status }.filter { it.toString() == status }.count()
        }

        return GameStats(numberOfGames("NEW"), numberOfGames("ONGOING"), numberOfGames("FINISHED"), numberOfGames("CANCELED"), games)

    }



}