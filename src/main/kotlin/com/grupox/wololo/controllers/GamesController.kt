package com.grupox.wololo.controllers

import com.grupox.wololo.errors.NotFoundException
import com.grupox.wololo.model.Game
import com.grupox.wololo.model.RepoGames
import com.grupox.wololo.model.Town
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
    fun getGameById(@PathVariable("id") id: Int): Game = RepoGames.getGameById(id) ?: throw NotFoundException("Game was not found")

    @PutMapping("/{id}")
    fun updateGame(@PathVariable("id") id: Int) {
        val game: Game = RepoGames.getGameById(id) ?: throw NotFoundException("Game was not found")
        TODO("UPDATE GAME VALUE")
    }

    @PutMapping("/{id}/town/{idTown}")
    fun updateTown(@PathVariable("id") id: Int, @PathVariable("idTown") idTown: Int) {
        val town: Town = RepoGames.getGameById(id)?.getTownById(idTown) ?: throw NotFoundException("Town was not found")
        TODO("UPDATE TOWN VALUE")
    }


    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundException(exception: NotFoundException) = exception.getJSON()
}
