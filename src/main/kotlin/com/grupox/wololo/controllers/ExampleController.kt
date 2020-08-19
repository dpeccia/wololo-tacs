package com.grupox.wololo.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("")
@RestController
class ExampleController {
    @GetMapping
    fun home(): String {
        return "Hello spring boot!!!"
    }
}