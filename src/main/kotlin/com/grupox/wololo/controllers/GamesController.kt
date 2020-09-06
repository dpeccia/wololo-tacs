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

@RequestMapping("/games")
@RestController
class GamesController {
    @GetMapping
    fun getGames(): List<Game> = RepoGames.getGames()

    @PostMapping
    fun createGame(@RequestBody game: Game) {
        RepoGames.insertGame(game)
    }

    @GetMapping("/{id}")
    fun getGameById(@PathVariable("id") id: Int): Game = RepoGames.getGameById(id) ?: throw CustomException.NotFoundException("Game was not found")

    @PutMapping("/{id}")
    fun updateGame(@PathVariable("id") id: Int) {
        val game: Game = RepoGames.getGameById(id) ?: throw CustomException.NotFoundException("Game was not found")
        TODO("UPDATE GAME VALUE")
    }

    @PostMapping("/{id}/actions/movement")
    fun moveGauchosBetweenTowns(
            @PathVariable("id") id: Int,
            @PathVariable("playerId") playerId: Int,
            @RequestParam("from") fromTownId: Int,
            @RequestParam("to") toTownId: Int,
            @RequestParam("quantity") gauchosQuantity: Int) {
        TODO("MOVE GAUCHOS BETWEEN TOWNS")
    }

    @PostMapping("/{id}/actions/attack")
    fun attackTown(
            @PathVariable("id") id: Int,
            @RequestParam("attacker") attackerId: Int,
            @RequestParam("defender") defenderId: Int
    ) {
        TODO("ATTACK LOGIC")
    }

    @PutMapping("/{id}/towns/{idTown}")
    fun updateTownSpecialization(
            @PathVariable("id") id: Int,
            @PathVariable("playerId") playerId: Int,
            @PathVariable("specialization") specialization: Int // TODO change int to specialization type
    ) {
        TODO("UPDATE SPECIALIZATION LOGIC")
    }

    @GetMapping("/{id}/towns/{idTown}")
    fun getTownData(
            @PathVariable("id") id: Int,
            @PathVariable("idTown") idTown: Int
    ) {
        TODO("GET TOWN STATISTICS OF PRODUCTION AND DEFENSE")
    }

    @GetMapping("/provinces")
    fun getProvinces() : List<Province> {
        TODO()
    }

    @ExceptionHandler(CustomException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleDomainException(exception: CustomException) = exception.getJSON()
}
