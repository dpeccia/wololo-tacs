package com.grupox.wololo.controllers

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.grupox.wololo.errors.UserNotFound
import com.grupox.wololo.errors.UserNotFoundException
import com.grupox.wololo.model.RepoUsers
import com.grupox.wololo.model.User
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.lang.IllegalArgumentException
import java.util.*

@RequestMapping("/users")
@RestController
class UsersController {

    @GetMapping()
    fun getUsers(): ArrayList<User> = RepoUsers.getUsers()

    @GetMapping("/{id}")
    fun getUser(@PathVariable("id") id: Int) = RepoUsers.getUser(id) ?: throw UserNotFoundException("hola")

    @PostMapping
    fun createUser(@RequestBody user: User){ //que agarre el request body del request
        // TODO cambiar que no reciba todo el usuario por un json
        RepoUsers.createUser(user)
    }

    @ExceptionHandler(UserNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleUserNotFoundError(exception: UserNotFoundException) = UserNotFound(exception.message)
}