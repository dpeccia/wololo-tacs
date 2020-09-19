package com.grupox.wololo.services

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.fx
import arrow.core.extensions.list.traverse.sequence
import arrow.core.fix
import arrow.optics.extensions.list.cons.cons
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.externalservices.GeoRef
import com.grupox.wololo.model.externalservices.ProvincesImages
import com.grupox.wololo.model.externalservices.TopoData
import com.grupox.wololo.model.helpers.*
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class GamesControllerService {
    @Autowired
    lateinit var geoRef: GeoRef
    @Autowired
    lateinit var topoData: TopoData
    @Autowired
    lateinit var provinceImages: ProvincesImages

    fun surrender(gameId: Int, userId: Int) {
        val game: Game = RepoGames.getById(gameId).getOrThrow()
        val user: User = game.getMember(userId).getOrThrow()

        user.updateGamesLostStats()

        if (game.playerAmount <= 2) {
            game.players.filter { it.id != user.id }.map { it.updateGamesWonStats() }  // Update stats ganadores
            game.status = Status.CANCELED
        }
    }

    fun finishTurn(userId: Int, gameId: Int) {
        val game = RepoGames.getById(gameId).getOrThrow()
        val user = RepoUsers.getById(userId).getOrThrow()
        game.finishTurn(user)
    }

    fun moveGauchosBetweenTowns(userId: Int, gameId: Int, movementData: MovementForm) {
        val game = RepoGames.getById(gameId).getOrThrow()
        val user = RepoUsers.getById(userId).getOrThrow()
        game.moveGauchosBetweenTowns(user, movementData)
    }

    fun attackTown(userId: Int, gameId: Int, attackData: AttackForm) {
        val game = RepoGames.getById(gameId).getOrThrow()
        val user = RepoUsers.getById(userId).getOrThrow()
        game.attackTown(user, attackData)
    }

    fun getTownStats(gameId: Int, townId: Int): TownInfo {

        val game = RepoGames.getById(gameId).getOrThrow()
        val town = game.province.getTownById(townId).getOrThrow()

        return TownInfo(town.gauchosGeneratedByDefense, town.gauchosGeneratedByProduction)
    }

    fun getProvinces() = geoRef.requestAvailableProvinces().getOrThrow()

    fun getGame(userId: Int, gameId: Int): Game {
        val user = RepoUsers.getById(userId).getOrThrow()
        val game = RepoGames.getById(gameId).getOrThrow()
        if(!game.isParticipating(user)) throw CustomException.Unauthorized.TokenException("User not participating in this game") // Por ahi no corresponde esta excepcion

        return game
    }

    fun getGames(userId: Int, sort: String?, status: Status?, date: Date?): List<Game> {
        val user = RepoUsers.getById(userId).getOrThrow()
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

    fun createGame(userId: Int, form: GameForm) {
        val game: Game = Either.fx<CustomException, Game> {
            val users = !userId.cons(form.participantsIds).distinct().map { RepoUsers.getById(it) }
                    .sequence(Either.applicative()).fix().map{ it.fix() }

            val townsData: List<TownGeoRef> = !geoRef.requestTownsData(form.provinceName, form.townAmount)
            val towns = !townsData.map { data ->
                topoData.requestElevation(data.coordinates).map { elevation ->
                    Town(data.id, data.name, data.coordinates, elevation.toDouble())
                }
            }.sequence(Either.applicative()).fix().map { it.fix() }

            val provinceImage: String = !provinceImages.getUrl(form.provinceName)
            Game(0,users,  Province(0, form.provinceName, ArrayList(towns), provinceImage))
        }.getOrThrow()

        RepoGames.insert(game)
    }

    fun updateTownSpecialization(userId: Int, gameId: Int, townId: Int, newSpecialization: String){
        val game = RepoGames.getById(gameId).getOrThrow()
        val user = RepoUsers.getById(userId).getOrThrow()
        when (newSpecialization.toUpperCase()) {
            "PRODUCTION" -> game.changeTownSpecialization(user, townId, Production())
            "DEFENSE"    -> game.changeTownSpecialization(user, townId, Defense())
        }
    }
}