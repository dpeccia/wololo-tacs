package com.grupox.wololo.model

import java.util.*

object RepoUsers {
    private val usersInDB: ArrayList<User> = arrayListOf(
            User(1, "Lorem", "Ipsum", "sit", true),
            User(2, "maxi", "unmail@gmail.com", "1234", false)
    )

    fun getUsers(): ArrayList<User> = usersInDB

    fun getUser(id: Int): User? = usersInDB.find { it.id == id }

    fun createUser(usuario: User) {
        usersInDB.add(usuario)
    }


}
