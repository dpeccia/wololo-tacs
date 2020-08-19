package com.grupox.wololo.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("")
@Controller // El string retornado es el nombre del template a utilizar. Si uso @RestController retorna el literal.
class ExampleController {
    @GetMapping
    fun home(model: Model): String {
        model["title"] = "Home page"
        model["message"] = "Hello spring boot!"
        return "sample"
    }
}