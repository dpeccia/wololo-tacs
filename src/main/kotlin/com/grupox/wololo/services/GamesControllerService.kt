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
import java.time.LocalDate
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
    @Autowired
    lateinit var gameModeService: GameModeService
    @Autowired
    lateinit var mailSender: MailService


    fun surrender(gameId: ObjectId, userId: ObjectId): DTO.GameDTO =
            this.play(gameId, userId) { game, user -> game.surrender(user) }

    fun finishTurn(userId: ObjectId, gameId: ObjectId): DTO.GameDTO =
            this.play(gameId, userId) { game, user -> game.finishTurn(user) }

    fun moveGauchosBetweenTowns(userId: ObjectId, gameId: ObjectId, movementData: MovementForm): DTO.GameDTO =
            this.play(gameId, userId) { game, user -> game.moveGauchosBetweenTowns(user, movementData) }

    fun attackTown(userId: ObjectId, gameId: ObjectId, attackData: AttackForm): DTO.GameDTO =
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

    fun getGamesStats(gamesList: List<DTO.GameDTO>): GamePublicInfo {

        fun numberOfGames(gamesList: List<DTO.GameDTO>, status : String) : Int {
            return gamesList.map { it.status }.filter { it.toString() == status }.count()
        }

        return GamePublicInfo(numberOfGames(gamesList,"NEW"), numberOfGames(gamesList,"ONGOING"), numberOfGames(gamesList,"FINISHED"), numberOfGames(gamesList,"CANCELED"))
    }

    fun getAllGamesDTO(): List<DTO.GameDTO> {
        val games: List<Game> = repoGames.findAll()
        return games.map { it.dto() }
    }

    fun createGame(userId: ObjectId, form: GameForm): DTO.GameDTO {
        if(form.townAmount > 100) throw CustomException.BadRequest.IllegalGameException("Max quantity of towns is 100") // limitante de topodata

        lateinit var gameMode: GameMode
        when (form.difficulty) {
            "EASY" -> gameMode = gameModeService.getDifficultyMultipliers("EASY")
            "NORMAL" -> gameMode = gameModeService.getDifficultyMultipliers("NORMAL")
            "HARD" -> gameMode = gameModeService.getDifficultyMultipliers("HARD")
        }

        val game: Game = Either.fx<CustomException, Game> {
            val users = userId.toString().cons(form.participantsIds).distinct()
                    .map { repoUsers.findByIsAdminFalseAndId(ObjectId(it)).orElseThrow { CustomException.NotFound.UserNotFoundException() } }
            val towns = !getRandomTowns(form)
            val province = Province(formatTownName(form.provinceName), ArrayList(towns), provincesService.getUrl(form.provinceName))
            Game.new(users, province, gameMode, mailSender)
        }.getOrThrow()

        val savedGame = repoGames.save(game)
        return savedGame.dto()
    }

    fun updateTownSpecialization(userId: ObjectId, gameId: ObjectId, townId: Int, newSpecialization: String): DTO.GameDTO =
            this.play(gameId, userId) { game, user ->
                when (newSpecialization.toUpperCase()) {
                    "PRODUCTION" -> game.changeTownSpecialization(user, townId, Production())
                    "DEFENSE"    -> game.changeTownSpecialization(user, townId, Defense())
                }
            }

    private fun getRandomTowns(form: GameForm): Either<CustomException, List<Town>> {
        return Either.fx {
            val townsGeoJson = !provincesService.getRandomBorderingTowns(form.provinceName, form.townAmount)
            val townsWithCoordinates = !geoRef.requestTownsData(form.provinceName, townsGeoJson.map { it.town })
            val townsWithElevation = !topoData.requestElevation(townsWithCoordinates.map { it.coordinates })

            val townsGeoJsonSortedByName = townsGeoJson.sortedBy { it.town }
            townsWithCoordinates.forEach { it.name = formatTownName(it.name) }
            val townsWithCoordinatesSortedByName = townsWithCoordinates.sortedBy { it.name }

            val townsWithBorderingAndCoordinates = townsGeoJsonSortedByName.zip(townsWithCoordinatesSortedByName) {
                geojson, georef -> MergedGeoRefGeoJsonTown(geojson.town, georef.coordinates, geojson.borderingTowns)
            }.sortedWith(compareBy({ it.coordinates.latitude }, { it.coordinates.longitude }))

            val townsWithElevationSortedByCoord = townsWithElevation.sortedWith(compareBy({ it.location.lat }, { it.location.lng }))

            townsWithBorderingAndCoordinates.zip(townsWithElevationSortedByCoord) {
                mergedTown, topoDataTown ->
                    Town.new(mergedTown.name, topoDataTown.elevation, mergedTown.borderingTowns, mergedTown.coordinates, !pixabay.requestTownImage(mergedTown.name))
            }
        }
    }

    private fun getGame(id: ObjectId): Game =
            repoGames.findById(id).orElseThrow { CustomException.NotFound.GameNotFoundException() }

    private fun getUser(id: ObjectId): User =
            repoUsers.findByIsAdminFalseAndId(id).orElseThrow { CustomException.NotFound.UserNotFoundException() }

    private fun play(gameId: ObjectId, userId: ObjectId, action: (Game, User) -> Unit): DTO.GameDTO {
        val game = getGame(gameId)
        val user = getUser(userId)
        action(game, user)
        if(game.status == Status.FINISHED || game.status == Status.CANCELED) game.players.filter { it.id != user.id }.forEach { repoUsers.save(it) }
        repoUsers.save(user)
        val updatedGame = repoGames.save(game)
        return updatedGame.dto()
    }

    private data class MergedGeoRefGeoJsonTown(val name: String, val coordinates: Coordinates, val borderingTowns: List<String>)
}