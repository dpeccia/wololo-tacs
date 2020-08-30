package com.grupox.wololo.controllers

import com.grupox.wololo.errors.ExceptionData
import com.grupox.wololo.errors.NotFoundException
import com.grupox.wololo.model.JWT
import com.grupox.wololo.model.LoginModel
import com.grupox.wololo.model.RepoUsers
import com.grupox.wololo.model.User
import java.util.*
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/users")
@RestController
class UsersController {
    @GetMapping()
    fun getUsers(): ArrayList<User> = RepoUsers.getUsers()

    @GetMapping("/{id}")
    fun getUser(@PathVariable("id") id: Int) = RepoUsers.getUserById(id) ?: throw NotFoundException("User was not found")

    @PostMapping()
    fun createUser(@RequestBody user: User) { // que agarre el request body del request
        // TODO cambiar que no reciba todo el usuario por un json
        RepoUsers.insertUser(user)
    }

    @PostMapping("/tokens")
    fun createToken(@RequestBody user: LoginModel): JWT = JWT("token example", "1h")

    @DeleteMapping("/tokens")
    fun deleteToken(@RequestBody token: JWT): String = "deleted"

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundError(exception: NotFoundException) = ExceptionData(exception.message)
}
