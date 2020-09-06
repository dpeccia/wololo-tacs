package com.grupox.wololo.model

import java.util.*

object RepoUsers {
    private val usersInDB: ArrayList<User> = arrayListOf(
            User(1, "Lorem", "Ipsum", "sit", true, Stats(0, 0)),
            User(2, "maxi", "unmail@gmail.com", "1234", false, Stats(1, 1))
    )

    fun getUsers(): ArrayList<User> = usersInDB

    // TODO FILTER AND SORT
    fun find(filters: List<String>, orderBys: List<String>): ArrayList<User> = usersInDB

    fun getUserById(id: Int): User? = usersInDB.find { it.id == id }

    fun getUserByLogin(loginData: LoginModel): User? = usersInDB.find {it.isUserByLoginData(loginData)}

    // TODO: ENCRYPT USER PASSWORD BEFORE SAVING
    fun insertUser(usuario: User) {
        usersInDB.add(usuario)
    }
}
