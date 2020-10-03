package com.grupox.wololo.services

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.fx
import arrow.core.extensions.list.functorFilter.filter
import arrow.core.extensions.list.traverse.sequence
import arrow.core.fix
import arrow.optics.extensions.list.cons.cons
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.externalservices.*
import com.grupox.wololo.model.helpers.*
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers
import mu.KotlinLogging
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class GamesControllerService(@Autowired val repoUsers: RepoUsers) {
    @Autowired
    lateinit var geoRef: GeoRef
    @Autowired
    lateinit var topoData: TopoData
    @Autowired
    lateinit var provinceImages: ProvincesService
    @Autowired
    lateinit var pixabay: Pixabay

    private val logger = KotlinLogging.logger {}
    
    fun surrender(gameId: Int, userId: ObjectId): DTO.GameDTO {
        val game: Game = RepoGames.getById(gameId).getOrThrow()
        val user: User = game.getMember(userId).getOrThrow()

        user.updateGamesLostStats()

        if (game.playerAmount <= 2) {
            game.players.filter { it.id != user.id }.map { it.updateGamesWonStats() }  // Update stats ganadores
            game.status = Status.CANCELED
        }

        return game.dto()
    }

    fun finishTurn(userId: ObjectId, gameId: Int): DTO.GameDTO {
        val game = RepoGames.getById(gameId).getOrThrow()
        val user = getUser(userId)
        game.finishTurn(user)
        return game.dto()
    }

    fun moveGauchosBetweenTowns(userId: ObjectId, gameId: Int, movementData: MovementForm): DTO.GameDTO {
        val game = RepoGames.getById(gameId).getOrThrow()
        val user = getUser(userId)
        game.moveGauchosBetweenTowns(user, movementData)
        return game.dto()
    }

    fun attackTown(userId: ObjectId, gameId: Int, attackData: AttackForm): DTO.GameDTO {
        val game = RepoGames.getById(gameId).getOrThrow()
        val user = getUser(userId)
        game.attackTown(user, attackData)
        return game.dto()
    }

    fun getProvinces(): List<String> = provinceImages.availableProvinces().getOrThrow()

    fun getTownStats(gameId: Int, townId: Int): DTO.TownDTO {
        val game = RepoGames.getById(gameId).getOrThrow()
        val town = game.province.getTownById(townId).getOrThrow()

        return town.dto()
    }

    fun getGame(userId: ObjectId, gameId: Int): DTO.GameDTO {
        val user = getUser(userId)
        val game = RepoGames.getById(gameId).getOrThrow()
        if(!game.isParticipating(user)) throw CustomException.Unauthorized.TokenException("User not participating in this game") // Por ahi no corresponde esta excepcion

        return game.dto()
    }

    fun getGames(userId: ObjectId, sort: String?, status: Status?, date: Date?): List<DTO.GameDTO> {
        val user = getUser(userId)
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

        return games.map { it.dto() }
    }

    fun getGamesInADateRange(from: Date, to: Date): List<DTO.GameDTO> {
        return RepoGames.filter { it.date in from..to }.map { it.dto()}
        }

    fun getGamesStats(from: Date, to: Date): GamePublicInfo {

        fun numberOfGames(status : String) : Int {
            return getGamesInADateRange(from, to).map { it.status }.filter { it.toString() == status }.count()
        }

        return GamePublicInfo(numberOfGames("NEW"), numberOfGames("ONGOING"), numberOfGames("FINISHED"), numberOfGames("CANCELED"))
    }

    fun createGame(userId: ObjectId, form: GameForm): DTO.GameDTO {
        val game: Game = Either.fx<CustomException, Game> {
            // no se si rompi esto
            val users = userId.toString().cons(form.participantsIds).distinct()
                    .map { repoUsers.findByIsAdminFalseAndId(ObjectId(it)).orElseThrow { CustomException.NotFound.UserNotFoundException() } }

            val townsData: List<TownGeoRef> = !geoRef.requestTownsData(form.provinceName, form.townAmount)
            val towns = townsData.map { data ->
                val elevation = !topoData.requestElevation(data.coordinates)
                val imageUrl = !pixabay.requestTownImage(data.name)
                Town(data.name, data.coordinates, elevation, imageUrl)
            }

            val provinceImage: String = provinceImages.getUrl(form.provinceName)
            Game(users,  Province(form.provinceName, ArrayList(towns), provinceImage))
        }.getOrThrow()

        RepoGames.insert(game)
        return game.dto()
    }

    fun updateTownSpecialization(userId: ObjectId, gameId: Int, townId: Int, newSpecialization: String): DTO.GameDTO {
        val game = RepoGames.getById(gameId).getOrThrow()
        val user = getUser(userId)
        when (newSpecialization.toUpperCase()) {
            "PRODUCTION" -> game.changeTownSpecialization(user, townId, Production())
            "DEFENSE"    -> game.changeTownSpecialization(user, townId, Defense())
        }

        return game.dto()
    }

    private fun getUser(id: ObjectId): User =
        repoUsers.findByIsAdminFalseAndId(id).orElseThrow { CustomException.NotFound.UserNotFoundException() }
}