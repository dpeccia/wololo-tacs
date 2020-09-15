package com.grupox.wololo.services

import arrow.core.*
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.fx
import arrow.core.extensions.list.traverse.sequence
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.externalservices.GeoRef
import com.grupox.wololo.model.externalservices.TopoData
import com.grupox.wololo.model.helpers.AttackForm
import com.grupox.wololo.model.helpers.GameForm
import com.grupox.wololo.model.helpers.MovementForm
import com.grupox.wololo.model.helpers.TownGeoRef
import org.springframework.stereotype.Service
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

@Service
class GamesControllerService {
    @Autowired
    lateinit var geoRef: GeoRef
    @Autowired
    lateinit var topoData: TopoData

    fun surrender(gameId: Int, userId: Int) {
        val game: Game = RepoGames.getById(gameId).getOrHandle { throw it }
        val user: User = game.getMember(userId).getOrHandle { throw it }

        user.updateGamesLostStats()

        if (game.playerAmount <= 2) {
            game.players.filter { it.id != user.id }.map { it.updateGamesWonStats() }  // Update stats ganadores
            game.status = Status.CANCELED
        }
    }

    fun moveGauchosBetweenTowns(userId: Int, gameId: Int, movementData: MovementForm) {
        val game = RepoGames.getById(gameId).getOrHandle { throw it }

        if (!game.isParticipating(userId)) throw CustomException.Forbidden.NotAMemberException()

        game.moveGauchosBetweenTowns(userId, movementData)
    }

    fun attackTown(userId: Int, gameId: Int, attackData: AttackForm) {
        val game = RepoGames.getById(gameId).getOrHandle { throw it }

        if (!game.isParticipating(userId)) throw CustomException.Forbidden.NotAMemberException()

        game.attackTown(userId, attackData)
    }

    fun getProvinces() = geoRef.requestAvailableProvinces().getOrHandle { throw it }

    fun getGame(gameId: Int, userId: Int): Game {
        val user = RepoUsers.getById(userId).getOrHandle { throw it }
        val game = RepoGames.getById(gameId).getOrHandle { throw it }
        if(!game.isParticipating(user)) throw CustomException.Unauthorized.TokenException("User not participating in this game") // Por ahi no corresponde esta excepcion

        return game
    }

    fun getGames(userId: Int, sort: String?, status: Status?, date: Date?): List<Game> {
        val user = RepoUsers.getById(userId).getOrHandle { throw it }
        var games: List<Game> = RepoGames.filter { it.isParticipating(user) }

        if(status != null)
            games = games.filter { it.status == status }

        if(date != null)
            games = games.filter { it.date == date }

        games = when(sort) {
            "id"              -> games.sortedBy { it.id }
            "date"            -> games.sortedBy { it.date }  // Checkear que Date sea comparable
            "numberOfTowns"   -> games.sortedBy { it.townsAmount }
            "numberOfPlayers" -> games.sortedBy { it.playerAmount }
            else              -> games
        }

        return games
    }

    fun createGame(form: GameForm) {
        val game: Game = Either.fx<CustomException, Game> {
            val users = !form.participantsIds.map { RepoUsers.getById(it) }
                    .sequence(Either.applicative()).fix().map{ it.fix() }

            val townsData: List<TownGeoRef> = !geoRef.requestTownsData(form.provinceName, form.townAmount)

            val towns = !townsData.map { data ->
                topoData.requestElevation(data.coordinates).map { elevation ->
                    Town(data.id, data.name, data.coordinates, elevation.toDouble())
                }
            }.sequence(Either.applicative()).fix().map { it.fix() }
            Game(0,users,  Province(0,form.provinceName, ArrayList(towns)))
        }.getOrHandle { throw it }

        RepoGames.insert(game)
    }

    fun updateTownSpecialization(userId: Int, gameId: Int, townId: Int, newSpecialization: String){
        val game = RepoGames.getById(gameId).getOrHandle { throw it }

        if(!game.isParticipating(userId)) throw CustomException.Forbidden.NotAMemberException()

        when (newSpecialization.toUpperCase()) {
            "PRODUCTION" -> game.changeTownSpecialization(townId, Production())
            "DEFENSE"    -> game.changeTownSpecialization(townId, Defense())
        }
    }
}