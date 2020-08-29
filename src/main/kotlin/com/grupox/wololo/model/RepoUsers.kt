package com.grupox.wololo.model

import java.util.*

object RepoUsers {
    private val DB: ArrayList<User> = arrayListOf(
            User(1, "Lorem", "Ipsum", "sit", true),
            User(2, "maxi", "unmail@gmail.com", "1234", false)
    )

    fun getUsers(): ArrayList<User> = DB

    fun getUser(id: Int): User? = DB.find { it.id == id }

    fun createUser(usuario: User) {
        DB.add(usuario)
    }


}
