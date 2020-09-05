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

    @PutMapping("/{id}/town/{idTown}")
    fun updateTown(@PathVariable("id") id: Int, @PathVariable("idTown") idTown: Int) {
        val town: Town = RepoGames.getGameById(id)?.getTownById(idTown) ?: throw CustomException.NotFoundException("Town was not found")
        TODO("UPDATE TOWN VALUE")
    }

    @GetMapping("/provincias/{name}")
    fun getProvincia(@PathVariable("name") name: String): Province {   // Solo para debugging, este no va a ser un endpoint
        return GeoRef.generateProvince(name)
                .getOrHandle { throw it }
    }

    @GetMapping("/stats")
    fun getStats(@RequestParam allParams: Map<String, String>): Stats {
        // TODO ADMIN ONLY
        TODO("GET GAMES STATS")
    }

    @GetMapping("/scoreboard")
    fun getScoreboard(): List<User> {
        // TODO ADMIN ONLY
        TODO("GET SCOREBOARD")
    }

    @ExceptionHandler(CustomException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleDomainException(exception: CustomException) = exception.getJSON()
}
