package com.grupox.wololo.services

import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.extensions.list.functorFilter.filter
import arrow.optics.extensions.list.cons.cons
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.externalservices.*
import com.grupox.wololo.model.helpers.*
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class GamesControllerService(@Autowired val repoUsers: RepoUsers, @Autowired val repoGames: RepoGames) {
    @Autowired
    lateinit var geoRef: GeoRef
    @Autowired
    lateinit var topoData: TopoData
    @Autowired
    lateinit var provincesService: ProvincesService
    @Autowired
    lateinit var pixabay: Pixabay
    
    fun surrender(gameId: ObjectId, userId: ObjectId): Change.GameChange =
            this.play(gameId, userId) { game, user -> game.surrender(user) }

    fun finishTurn(userId: ObjectId, gameId: ObjectId): Change.GameChange =
            this.play(gameId, userId) { game, user -> game.finishTurn(user) }

    fun moveGauchosBetweenTowns(userId: ObjectId, gameId: ObjectId, movementData: MovementForm): Change.GameChange =
            this.play(gameId, userId) { game, user -> game.moveGauchosBetweenTowns(user, movementData) }

    fun attackTown(userId: ObjectId, gameId: ObjectId, attackData: AttackForm): Change.GameChange =
            this.play(gameId, userId) { game, user -> game.attackTown(user, attackData) }

    fun getProvinces(): List<String> = provincesService.availableProvinces().getOrThrow()

    fun getTownsGeoJSONs(province: String, towns: String): List<TownGeoJSON> {
        val townNames: List<String> = towns.split('|').map { it.trim() }
        return provincesService.townsGeoJSONs(province, townNames)
    }
    
    fun getTownStats(gameId: ObjectId, townId: Int): DTO.TownDTO {
        val game = getGame(gameId)
        val town = game.province.getTownById(townId).getOrThrow()
        return town.dto()
    }

    fun getGame(userId: ObjectId, gameId: ObjectId): DTO.GameDTO {
        val user = getUser(userId)
        val game = getGame(gameId)
        if(!game.isParticipating(user)) throw CustomException.Unauthorized.TokenException("User not participating in this game")
        return game.dto()
    }

    fun getGames(userId: ObjectId, sort: String?, status: Status?, date: Date?): List<DTO.GameDTO> {
        val user = getUser(userId)
        var games: List<Game> = repoGames.findAllByPlayersContains(user)

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

        return games.map { it.dto() }
    }

    fun getGamesInADateRange(from: Date, to: Date): List<DTO.GameDTO> {
        return repoGames.findAll().filter { it.date in from..to }.map { it.dto()}
    }

    fun getGamesStats(from: Date, to: Date): GamePublicInfo {

        fun numberOfGames(status : String) : Int {
            return getGamesInADateRange(from, to).map { it.status }.filter { it.toString() == status }.count()
        }

        return GamePublicInfo(numberOfGames("NEW"), numberOfGames("ONGOING"), numberOfGames("FINISHED"), numberOfGames("CANCELED"))
    }

    fun createGame(userId: ObjectId, form: GameForm): DTO.GameDTO {
        val game: Game = Either.fx<CustomException, Game> {
            val users = userId.toString().cons(form.participantsIds).distinct()
                    .map { repoUsers.findByIsAdminFalseAndId(ObjectId(it)).orElseThrow { CustomException.NotFound.UserNotFoundException() } }

            val townsData: List<TownGeoRef> = !geoRef.requestTownsData(form.provinceName, form.townAmount)

            val towns = townsData.map { data ->
                Town.new(data.name, !topoData.requestElevation(data.coordinates), data.coordinates, !pixabay.requestTownImage(data.name))
            }
            val province = Province(form.provinceName, ArrayList(towns), provincesService.getUrl(form.provinceName))
            Game.new(users, province)
        }.getOrThrow()

        val savedGame = repoGames.save(game)
        return savedGame.dto()
    }

    fun updateTownSpecialization(userId: ObjectId, gameId: ObjectId, townId: Int, newSpecialization: String): Change.GameChange =
            this.play(gameId, userId) { game, user ->
                when (newSpecialization.toUpperCase()) {
                    "PRODUCTION" -> game.changeTownSpecialization(user, townId, Production())
                    "DEFENSE"    -> game.changeTownSpecialization(user, townId, Defense())
                }
            }

    private fun getGame(id: ObjectId): Game =
            repoGames.findById(id).orElseThrow { CustomException.NotFound.GameNotFoundException() }

    private fun getUser(id: ObjectId): User =
            repoUsers.findByIsAdminFalseAndId(id).orElseThrow { CustomException.NotFound.UserNotFoundException() }

    private fun play(gameId: ObjectId, userId: ObjectId, action: (Game, User) -> Unit): Change.GameChange {
        val game = getGame(gameId)
        val user = getUser(userId)
        val initialState = game.state()
        action(game, user)
        val updatedGame = repoGames.save(game)
        val finalState = updatedGame.state()
        return finalState.diff(initialState)
    }
}