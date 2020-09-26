package com.grupox.wololo.controllers

import com.grupox.wololo.model.Status
import com.grupox.wololo.model.User
import com.grupox.wololo.model.helpers.*
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers
import com.grupox.wololo.services.GamesControllerService
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.util.*
import javax.servlet.http.HttpServletRequest

@RequestMapping("/games")
@RestController
class GamesController : BaseController() {
    @Autowired
    lateinit var gamesControllerService: GamesControllerService

    @GetMapping
    @ApiOperation(value = "Gets the games of the current user. Params: sort=id|date|numberOfTowns|numberOfPlayers & status & date")
    fun getGames(@RequestParam("sort", required = false) sort: String?, // TODO query params
                 @RequestParam("status", required = false) status: Status?,
                 @RequestParam("date", required = false) date: Date?,
                 request: HttpServletRequest): List<DTO.GameDTO> {
        val token = checkAndGetToken(request)
        val userId = token.body.subject.toInt()
        return gamesControllerService.getGames(userId, sort, status, date)
    }

    @PostMapping
    @ApiOperation(value = "Creates a new game")
    fun createGame(@RequestBody form: GameForm, request: HttpServletRequest): DTO.GameDTO {
        val token = checkAndGetToken(request)
        val userId = token.body.subject.toInt()
        return gamesControllerService.createGame(userId, form)
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Gets a game")
    fun getGameById(@PathVariable("id") id: Int, request: HttpServletRequest): DTO.GameDTO {
        val token = checkAndGetToken(request)
        val userId = token.body.subject.toInt()
        return gamesControllerService.getGame(userId, id)
    }

    @GetMapping("/stats")
    @ApiOperation(value = "Gets games stats from a date range")
    fun getGamesStats(@RequestParam("from", required = false) from: Date,
                      @RequestParam("to", required = false) to: Date,

                      @ApiIgnore @CookieValue("X-Auth") authCookie : String?,
                      request: HttpServletRequest): GamePublicInfo {

        val token = checkAndGetToken(request)
        val userId = token.body.subject.toInt()

        throwIfNotAllowed(userId)

        return gamesControllerService.getGamesStats(from, to)

    }

    @GetMapping("/date")
    @ApiOperation(value = "Gets games from a date range")
    fun getGamesByDateRange(@RequestParam("from", required = false) from: Date,
                      @RequestParam("to", required = false) to: Date,

                      @ApiIgnore @CookieValue("X-Auth") authCookie : String?,
                      request: HttpServletRequest): List<DTO.GameDTO> {
        val token = checkAndGetToken(request)
        val userId = token.body.subject.toInt()
        throwIfNotAllowed(userId)
        return gamesControllerService.getGamesInADateRange(from, to)

    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Surrenders in a game (it becomes CANCELED)")
    fun surrender(@PathVariable("id") id: Int, request: HttpServletRequest): DTO.GameDTO {
        val token = checkAndGetToken(request)
        val userId: Int = token.body.subject.toInt()
        return gamesControllerService.surrender(id, userId)
    }

    @PutMapping("/{id}/actions/turn")
    @ApiOperation(value = "Finishes the current Turn")
    fun finishTurn(@PathVariable("id") id: Int, request: HttpServletRequest): DTO.GameDTO {
        val userId = checkAndGetToken(request).body.subject.toInt()
        return gamesControllerService.finishTurn(userId, id)
    }

    @PostMapping("/{id}/actions/movement")
    @ApiOperation(value = "Moves the gauchos between towns")
    fun moveGauchosBetweenTowns(
            @PathVariable("id") id: Int,
            @RequestBody movementData: MovementForm,
            request: HttpServletRequest): DTO.GameDTO {
        val userId = checkAndGetToken(request).body.subject.toInt()
        return gamesControllerService.moveGauchosBetweenTowns(userId, id, movementData)
    }

    @PostMapping("/{id}/actions/attack")
    @ApiOperation(value = "Attacks a town")
    fun attackTown(
            @PathVariable("id") id: Int,
            @RequestBody attackData: AttackForm,
            request: HttpServletRequest): DTO.GameDTO {
        val userId = checkAndGetToken(request).body.subject.toInt()
        return gamesControllerService.attackTown(userId, id, attackData)
    }

    @PutMapping("/{id}/towns/{idTown}")
    @ApiOperation(value = "Updates the town specialization")
    fun updateTownSpecialization(
            @PathVariable("id") id: Int,
            @PathVariable("idTown") townId: Int,
            @RequestBody newSpecialization: String,
            request: HttpServletRequest): DTO.GameDTO {
        val token = checkAndGetToken(request)
        val userId = token.body.subject.toInt()
        return gamesControllerService.updateTownSpecialization(userId, id, townId, newSpecialization)
    }

    @GetMapping("/{id}/towns/{idTown}")
    @ApiOperation(value = "Gets the town stats and an image")
    fun getTownData(
            @PathVariable("id") id: Int,
            @PathVariable("idTown") idTown: Int,
            request: HttpServletRequest) : DTO.TownDTO {
        checkAndGetToken(request)
        return gamesControllerService.getTownStats(id, idTown)
    }

    @GetMapping("/provinces")
    @ApiOperation(value = "Gets all provinces")
    fun getProvinces(request: HttpServletRequest) : List<String> {
        checkAndGetToken(request)
        return gamesControllerService.getProvinces()
    }
}
