package com.grupox.wololo.controllers

import arrow.core.getOrHandle
import arrow.core.toOption
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Game
import com.grupox.wololo.model.helpers.JwtSigner
import com.grupox.wololo.model.User
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.util.*

@RequestMapping("/admin")
@RestController
class AdminController {
    @GetMapping("/games")
    @ApiOperation(value = "Gets the games stats")
    fun getGamesStats(@RequestParam("from", required = false) from: Date?,
                      @RequestParam("to", required = false) to: Date?,
                      @ApiIgnore @CookieValue("X-Auth") authCookie : String?): List<Game> {
        JwtSigner.validateJwt(authCookie.toOption()).getOrHandle { throw it }
        // Seguramente no devuelva una lista de games sino algo como lista de GameStats (a confirmar)
        TODO("proveer estadísticas de cantidad de partidas creadas, en curso, " +
                "terminadas y canceladas permitiendo seleccionar el rango de fechas")
    }

    @GetMapping("/scoreboard")
    @ApiOperation(value = "Gets the scoreboard")
    fun getScoreBoard(@ApiIgnore @CookieValue("X-Auth") authCookie : String?): List<User> {
        JwtSigner.validateJwt(authCookie.toOption()).getOrHandle { throw it }
        TODO("devolver el scoreboard")
    }

    @GetMapping("/users")
    @ApiOperation(value = "Gets the users stats")
    fun getUsersStats(@RequestParam("username", required = false) username: String?,
                      @ApiIgnore @CookieValue("X-Auth") authCookie : String?): List<User> {
        JwtSigner.validateJwt(authCookie.toOption()).getOrHandle { throw it }
        // Seguramente no devuelva una lista de users sino algo como lista de UserStats (a confirmar)
        TODO("proveer estadísticas de los usuarios permitiendo seleccionar un usuario particular")
    }

    @ExceptionHandler(CustomException.NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundError(exception: CustomException) = exception.getJSON()

    @ExceptionHandler(CustomException.TokenException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleExpiredTokenError(exception: CustomException) = exception.getJSON()
}