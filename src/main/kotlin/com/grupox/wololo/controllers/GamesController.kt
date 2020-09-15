package com.grupox.wololo.controllers

import arrow.core.*
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.fx
import arrow.core.extensions.list.traverse.sequence

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.GameForm
import com.grupox.wololo.model.helpers.JwtSigner
import com.grupox.wololo.model.helpers.ProvinceGeoRef
import com.grupox.wololo.model.helpers.TownForm
import com.grupox.wololo.model.helpers.*
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers
import com.grupox.wololo.model.externalservices.GeoRef
import com.grupox.wololo.model.externalservices.TopoData
import com.grupox.wololo.services.GamesControllerService
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.util.*
import kotlin.collections.ArrayList


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
                 @ApiIgnore @CookieValue("X-Auth") authCookie : String?): List<Game> {
        val token = checkAndGetToken(authCookie)
        val userId = token.body.subject.toInt()
        return gamesControllerService.getGames(userId, sort, status, date)
    }

    @PostMapping
    @ApiOperation(value = "Creates a new game")
    fun createGame(@RequestBody form: GameForm, @ApiIgnore @CookieValue("X-Auth") authCookie : String?) {
        checkAndGetToken(authCookie)
        gamesControllerService.createGame(form)
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Gets a game")
    fun getGameById(@PathVariable("id") id: Int,
                    @ApiIgnore @CookieValue("X-Auth") authCookie : String?): Game {
        val token = checkAndGetToken(authCookie)
        val userId = token.body.subject.toInt()
        return gamesControllerService.getGame(id, userId)
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Surrenders in a game (it becomes CANCELED)")
    fun surrender(@PathVariable("id") id: Int, @ApiIgnore @CookieValue("X-Auth") authCookie : String?) {
        val token = checkAndGetToken(authCookie)
        val userId: Int = token.body.subject.toInt()
        gamesControllerService.surrender(id, userId)
    }

    @PostMapping("/{id}/actions/movement")
    @ApiOperation(value = "Moves the gauchos between towns")
    fun moveGauchosBetweenTowns(
            @PathVariable("id") id: Int,
            @RequestBody movementData: MovementForm,
            @ApiIgnore @CookieValue("X-Auth") authCookie : String?) {
        val userId = checkAndGetToken(authCookie).body.subject.toInt()
        gamesControllerService.moveGauchosBetweenTowns(userId, id, movementData)
    }

    @PostMapping("/{id}/actions/attack")
    @ApiOperation(value = "Attacks a town")
    fun attackTown(
            @PathVariable("id") id: Int,
            @RequestBody attackData: AttackForm,
            @ApiIgnore @CookieValue("X-Auth") authCookie : String?) {
        val userId = checkAndGetToken(authCookie).body.subject.toInt()
        gamesControllerService.attackTown(userId, id, attackData)
    }

    @PutMapping("/{id}/towns/{idTown}")
    @ApiOperation(value = "Updates the town specialization")
    fun updateTownSpecialization(
            @PathVariable("id") id: Int,
            @PathVariable("idTown") townId: Int,
            @RequestBody newSpecialization: String,
            @ApiIgnore @CookieValue("X-Auth") authCookie : String?) {
        val token = checkAndGetToken(authCookie)
        val userId = token.body.subject.toInt()
        gamesControllerService.updateTownSpecialization(userId, id, townId, newSpecialization)
    }

    @GetMapping("/{id}/towns/{idTown}")
    @ApiOperation(value = "Gets the town stats and an image")
    fun getTownData(
            @PathVariable("id") id: Int,
            @PathVariable("idTown") idTown: Int,
            @ApiIgnore @CookieValue("X-Auth") authCookie : String?) {
        checkAndGetToken(authCookie)
        TODO("visualizar las estadísticas de producción y defensa de cada municipio, y la imagen")
    }

    @GetMapping("/provinces")
    @ApiOperation(value = "Gets all provinces")
    fun getProvinces(@ApiIgnore @CookieValue("X-Auth") authCookie : String?) : List<ProvinceGeoRef> {
        checkAndGetToken(authCookie)
        return gamesControllerService.getProvinces()
    }
}
