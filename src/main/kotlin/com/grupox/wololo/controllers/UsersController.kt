package com.grupox.wololo.controllers

import arrow.core.getOrElse
import arrow.core.getOrHandle
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.JwtSigner
import com.grupox.wololo.model.helpers.UserCredentials
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/users")
@RestController
class UsersController {
    @PostMapping
    @ApiOperation(value = "Creates a new user (Sign Up / Register)")
    fun createUser(@RequestBody newUser: UserCredentials) {
        if(RepoUsers.getUserByName(newUser.mail).nonEmpty())
            throw CustomException.NotFoundException("User already exists")
        val user = User(newUser.mail, newUser.password, false, Stats(0,0))
        RepoUsers.insertUser(user)
    }

    @PostMapping("/tokens")
    @ApiOperation(value = "Log In")
    fun login(@RequestBody _user: UserCredentials): ResponseEntity<Void> {
        val user = RepoUsers.getUserByLogin(_user) ?: throw CustomException.NotFoundException("Bad Login")

        val jwt = JwtSigner.createJwt(user.mail)
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
    fun logout(@CookieValue("X-Auth") _authCookie : String?): ResponseEntity<Void> {
        JwtSigner.validateJwt(_authCookie.toString()).getOrHandle { throw it }
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
                 @CookieValue("X-Auth") authCookie : String?): List<User> {
        JwtSigner.validateJwt(authCookie.toString()).getOrHandle { throw it }
        val username = _username ?: return RepoUsers.getUsers()
        val user = ArrayList<User>()
        user.add(RepoUsers.getUserByName(username).getOrElse { throw CustomException.NotFoundException("No user with such name") })
        return user
    }
    // TODO obtener usuarios o un usuario en particular (sin stats)

    @ExceptionHandler(CustomException.NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundError(exception: CustomException) = exception.getJSON()

    @ExceptionHandler(CustomException.ExpiredTokenException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleExpiredTokenError(exception: CustomException) = exception.getJSON()
}
