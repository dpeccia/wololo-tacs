package com.grupox.wololo.model

import java.util.*

class Game(private val id: Int, private val status: Status) {
    fun getId(): Int = id
    fun getStatus(): Status = status
}
