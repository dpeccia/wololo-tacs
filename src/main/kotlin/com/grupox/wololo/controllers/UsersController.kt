package com.grupox.wololo.controllers

import arrow.core.getOrHandle
import arrow.core.toOption
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.JwtSigner
import com.grupox.wololo.model.helpers.UserCredentials
import com.grupox.wololo.model.helpers.UserWithoutStats
import com.grupox.wololo.services.UsersService
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore

@RequestMapping("/users")
@RestController
class UsersController {
    @Autowired
    lateinit var usersService: UsersService

    @PostMapping
    @ApiOperation(value = "Creates a new user (Sign Up / Register)")
    fun createUser(@RequestBody newUser: UserCredentials) {
        if(RepoUsers.getUserByName(newUser.mail).nonEmpty())
            throw CustomException.NotFoundException("User already exists")
        val user = User(3, newUser.mail, newUser.password, false, Stats(0,0)) // TODO el id se tiene que autoincrementar
        RepoUsers.insertUser(user)
    }

    @PostMapping("/tokens")
    @ApiOperation(value = "Log In")
    fun login(@RequestBody _user: UserCredentials): ResponseEntity<Void> {
        val user = RepoUsers.getUserByLogin(_user) ?: throw CustomException.BadLoginException("Bad Login")

        val jwt = JwtSigner.createJwt(user.id)
        val authCookie = ResponseCookie.fromClientResponse("X-Auth", jwt)
                .maxAge(3600)
                .httpOnly(true)
                .path("/")
                .secure(false) // Setear a true si tenemos https
                .build()

        return ResponseEntity.ok().header("Set-Cookie", authCookie.toString()).build<Void>()
    }

    @DeleteMapping("/tokens")
    @ApiOperation(value = "Log Out")
    fun logout(@ApiIgnore @CookieValue("X-Auth") _authCookie : String?): ResponseEntity<Void> {
        JwtSigner.validateJwt(_authCookie.toOption()).getOrHandle { throw it }
        val authCookie = ResponseCookie.fromClientResponse("X-Auth", _authCookie.toString())
                .maxAge(0)
                .httpOnly(true)
                .path("/")
                .secure(false)
                .build()

        return ResponseEntity.ok().header("Set-Cookie", authCookie.toString()).build<Void>()
    }

    @GetMapping
    @ApiOperation(value = "Gets the users without stats")
    fun getUsers(@RequestParam("username", required = false) _username: String?,
                 @ApiIgnore @CookieValue("X-Auth") authCookie : String?): List<UserWithoutStats> {
        JwtSigner.validateJwt(authCookie.toOption()).getOrHandle { throw it }
        return usersService.getUsers(_username)
    }
    // TODO obtener usuarios o un usuario en particular (sin stats)

    @ExceptionHandler(CustomException.NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundError(exception: CustomException) = exception.getJSON()

    @ExceptionHandler(CustomException.BadLoginException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleBadLoginError(exception: CustomException) = exception.getJSON()

    @ExceptionHandler(CustomException.TokenException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleExpiredTokenError(exception: CustomException) = exception.getJSON()
}
