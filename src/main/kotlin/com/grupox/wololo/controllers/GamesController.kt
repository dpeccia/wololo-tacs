package com.grupox.wololo.controllers

import arrow.core.*
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.fx
import arrow.core.extensions.list.traverse.sequence

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.GameForm
import com.grupox.wololo.model.helpers.JwtSigner
import com.grupox.wololo.model.helpers.ProvinceGeoRef
import com.grupox.wololo.model.helpers.TownForm
import com.grupox.wololo.model.helpers.*
import com.grupox.wololo.model.services.GeoRef
import com.grupox.wololo.model.services.TopoData
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

@RequestMapping("/games")
@RestController
class GamesController(@Autowired private val geoRef: GeoRef, @Autowired private val topoData: TopoData) {
    @GetMapping
    @ApiOperation(value = "Gets the games of the current user")
    fun getGames(@RequestParam("sort", required = false) sort: String?,
                 @RequestParam("filter", required = false) filter: String?,
                 @ApiIgnore @CookieValue("X-Auth") authCookie : String?): List<Game> {
        JwtSigner.validateJwt(authCookie.toOption()).getOrHandle { throw it }
        return RepoGames.getGames()
    }
    // TODO obtener mis partidas y filtrar u ordenar por fecha y estado

    @PostMapping
    @ApiOperation(value = "Creates a new game")
    fun createGame(@RequestBody form: GameForm, @ApiIgnore @CookieValue("X-Auth") authCookie : String?) {
        JwtSigner.validateJwt(authCookie.toOption()).getOrHandle { throw it }

        val game: Game = Either.fx<CustomException, Game> {
            val users = !form.participantsIds.map { RepoUsers.getUserById(it).toEither { CustomException.NotFoundException("There is no such user with id=$it") } }
                                            .sequence(Either.applicative()).fix().map{ it.fix() }

            val townsData: List<TownGeoRef> = !geoRef.requestTownsData(form.provinceName, form.townAmount)
            val towns = !townsData.map { data ->
                topoData.requestElevation(data.coordinates).map { elevation ->
                    Town(data.id, data.name, data.coordinates, elevation)
                }
            }.sequence(Either.applicative()).fix().map { it.fix() }
//TODO: id autoincrementada
            Game(0,users,  Province(0,form.provinceName, ArrayList(towns)))

        }.getOrHandle { throw it }

        RepoGames.insertGame(game)
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Gets a game")
    fun getGameById(@PathVariable("id") id: Int,
                    @ApiIgnore @CookieValue("X-Auth") authCookie : String?): Game {
        JwtSigner.validateJwt(authCookie.toOption()).getOrHandle { throw it }
        return RepoGames.getGameById(id).getOrElse { throw CustomException.NotFoundException("Game was not found") }
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Modifies a game status (on going, finished, canceled)")
    fun updateGame(@PathVariable("id") id: Int, @RequestBody gameData: GameForm, @ApiIgnore @CookieValue("X-Auth") authCookie : String?)  {

        val userMail : String = JwtSigner.validateJwt(authCookie.toOption()).getOrHandle { throw it }.body.subject
        val userID : Int = RepoUsers.getUserByName(userMail).getOrElse {  throw CustomException.NotFoundException("User was not found")  }.id
        val game: Game = RepoGames.getGameById(id).getOrElse { throw CustomException.NotFoundException("Game was not found") }
        val participantsIds: List<Int> = gameData.participantsIds

        if ((participantsIds.size) <= 2) {
            RepoGames.changeGameStatus(id, Status.CANCELED)
            RepoUsers.updateUserGamesWon(participantsIds.find { it != userID }.toOption().getOrElse {throw CustomException.NotFoundException("Not enough participants from game")})
        }

        RepoUsers.updateUserGamesLost(userID)



    }

    @PostMapping("/{id}/actions/movement")
    @ApiOperation(value = "Moves the gauchos between towns")
    fun moveGauchosBetweenTowns(
            @PathVariable("id") id: Int,
            @PathVariable("playerId") playerId: Int,
            @RequestParam("from") fromTownId: Int,
            @RequestParam("to") toTownId: Int,
            @RequestParam("quantity") gauchosQuantity: Int,
            @ApiIgnore @CookieValue("X-Auth") authCookie : String?) {
        JwtSigner.validateJwt(authCookie.toOption()).getOrHandle { throw it }
        TODO("mover gauchos de un municipio a otro")
        // TODO("definir Body")
    }

    @PostMapping("/{id}/actions/attack")
    @ApiOperation(value = "Attacks a town")
    fun attackTown(
            @PathVariable("id") id: Int,
            @RequestParam("attacker") attackerId: Int,
            @RequestParam("defender") defenderId: Int,
            @ApiIgnore @CookieValue("X-Auth") authCookie : String?) {
        JwtSigner.validateJwt(authCookie.toOption()).getOrHandle { throw it }
        TODO("logica de ataque")
        // TODO("definir Body")
    }

    @PutMapping("/{id}/towns/{idTown}")
    @ApiOperation(value = "Updates the town specialization")
    fun updateTownSpecialization(
            @PathVariable("id") id: Int,
            @PathVariable("idTown") idTown: Int,
            @RequestBody townData: TownForm,
            @ApiIgnore @CookieValue("X-Auth") authCookie : String?) {
        JwtSigner.validateJwt(authCookie.toOption()).getOrHandle { throw it }

        if (townData.specialization == "PRODUCTION"){
            RepoGames.changeGameTownSpecialization(id,idTown,Production())
        } else if (townData.specialization == "DEFENSE"){
            RepoGames.changeGameTownSpecialization(id,idTown,Defense())
        }

    }

    @GetMapping("/{id}/towns/{idTown}")
    @ApiOperation(value = "Gets the town stats and an image")
    fun getTownData(
            @PathVariable("id") id: Int,
            @PathVariable("idTown") idTown: Int,
            @ApiIgnore @CookieValue("X-Auth") authCookie : String?) {
        JwtSigner.validateJwt(authCookie.toOption()).getOrHandle { throw it }
        TODO("visualizar las estadísticas de producción y defensa de cada municipio, y la imagen")
    }

    @GetMapping("/provinces")
    @ApiOperation(value = "Gets all provinces")
    fun getProvinces(@ApiIgnore @CookieValue("X-Auth") authCookie : String?) : List<ProvinceGeoRef> {
        JwtSigner.validateJwt(authCookie.toOption()).getOrHandle { throw it }
        return geoRef.requestAvailableProvinces().getOrHandle { throw it }
    }

    @ExceptionHandler(CustomException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleDomainException(exception: CustomException) = exception.getJSON()

    @ExceptionHandler(CustomException.TokenException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleExpiredTokenError(exception: CustomException) = exception.getJSON()
}
