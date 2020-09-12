package com.grupox.wololo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
class WololoApplication

fun main(args: Array<String>) {
    runApplication<WololoApplication>(*args)
}
