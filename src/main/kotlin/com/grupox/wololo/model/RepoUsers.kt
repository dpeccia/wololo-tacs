package com.grupox.wololo.model

import java.util.*

object RepoUsers {
    private val DB: ArrayList<User> = arrayListOf()
    fun getUser(id: UUID): Optional<User> {
        return Optional.of(DB.first { it.id == id })
    }

    fun createUser(usuario: User) {
        DB.add(usuario)
    }


}
