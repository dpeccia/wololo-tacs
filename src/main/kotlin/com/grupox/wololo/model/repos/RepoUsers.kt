package com.grupox.wololo.model.repos

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.toOption
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Stats
import com.grupox.wololo.model.User
import com.grupox.wololo.model.helpers.UserCredentials
import com.grupox.wololo.model.helpers.UserStats
import com.grupox.wololo.model.helpers.UserWithoutStats
import java.util.*

object RepoUsers : Repository<User> {
    private val usersInDB: ArrayList<User> = arrayListOf(
            User(1,"admin", "admin", true, Stats(0, 0)),
            User(2,"unmail@gmail.com", "1234", false, Stats(1, 1)),
            User(3,"otromail@gmail.com", "1234", false, Stats(1, 2))
    )

    override fun getAll(): List<User> = usersInDB

    fun getNormalUsers(): List<User> = getAll().filter { !it.esAdmin }

    // TODO FILTER AND SORT
    fun find(filters: List<String>, orderBys: List<String>): ArrayList<User> = usersInDB

    fun getUserByName(mail: String): Option<User> = getNormalUsers().find { it.mail == mail }.toOption()

    override fun getById(id: Int): Option<User> = getNormalUsers().find { it.id == id }.toOption()

    fun updateUserGamesLost(id: Int){
        getById(id).getOrElse {  throw CustomException.NotFoundException("User was not found")  }.updateGamesLostStats()
    }

    fun updateUserGamesWon(id: Int){
        getById(id).getOrElse {  throw CustomException.NotFoundException("User was not found")  }.updateGamesLostStats()
    }

    fun getUserByLogin(loginData: UserCredentials): User? = getAll().find {it.isUserByLoginData(loginData)}

    fun getUsersWithoutStats(): List<UserWithoutStats> = getNormalUsers().map { it.toUserWithoutStats() }
    // TODO: ENCRYPT USER PASSWORD BEFORE SAVING

    fun getUsersStats(): List<UserStats> = getNormalUsers().map { it.toUserStats() }

    override fun filter(predicate: (obj: User) -> Boolean): List<User> = usersInDB.filter { predicate(it) }

    override fun insert(obj: User) {
        usersInDB.add(obj)
    }
}
