package com.grupox.wololo.controllers

import com.grupox.wololo.model.User
import com.grupox.wololo.model.helpers.JwtSigner
import com.grupox.wololo.model.helpers.LoginForm
import com.grupox.wololo.model.helpers.UserForm
import com.grupox.wololo.model.helpers.UserPublicInfoWithoutStats
import com.grupox.wololo.services.UsersControllerService
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore

@RequestMapping("/users")
@RestController
class UsersController : BaseController() {
    @Autowired
    lateinit var usersControllerService: UsersControllerService

    @PostMapping
    @ApiOperation(value = "Creates a new user (Sign Up / Register)")
    fun createUser(@RequestBody newUser: UserForm): User {
        return usersControllerService.createUser(newUser)
    }

    @PostMapping("/tokens")
    @ApiOperation(value = "Log In")
    fun login(@RequestBody _user: LoginForm): ResponseEntity<Void> {
        val user = usersControllerService.checkUserCredentials(_user)

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
        checkAndGetToken(_authCookie)
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
                 @ApiIgnore @CookieValue("X-Auth") authCookie : String?): List<UserPublicInfoWithoutStats> {
        checkAndGetToken(authCookie)
        return usersControllerService.getUsers(_username)
    }
}
