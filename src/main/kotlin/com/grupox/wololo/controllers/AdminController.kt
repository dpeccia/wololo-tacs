package com.grupox.wololo.controllers

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Game
import com.grupox.wololo.model.User
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/admin")
@RestController
class AdminController {
    @GetMapping("/games")
    fun getGamesStats(): List<Game> {
        TODO()
    }

    @GetMapping("/scoreboard")
    fun getScoreBoard(): List<User> {
        TODO()
    }

    @GetMapping("/users")
    fun getUsersStats(): List<User> {
        TODO()
    }

    @ExceptionHandler(CustomException.NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundError(exception: CustomException) = exception.getJSON()
}