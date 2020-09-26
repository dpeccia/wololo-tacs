package com.grupox.wololo.model.repos

import arrow.core.Either
import arrow.core.rightIfNotNull
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Stats
import com.grupox.wololo.model.User
import com.grupox.wololo.model.helpers.LoginForm
import com.grupox.wololo.model.helpers.UserForm
import com.grupox.wololo.model.helpers.UserPublicInfo
import com.grupox.wololo.model.helpers.UserPublicInfoWithoutStats
import java.util.*

object RepoUsers : Repository<User> {
    private val usersInDB: ArrayList<User> = arrayListOf(
            User(1, "", "admin", "admin", true, Stats(0, 0)),
            User(2, "", "unmail@gmail.com", "1234", false, Stats(1, 1)),
            User(3, "", "otromail@gmail.com", "1234", false, Stats(1, 2)),
            User(4, "", "mail", "password", false, Stats(1,1)),
            User(5, "", "mail2", "password2", false, Stats(0,0))
    )

    fun getNormalUsers(): List<User> = getAll().filter { !it.isAdmin }

    // TODO FILTER AND SORT
    fun find(filters: List<String>, orderBys: List<String>): ArrayList<User> = usersInDB

    fun getUserByName(mail: String): Either<CustomException.NotFound, User> = getNormalUsers().find { it.mail == mail }.rightIfNotNull { CustomException.NotFound.UserNotFoundException() }

    fun getUserByLogin(loginData: LoginForm, hashedPassword: String): Either<CustomException.Unauthorized, User> = getAll().find {it.isUserByLoginData(loginData, hashedPassword)}.rightIfNotNull { CustomException.Unauthorized.BadLoginException() }

    fun getAdminById(id: Int): Either<CustomException.Unauthorized, User> = getAll().find { it.id == id && it.isAdmin}.rightIfNotNull { CustomException.Unauthorized.OperationNotAuthorized() }

    fun getUsersWithoutStats(): List<UserPublicInfoWithoutStats> = getNormalUsers().map { it.publicInfoWithoutStats() }
    // TODO: ENCRYPT USER PASSWORD BEFORE SAVING

    fun getUsersStats(): List<UserPublicInfo> = getNormalUsers().map { it.publicInfo() }

    override fun getAll(): List<User> = usersInDB

    override fun getById(id: Int): Either<CustomException.NotFound, User> = getNormalUsers().find { it.id == id }.rightIfNotNull { CustomException.NotFound.UserNotFoundException() }

    override fun filter(predicate: (obj: User) -> Boolean): List<User> = usersInDB.filter { predicate(it) }

    override fun insert(obj: User) {
        usersInDB.add(obj)
    }
}
