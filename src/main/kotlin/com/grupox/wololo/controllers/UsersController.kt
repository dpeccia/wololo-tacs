package com.grupox.wololo.controllers

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.JWT
import com.grupox.wololo.model.LoginModel
import com.grupox.wololo.model.RepoUsers
import com.grupox.wololo.model.User
import com.grupox.wololo.model.Stats
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/users")
@RestController
class UsersController {
    @PostMapping
    fun createUser(@RequestBody user: User) { // que agarre el request body del request
        // TODO cambiar que no reciba todo el usuario por un json
        RepoUsers.insertUser(user)
    }

    @PostMapping("/tokens")
    fun login(@RequestBody user: LoginModel): JWT {
        var user = RepoUsers.getUserByLogin(user) ?: throw CustomException.NotFoundException("Bad Login")

        // TODO CREATE JWT
        return JWT("${user.mail}.${user.nombre}", "1h")
    }

    @DeleteMapping("/tokens")
    fun logout(@RequestBody token: JWT): Nothing = TODO("implementar borrado de token")

    @GetMapping
    fun getUsers(@RequestParam("username") username: String): List<User> = RepoUsers.getUsers()
    // TODO obtener usuarios o un usuario en particular (sin stats)

    @ExceptionHandler(CustomException.NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundError(exception: CustomException) = exception.getJSON()
}
