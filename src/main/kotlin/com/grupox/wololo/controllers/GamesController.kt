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
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RequestMapping("/games")
@RestController
class GamesController {
    @GetMapping
    fun getGames(@RequestParam("sort") sort: String, @RequestParam("filter") filter: String): List<Game> = RepoGames.getGames()
    // TODO obtener mis partidas y filtrar u ordenar por fecha y estado

    @PostMapping
    fun createGame(@RequestBody game: Game) {
        RepoGames.insertGame(game)
    }

    @GetMapping("/{id}")
    fun getGameById(@PathVariable("id") id: Int): Game = RepoGames.getGameById(id) ?: throw CustomException.NotFoundException("Game was not found")

    @PutMapping("/{id}")
    fun updateGame(@PathVariable("id") id: Int) {
        val game: Game = RepoGames.getGameById(id) ?: throw CustomException.NotFoundException("Game was not found")
        TODO("modificar estado de una partida")
        // TODO("definir Body")
    }

    @PostMapping("/{id}/actions/movement")
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
    fun attackTown(
            @PathVariable("id") id: Int,
            @RequestParam("attacker") attackerId: Int,
            @RequestParam("defender") defenderId: Int
    ) {
        TODO("logica de ataque")
        // TODO("definir Body")
    }

    @PutMapping("/{id}/towns/{idTown}")
    fun updateTownSpecialization(
            @PathVariable("id") id: Int,
            @PathVariable("playerId") playerId: Int,
            @PathVariable("specialization") specialization: Int // TODO change int to specialization type
    ) {
        TODO("modificar la especialización del municipio entre producción o defensa")
        // TODO("definir Body")
    }

    @GetMapping("/{id}/towns/{idTown}")
    fun getTownData(
            @PathVariable("id") id: Int,
            @PathVariable("idTown") idTown: Int
    ) {
        TODO("visualizar las estadísticas de producción y defensa de cada municipio, y la imagen")
    }

    @GetMapping("/provinces")
    fun getProvinces() : List<Province> {
        TODO("obtener provincias para que el usuario pueda seleccionar en que provincia quiere jugar")
    }

    @ExceptionHandler(CustomException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleDomainException(exception: CustomException) = exception.getJSON()
}
