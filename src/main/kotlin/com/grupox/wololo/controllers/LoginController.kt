package com.grupox.wololo.controllers

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.util.*

@RequestMapping("v1/")
class LoginController {
    @PostMapping(path = arrayOf("id"))
    fun logout(@PathVariable("id") id: UUID) {
        //TODO remover ese id de los activos
    } //dejo por ahora que se le pase el id de quien se desloguea
    @PostMapping(value = ["/login/{id}/"])
    fun login(@PathVariable("id") id:UUID){
        //TODO logica de usuarios logueados
    }
}