package com.grupox.wololo.controllers

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.JWT
import com.grupox.wololo.model.LoginModel
import com.grupox.wololo.model.RepoUsers
import com.grupox.wololo.model.User
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/users")
@RestController
class UsersController {
    @GetMapping()
    fun getUsers(): List<User> = RepoUsers.getUsers()

    @GetMapping("/{id}")
    fun getUserById(@PathVariable("id") id: Int): User = RepoUsers.getUserById(id) ?: throw CustomException.NotFoundException("User was not found")

    @GetMapping("/{id}/stats")
    fun getUserStats(@PathVariable("id") id: Int): Stats {
        // TODO ADMIN ONLY
        TODO("GET USER STATS")
    }

    @PostMapping()
    fun createUser(@RequestBody user: User) { // que agarre el request body del request
        // TODO cambiar que no reciba todo el usuario por un json
        RepoUsers.insertUser(user)
    }

    @PostMapping("/tokens")
    fun createToken(@RequestBody user: LoginModel): JWT {
        var user = RepoUsers.getUserByLogin(user) ?: throw CustomException.NotFoundException("Bad Login")

        // TODO CREATE JWT
        return JWT("${user.mail}.${user.nombre}", "1h")
    }

    @DeleteMapping("/tokens")
    fun deleteToken(@RequestBody token: JWT): Nothing = TODO("IMPLEMENT TOKEN DELETION")

    @ExceptionHandler(CustomException.NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundError(exception: CustomException) = exception.getJSON()
}
