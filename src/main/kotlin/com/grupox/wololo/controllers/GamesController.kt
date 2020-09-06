package com.grupox.wololo.controllers

import arrow.core.getOrHandle
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Game
import com.grupox.wololo.model.Province
import com.grupox.wololo.model.RepoGames
import com.grupox.wololo.model.Town
import com.grupox.wololo.model.Stats
import com.grupox.wololo.model.User
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
                 @RequestParam("filter", required = false) filter: String?): List<Game> = RepoGames.getGames()
    // TODO obtener mis partidas y filtrar u ordenar por fecha y estado

    @PostMapping
    @ApiOperation(value = "Creates a new game")
    fun createGame(@RequestBody game: Game) {
        RepoGames.insertGame(game)
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Gets a game")
    fun getGameById(@PathVariable("id") id: Int): Game = RepoGames.getGameById(id) ?: throw CustomException.NotFoundException("Game was not found")

    @PutMapping("/{id}")
    @ApiOperation(value = "Modifies a game status (on going, finished, canceled)")
    fun updateGame(@PathVariable("id") id: Int) {
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
            @RequestParam("quantity") gauchosQuantity: Int) {
        TODO("mover gauchos de un municipio a otro")
        // TODO("definir Body")
    }

    @PostMapping("/{id}/actions/attack")
    @ApiOperation(value = "Attacks a town")
    fun attackTown(
            @PathVariable("id") id: Int,
            @RequestParam("attacker") attackerId: Int,
            @RequestParam("defender") defenderId: Int
    ) {
        TODO("logica de ataque")
        // TODO("definir Body")
    }

    @PutMapping("/{id}/towns/{idTown}")
    @ApiOperation(value = "Updates the town specialization")
    fun updateTownSpecialization(
            @PathVariable("id") id: Int,
            @PathVariable("playerId") playerId: Int,
            @PathVariable("specialization") specialization: Int // TODO change int to specialization type
    ) {
        TODO("modificar la especialización del municipio entre producción o defensa")
        // TODO("definir Body")
    }

    @GetMapping("/{id}/towns/{idTown}")
    @ApiOperation(value = "Gets the town stats and an image")
    fun getTownData(
            @PathVariable("id") id: Int,
            @PathVariable("idTown") idTown: Int
    ) {
        TODO("visualizar las estadísticas de producción y defensa de cada municipio, y la imagen")
    }

    @GetMapping("/provinces")
    @ApiOperation(value = "Gets all provinces")
    fun getProvinces() : List<Province> {
        TODO("obtener provincias para que el usuario pueda seleccionar en que provincia quiere jugar")
    }

    @ExceptionHandler(CustomException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleDomainException(exception: CustomException) = exception.getJSON()
}
