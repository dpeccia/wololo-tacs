package com.grupox.wololo.controllers

import com.grupox.wololo.model.helpers.DTO
import com.grupox.wololo.model.helpers.JwtSigner
import com.grupox.wololo.model.helpers.LoginForm
import com.grupox.wololo.model.helpers.UserForm
import com.grupox.wololo.services.UsersControllerService
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.WebUtils
import javax.servlet.http.HttpServletRequest

@RequestMapping("/users")
@RestController
class UsersController : BaseController() {
    @Autowired
    lateinit var usersControllerService: UsersControllerService

    @PostMapping
    @ApiOperation(value = "Creates a new user (Sign Up / Register)")
    fun createUser(@RequestBody newUser: UserForm): DTO.UserDTO {
        return usersControllerService.createUser(newUser)
    }

    @PostMapping("/tokens")
    @ApiOperation(value = "Log In")
    fun login(@RequestBody _user: LoginForm): ResponseEntity<DTO.UserDTO> {
        val userDTO = usersControllerService.checkUserCredentials(_user)

        val jwt = JwtSigner.createJwt(userDTO.id)
        val authCookie = ResponseCookie.fromClientResponse("X-Auth", jwt)
                .maxAge(3600)
                .httpOnly(true)
                .path("/")
                .secure(false) // Setear a true si tenemos https
                .build()

        return ResponseEntity.ok().header("Set-Cookie", authCookie.toString()).body(userDTO)
    }

    @DeleteMapping("/tokens")
    @ApiOperation(value = "Log Out")
    fun logout(request: HttpServletRequest): ResponseEntity<Void> {
        checkAndGetToken(request)
        val authCookie = ResponseCookie.fromClientResponse("X-Auth", WebUtils.getCookie(request, "X-Auth")!!.value)
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
                 request: HttpServletRequest): List<DTO.UserDTO> {
        checkAndGetToken(request)
        return usersControllerService.getUsers(_username)
    }
}
