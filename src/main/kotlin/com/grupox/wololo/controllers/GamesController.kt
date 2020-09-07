package com.grupox.wololo.controllers

import arrow.core.getOrHandle
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.services.GeoRef
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RequestMapping("/games")
@RestController
class GamesController {
    @GetMapping
    @ApiOperation(value = "Gets the games of the current user")
    fun getGames(@RequestParam("sort", required = false) sort: String?,
                 @RequestParam("filter", required = false) filter: String?,
                 @CookieValue("X-Auth") authCookie : String?): List<Game> {
        JwtSigner.validateJwt(authCookie.toString()).getOrHandle { throw it }
        return RepoGames.getGames()
    }
    // TODO obtener mis partidas y filtrar u ordenar por fecha y estado

    @PostMapping
    @ApiOperation(value = "Creates a new game")
    fun createGame(@RequestBody game: Game, @CookieValue("X-Auth") authCookie : String?) {
        JwtSigner.validateJwt(authCookie.toString()).getOrHandle { throw it }
        RepoGames.insertGame(game)
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Gets a game")
    fun getGameById(@PathVariable("id") id: Int,
                    @CookieValue("X-Auth") authCookie : String?): Game {
        JwtSigner.validateJwt(authCookie.toString()).getOrHandle { throw it }
        return RepoGames.getGameById(id) ?: throw CustomException.NotFoundException("Game was not found")
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Modifies a game status (on going, finished, canceled)")
    fun updateGame(@PathVariable("id") id: Int, @CookieValue("X-Auth") authCookie : String?) {
        JwtSigner.validateJwt(authCookie.toString()).getOrHandle { throw it }
        val game: Game = RepoGames.getGameById(id) ?: throw CustomException.NotFoundException("Game was not found")
        TODO("modificar estado de una partida")
        // TODO("definir Body")
    }

    @PostMapping("/{id}/actions/movement")
    @ApiOperation(value = "Moves the gauchos between towns")
    fun moveGauchosBetweenTowns(
            @PathVariable("id") id: Int,
            @PathVariable("playerId") playerId: Int,
            @RequestParam("from") fromTownId: Int,
            @RequestParam("to") toTownId: Int,
            @RequestParam("quantity") gauchosQuantity: Int,
            @CookieValue("X-Auth") authCookie : String?) {
        JwtSigner.validateJwt(authCookie.toString()).getOrHandle { throw it }
        TODO("mover gauchos de un municipio a otro")
        // TODO("definir Body")
    }

    @PostMapping("/{id}/actions/attack")
    @ApiOperation(value = "Attacks a town")
    fun attackTown(
            @PathVariable("id") id: Int,
            @RequestParam("attacker") attackerId: Int,
            @RequestParam("defender") defenderId: Int,
            @CookieValue("X-Auth") authCookie : String?) {
        JwtSigner.validateJwt(authCookie.toString()).getOrHandle { throw it }
        TODO("logica de ataque")
        // TODO("definir Body")
    }

    @PutMapping("/{id}/towns/{idTown}")
    @ApiOperation(value = "Updates the town specialization")
    fun updateTownSpecialization(
            @PathVariable("id") id: Int,
            @PathVariable("playerId") playerId: Int,
            @PathVariable("specialization") specialization: Int, // TODO change int to specialization type
            @CookieValue("X-Auth") authCookie : String?) {
        JwtSigner.validateJwt(authCookie.toString()).getOrHandle { throw it }
        TODO("modificar la especialización del municipio entre producción o defensa")
        // TODO("definir Body")
    }

    @GetMapping("/{id}/towns/{idTown}")
    @ApiOperation(value = "Gets the town stats and an image")
    fun getTownData(
            @PathVariable("id") id: Int,
            @PathVariable("idTown") idTown: Int,
            @CookieValue("X-Auth") authCookie : String?) {
        JwtSigner.validateJwt(authCookie.toString()).getOrHandle { throw it }
        TODO("visualizar las estadísticas de producción y defensa de cada municipio, y la imagen")
    }

    @GetMapping("/provinces")
    @ApiOperation(value = "Gets all provinces")
    fun getProvinces(@CookieValue("X-Auth") authCookie : String?) : List<Province> {
        JwtSigner.validateJwt(authCookie.toString()).getOrHandle { throw it }
        TODO("obtener provincias para que el usuario pueda seleccionar en que provincia quiere jugar")
    }

    @ExceptionHandler(CustomException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleDomainException(exception: CustomException) = exception.getJSON()

    @ExceptionHandler(CustomException.ExpiredTokenException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleExpiredTokenError(exception: CustomException) = exception.getJSON()
}
