package com.grupox.wololo.controllers

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.JWT
import com.grupox.wololo.model.LoginModel
import com.grupox.wololo.model.RepoUsers
import com.grupox.wololo.model.User
import com.grupox.wololo.model.Stats
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/users")
@RestController
class UsersController {
    @PostMapping
    @ApiOperation(value = "Creates a new user (Sign Up / Register)")
    fun createUser(@RequestBody user: User) { // que agarre el request body del request
        // TODO cambiar que no reciba todo el usuario por un json
        RepoUsers.insertUser(user)
    }

    @PostMapping("/tokens")
    @ApiOperation(value = "Log In")
    fun login(@RequestBody user: LoginModel): JWT {
        var user = RepoUsers.getUserByLogin(user) ?: throw CustomException.NotFoundException("Bad Login")

        // TODO CREATE JWT
        return JWT("${user.mail}.${user.nombre}", "1h")
    }

    @DeleteMapping("/tokens")
    @ApiOperation(value = "Log Out")
    fun logout(@RequestBody token: JWT): Nothing = TODO("implementar borrado de token")

    @GetMapping
    @ApiOperation(value = "Gets the users without stats")
    fun getUsers(@RequestParam("username", required = false) username: String?): List<User> = RepoUsers.getUsers()
    // TODO obtener usuarios o un usuario en particular (sin stats)

    @ExceptionHandler(CustomException.NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundError(exception: CustomException) = exception.getJSON()
}
