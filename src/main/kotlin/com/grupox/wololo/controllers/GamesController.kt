package com.grupox.wololo.controllers

import com.grupox.wololo.errors.ExceptionData
import com.grupox.wololo.errors.NotFoundException
import com.grupox.wololo.model.Game
import com.grupox.wololo.model.RepoGames
import com.grupox.wololo.model.dtos.DTO
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/games")
@RestController
class GamesController {
    @GetMapping
    fun getGames(): List<DTO> = RepoGames.getGames().map { it.getDTO() }

    @PostMapping
    fun createGame(@RequestBody game: Game) {
        RepoGames.insertGame(game)
    }

    @GetMapping("/{id}")
    fun getGameById(@PathVariable("id") id: Int): DTO = RepoGames.getGameById(id)?.getDTO() ?: throw NotFoundException("Game was not found")

    @PutMapping("/{id}")
    fun updateGame(@PathVariable("id") id: Int) {
        val game: Game = RepoGames.getGameById(id) ?: throw NotFoundException("Game was not found")
        TODO("UPDATE GAME VALUE")
    }

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundException(exception: NotFoundException) = ExceptionData(exception.message)
}
