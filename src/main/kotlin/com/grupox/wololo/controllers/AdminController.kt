package com.grupox.wololo.controllers

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Game
import com.grupox.wololo.model.User
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RequestMapping("/admin")
@RestController
class AdminController {
    @GetMapping("/games")
    fun getGamesStats(@RequestParam("from") from: Date, @RequestParam("to") to: Date): List<Game> {
        // Seguramente no devuelva una lista de games sino algo como lista de GameStats (a confirmar)
        TODO("proveer estadísticas de cantidad de partidas creadas, en curso, " +
                "terminadas y canceladas permitiendo seleccionar el rango de fechas")
    }

    @GetMapping("/scoreboard")
    fun getScoreBoard(): List<User> {
        TODO("devolver el scoreboard")
    }

    @GetMapping("/users")
    fun getUsersStats(@RequestParam("username") username: String): List<User> {
        // Seguramente no devuelva una lista de users sino algo como lista de UserStats (a confirmar)
        TODO("proveer estadísticas de los usuarios permitiendo seleccionar un usuario particular")
    }

    @ExceptionHandler(CustomException.NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundError(exception: CustomException) = exception.getJSON()
}