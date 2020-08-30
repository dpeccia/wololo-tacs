package com.grupox.wololo.model

import java.util.*

class Game(private val id: Int, private var status: Status) {
    fun getId(): Int = id
    fun getStatus(): Status = status
}
