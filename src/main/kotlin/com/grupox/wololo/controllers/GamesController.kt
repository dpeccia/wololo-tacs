package com.grupox.wololo.controllers

import com.grupox.wololo.model.Game
import com.grupox.wololo.model.RepoGames
import com.grupox.wololo.model.User
import org.springframework.web.bind.annotation.*
import java.util.*

@RequestMapping("v1/games")
class GamesController {
    @GetMapping
    fun getGames() :List<Game>{
        return RepoGames.getGames()
    }
    @PostMapping
    fun createGame(@RequestBody game: Game){
        RepoGames.insertGame(game)
    }
    @PutMapping(path = arrayOf("id"))
    fun updateGame(@PathVariable("id") id : UUID) {
        RepoGames.updateGame(id)
    }
}