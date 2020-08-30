package com.grupox.wololo.model

import java.util.*

class Game(id: Int, status: Status) {
    private val id: Int
        get

    private var status: Status
        get

    init {
        this.id = id
        this.status = status
    }

    // fun getId(): Int = id
    // fun getStatus(): Status = status
}
