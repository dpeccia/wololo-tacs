package com.grupox.wololo.services

import arrow.core.extensions.list.functorFilter.filter
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.GamePublicInfo
import com.grupox.wololo.model.helpers.UserPublicInfo
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers
import java.util.*

class AdminService {

    fun getScoreBoard(): List<UserPublicInfo> {
        return RepoUsers.getUsersStats()
    }

    fun getGamesStats(from: Date, to: Date): GamePublicInfo {
        val games: List<Game> = RepoGames.getAll().filter { it.date > from && it.date <to }

        fun numberOfGames(status : String) : Int {
            return games.map { it.status }.filter { it.toString() == status }.count()
        }

        return GamePublicInfo(numberOfGames("NEW"), numberOfGames("ONGOING"), numberOfGames("FINISHED"), numberOfGames("CANCELED"), games)

    }



}