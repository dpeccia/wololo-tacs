package com.grupox.wololo.model

import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.toOption
import com.grupox.wololo.model.helpers.UserCredentials
import java.util.*

object RepoUsers {
    private val usersInDB: ArrayList<User> = arrayListOf(
            User("admin", "admin", true, Stats(0, 0)),
            User("unmail@gmail.com", "1234", false, Stats(1, 1))
    )

    fun getUsers(): ArrayList<User> = usersInDB

    // TODO FILTER AND SORT
    fun find(filters: List<String>, orderBys: List<String>): ArrayList<User> = usersInDB

    fun getUserByName(mail: String): Option<User> = usersInDB.find { it.mail == mail }.toOption()

    fun getUserByLogin(loginData: UserCredentials): User? = usersInDB.find {it.isUserByLoginData(loginData)}

    // TODO: ENCRYPT USER PASSWORD BEFORE SAVING
    fun insertUser(usuario: User) {
        usersInDB.add(usuario)
    }
}
