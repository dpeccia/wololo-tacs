package com.grupox.wololo.controllers

import arrow.core.extensions.list.functorFilter.filter
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.GamePublicInfo
import com.grupox.wololo.model.helpers.UserPublicInfo
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers
import com.grupox.wololo.services.AdminControllerService
import com.grupox.wololo.services.GamesControllerService
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

    @GetMapping("/games")
    @ApiOperation(value = "Gets the games stats")
    fun getGamesStats(@RequestParam("from", required = false) from: Date,
                      @RequestParam("to", required = false) to: Date,
                      @ApiIgnore @CookieValue("X-Auth") authCookie : String?): GamePublicInfo {

        val games: List<Game> = RepoGames.getAll().filter { it.date >= from && it.date <= to  }

        fun numberOfGames(status : String) : Int {
            return games.map { it.status }.filter { it.toString() == status }.count()
        }

        checkAndGetToken(authCookie)

        return GamePublicInfo(numberOfGames("NEW"), numberOfGames("ONGOING"), numberOfGames("FINISHED"), numberOfGames("CANCELED"), games)

    }

    //Esto creo que conviene separarlo en dos requests, porque o devuelvo una lista, o devuelvo uno solo
    @GetMapping("/scoreboard")
    @ApiOperation(value = "Gets the scoreboard")
    fun getScoreBoard(@ApiIgnore @CookieValue("X-Auth") authCookie : String?): List<UserPublicInfo> {
        checkAndGetToken(authCookie)
        return adminControllerService.getScoreBoard()
    }

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