package com.grupox.wololo.controllers

import arrow.core.getOrHandle
import arrow.core.toOption
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.GameForm
import com.grupox.wololo.model.helpers.JwtSigner
import com.grupox.wololo.model.helpers.ProvinceGeoRef
import com.grupox.wololo.model.helpers.TownForm
import com.grupox.wololo.model.services.GeoRef
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore

@RequestMapping("/games")
@RestController
class GamesController {
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
    fun createGame(@RequestBody game: GameForm, @ApiIgnore @CookieValue("X-Auth") authCookie : String?) {
        JwtSigner.validateJwt(authCookie.toOption()).getOrHandle { throw it }
        TODO("Crear un game a partir del GameForm")
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Gets a game")
    fun getGameById(@PathVariable("id") id: Int,
                    @ApiIgnore @CookieValue("X-Auth") authCookie : String?): Game {
        JwtSigner.validateJwt(authCookie.toOption()).getOrHandle { throw it }
        return RepoGames.getGameById(id) ?: throw CustomException.NotFoundException("Game was not found")
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Modifies a game status (on going, finished, canceled)")
    fun updateGame(@PathVariable("id") id: Int, @RequestBody gameData: GameForm, @ApiIgnore @CookieValue("X-Auth") authCookie : String?)  {

        val userID : Int = JwtSigner.validateJwt(authCookie.toOption()).getOrHandle { throw it }.body.subject.toInt()
        val game: Game = RepoGames.getGameById(id) ?: throw CustomException.NotFoundException("Game was not found")
        val participantsIds: List<Int> = gameData.participantsIds

        RepoUsers.updateUserGamesLost(userID)

        if ((participantsIds.size) <= 2) {
            RepoGames.changeGameStatus(id, "CANCELED")
            RepoUsers.updateUserGamesWon(participantsIds.find { it != userID })
        }

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
            //por ahí mejor una función dentro de repogames que se encarge de hacer esto porque creo que rompo encapsulamiento, despues de adr lo cambio
            RepoGames.getGameById(id)?.getTownById(idTown)?.changeSpecialization(Production())
        } else{
            if (townData.specialization == "DEFENSE"){
                RepoGames.getGameById(id)?.getTownById(idTown)?.changeSpecialization(Defense())
            }
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
        return GeoRef.requestAvailableProvinces().getOrHandle { throw it }
    }

    @ExceptionHandler(CustomException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleDomainException(exception: CustomException) = exception.getJSON()

    @ExceptionHandler(CustomException.TokenException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleExpiredTokenError(exception: CustomException) = exception.getJSON()
}
