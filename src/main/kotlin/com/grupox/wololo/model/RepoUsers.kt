package com.grupox.wololo.model

import arrow.core.Option
import arrow.core.extensions.option.foldable.find
import arrow.core.extensions.option.foldable.firstOption
import arrow.core.extensions.option.foldable.get
import arrow.core.getOrElse
import arrow.core.toOption
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.helpers.UserCredentials
import com.grupox.wololo.model.helpers.UserStats
import com.grupox.wololo.model.helpers.UserWithoutStats
import java.util.*

object RepoUsers {
    private val usersInDB: ArrayList<User> = arrayListOf(
            User(1,"admin", "admin", true, Stats(0, 0)),
            User(2,"unmail@gmail.com", "1234", false, Stats(1, 1)),
            User(3,"otromail@gmail.com", "1234", false, Stats(1, 2))
    )

    fun getUsers(): List<User> = usersInDB

    fun getNormalUsers(): List<User> = this.getUsers().filter { !it.esAdmin }

    // TODO FILTER AND SORT
    fun find(filters: List<String>, orderBys: List<String>): ArrayList<User> = usersInDB

    fun getUserByName(mail: String): Option<User> = this.getNormalUsers().find { it.mail == mail }.toOption()

    fun getUserIdByMail(mail: String): Int? = this.getNormalUsers().find { it.mail == mail }?.id

    fun getUserById(id: Int): Option<User> = this.getNormalUsers().find { it.id == id }.toOption()

    fun updateUserGamesLost(id: Int){
        this.getUserById(id).getOrElse {  throw CustomException.NotFoundException("User was not found")  }.updateGamesLostStats()
    }

    fun updateUserGamesWon(id: Int){
        this.getUserById(id).getOrElse {  throw CustomException.NotFoundException("User was not found")  }.updateGamesLostStats()
    }

    fun getUserByLogin(loginData: UserCredentials): User? = this.getUsers().find {it.isUserByLoginData(loginData)}

    fun getUsersWithoutStats(): List<UserWithoutStats> = this.getNormalUsers().map { it.toUserWithoutStats() }
    // TODO: ENCRYPT USER PASSWORD BEFORE SAVING

    fun getUsersStats(): List<UserStats> = this.getNormalUsers().map { it.toUserStats() }

    fun insertUser(usuario: User) {
        usersInDB.add(usuario)
    }
}
