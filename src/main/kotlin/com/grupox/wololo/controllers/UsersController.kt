package com.grupox.wololo.controllers

import com.grupox.wololo.model.RepoUsers
import com.grupox.wololo.model.User
import org.springframework.web.bind.annotation.*
import java.util.*

@RequestMapping("v1/users")
@RestController
class UsersController {
    @GetMapping(path = arrayOf("id"))
    fun getUser(@PathVariable("id") id: UUID): User{
        return RepoUsers.getUser(id)
                .orElse(null)
    }

    @PostMapping
    fun createUser(@RequestBody user: User){ //que agarre el request body del request
//        TODO cambiar que no reciba todo el usuario por un json
        RepoUsers.createUser(user)
    }
}