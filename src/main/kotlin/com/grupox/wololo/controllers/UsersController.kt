package com.grupox.wololo.controllers

import com.grupox.wololo.model.helpers.DTO
import com.grupox.wololo.model.helpers.LoginForm
import com.grupox.wololo.model.helpers.UserForm
import com.grupox.wololo.services.UsersControllerService
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RequestMapping("/users")
@RestController
class UsersController : BaseController() {
    @Autowired
    lateinit var usersControllerService: UsersControllerService

    @PostMapping
    @ApiOperation(value = "Creates a new user (Sign Up / Register)")
    fun createUser(@RequestBody newUser: UserForm): ResponseEntity<DTO.UserDTO> {
        val userDTO : DTO.UserDTO = usersControllerService.createUser(newUser)
        return usersControllerService.login(userDTO)
    }

    @PostMapping("/tokens")
    @ApiOperation(value = "Log In")
    fun login(@RequestBody _user: LoginForm): ResponseEntity<DTO.UserDTO> {
        val userDTO = usersControllerService.checkUserCredentials(_user)
        return usersControllerService.login(userDTO)
    }

    @DeleteMapping("/tokens")
    @ApiOperation(value = "Log Out")
    fun logout(request: HttpServletRequest): ResponseEntity<Void> {
        checkAndGetUserId(request)
        return usersControllerService.logout(request)
    }

    @GetMapping
    @ApiOperation(value = "Gets the users without stats")
    fun getUsers(
            @RequestParam("username", required = false) _username: String?,
            request: HttpServletRequest): List<DTO.UserDTO> {
        checkAndGetUserId(request)
        return usersControllerService.getUsers(_username)
    }

    @GetMapping("/scoreboard")
    @ApiOperation(value = "Gets a top 10 ranking of users ordered by games won")
    fun getScoreboard(
            @RequestParam("sort", required = false) sort: String?,
            request: HttpServletRequest): List<DTO.UserDTO> {
        val userId = checkAndGetUserId(request)
        usersControllerService.throwIfNotAllowed(userId)
        return usersControllerService.getScoreboard(sort)
    }
}