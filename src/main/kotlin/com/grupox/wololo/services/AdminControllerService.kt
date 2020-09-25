package com.grupox.wololo.services

import arrow.core.extensions.list.functorFilter.filter
import com.grupox.wololo.model.Game
import com.grupox.wololo.model.helpers.DTO
import com.grupox.wololo.model.helpers.GamePublicInfo
import com.grupox.wololo.model.helpers.getOrThrow
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers
import org.springframework.stereotype.Service
import java.util.*

@Service
class AdminControllerService {

    fun getScoreBoard(): List<DTO.UserDTO> {
        return RepoUsers.getAll().map { it.dto() }
    }

    fun getScoreBoardById(id: Int): DTO.UserDTO {
        return RepoUsers.getById(id).getOrThrow().dto()
    }

    fun getGamesStats(from: Date, to: Date): GamePublicInfo {
        val games: List<Game> = RepoGames.filter { it.date in from..to }

        fun numberOfGames(status : String) : Int {
            return games.map { it.status }.filter { it.toString() == status }.count()
        }

        return GamePublicInfo(numberOfGames("NEW"), numberOfGames("ONGOING"), numberOfGames("FINISHED"), numberOfGames("CANCELED"), games)
    }
}