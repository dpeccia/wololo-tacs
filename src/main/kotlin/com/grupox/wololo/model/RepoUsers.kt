package com.grupox.wololo.model

import arrow.core.Option
import arrow.core.toOption
import com.grupox.wololo.model.helpers.UserCredentials
import com.grupox.wololo.model.helpers.UserWithoutStats
import java.util.*

object RepoUsers {
    private val usersInDB: ArrayList<User> = arrayListOf(
            User(1,"admin", "admin", true, Stats(0, 0)),
            User(2,"unmail@gmail.com", "1234", false, Stats(1, 1))
    )

    fun getUsers(): List<User> = usersInDB

    fun getNormalUsers(): List<User> = this.getUsers().filter { !it.esAdmin }

    // TODO FILTER AND SORT
    fun find(filters: List<String>, orderBys: List<String>): ArrayList<User> = usersInDB

    fun getUserByName(mail: String): Option<User> = this.getNormalUsers().find { it.mail == mail }.toOption()

    fun getUserById(id: Int): Option<User> = this.getNormalUsers().find { it.id == id }.toOption()

    fun getUserByLogin(loginData: UserCredentials): User? = this.getUsers().find {it.isUserByLoginData(loginData)}

    fun getUsersWithoutStats(): List<UserWithoutStats> = this.getNormalUsers().map { it.toUserWithoutStats() }
    // TODO: ENCRYPT USER PASSWORD BEFORE SAVING
    fun insertUser(usuario: User) {
        usersInDB.add(usuario)
    }
}
