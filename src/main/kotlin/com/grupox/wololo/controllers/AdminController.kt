package com.grupox.wololo.controllers

import arrow.core.Either
import arrow.core.extensions.list.functorFilter.filter
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.GamePublicInfo
import com.grupox.wololo.model.helpers.UserPublicInfo
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers
import com.grupox.wololo.services.AdminControllerService
import com.grupox.wololo.services.GamesControllerService
import com.grupox.wololo.services.UsersControllerService
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.util.*

@RequestMapping("/admin")
@RestController
class AdminController : BaseController() {

    @Autowired
    lateinit var adminControllerService: AdminControllerService

    @Autowired
    lateinit var userControllerService: UsersControllerService

    @GetMapping("/games")
    @ApiOperation(value = "Gets the games stats")
    fun getGamesStats(@RequestParam("from", required = false) from: Date,
                      @RequestParam("to", required = false) to: Date,
                      @ApiIgnore @CookieValue("X-Auth") authCookie : String?): GamePublicInfo {
        checkAndGetToken(authCookie)

        return adminControllerService.getGamesStats(from, to)

    }

    //Lo separé en dos requests, si no hay que hacer como un if else para ver si recibis o no el id opcional, pero como quieran
    @GetMapping("/scoreboard")
    @ApiOperation(value = "Gets the scoreboard")
    fun getScoreBoard(@ApiIgnore @CookieValue("X-Auth") authCookie : String?): List<UserPublicInfo> {
        checkAndGetToken(authCookie)
        val token = checkAndGetToken(authCookie)
        val userId: Int = token.body.subject.toInt()

    userControllerService.checkIfIsAdmin(userId)

        return adminControllerService.getScoreBoard()}

    @GetMapping("/scoreboard/{id}")
    @ApiOperation(value = "Gets the scoreboard")
    fun getScoreBoardById(@RequestParam("username", required = false) id: Int, @ApiIgnore @CookieValue("X-Auth") authCookie : String?): UserPublicInfo {
        checkAndGetToken(authCookie)
        return adminControllerService.getScoreBoardById(id)
    }

    @GetMapping("/users")
    @ApiOperation(value = "Gets the users stats")
    fun getUsersStats(@RequestParam("username", required = false) username: String?,
                      @ApiIgnore @CookieValue("X-Auth") authCookie : String?): List<User> {
        checkAndGetToken(authCookie)
        // Seguramente no devuelva una lista de users sino algo como lista de UserStats (a confirmar)

        TODO("proveer estadísticas de los usuarios permitiendo seleccionar un usuario particular")
    }
}