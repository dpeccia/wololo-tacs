package com.grupox.wololo.controllers

import com.grupox.wololo.model.User
import com.grupox.wololo.model.helpers.DTO
import com.grupox.wololo.model.helpers.GamePublicInfo
import com.grupox.wololo.services.AdminControllerService
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
    fun getGamesStats(@RequestParam("from") from: Date,
                      @RequestParam("to") to: Date,
                      @ApiIgnore @CookieValue("X-Auth") authCookie : String?): GamePublicInfo {
        checkAndGetToken(authCookie)
        return adminControllerService.getGamesStats(from, to)
    }

    //Esto creo que conviene separarlo en dos requests, porque o devuelvo una lista, o devuelvo uno solo
    @GetMapping("/scoreboard")
    @ApiOperation(value = "Gets the scoreboard")
    fun getScoreBoard(@ApiIgnore @CookieValue("X-Auth") authCookie : String?): List<DTO.UserDTO> {
        checkAndGetToken(authCookie)
        return adminControllerService.getScoreBoard()
    }

    @GetMapping("/scoreboard/{id}")
    @ApiOperation(value = "Gets the scoreboard")
    fun getScoreBoardById(@RequestParam("username", required = false) id: Int, @ApiIgnore @CookieValue("X-Auth") authCookie : String?): DTO.UserDTO {
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