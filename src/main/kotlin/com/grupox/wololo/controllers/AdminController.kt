package com.grupox.wololo.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("v1/admin")
class AdminController {
    @GetMapping
    @RequestMapping("/scoreBoard")
    fun getScoreBoard(){
        TODO("Not yet implemented")
    }

}