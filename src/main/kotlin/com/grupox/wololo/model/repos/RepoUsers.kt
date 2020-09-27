package com.grupox.wololo.model.repos

import arrow.core.Either
import arrow.core.rightIfNotNull
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Stats
import com.grupox.wololo.model.User
import com.grupox.wololo.model.helpers.LoginForm
import java.util.*

object RepoUsers : Repository<User> {
    private val usersInDB: ArrayList<User> = arrayListOf(
            User("admin", "admin", "44d13d15f9c969364437fe776b41d9a330952a17d644d652173355dba2db130bf723af585c02987cf614c583b579868337fb52c5eaafc59b3e4e5a277784f92f", isAdmin = true), //example_admin
            User("unuser", "unmail@gmail.com", "8939293fdbf58224d24b40f364771de933ea13d82da067f0b755c35462a24d244b0b5b1cac7389f1e20eb26e40891f0b4c5e4804450f0b7c9f17569504695649", stats = Stats(1, 1)), //1234
            User("otrouser", "otromail@gmail.com", "8939293fdbf58224d24b40f364771de933ea13d82da067f0b755c35462a24d244b0b5b1cac7389f1e20eb26e40891f0b4c5e4804450f0b7c9f17569504695649", stats = Stats(1, 2)),
            User("user", "mail", "8939293fdbf58224d24b40f364771de933ea13d82da067f0b755c35462a24d244b0b5b1cac7389f1e20eb26e40891f0b4c5e4804450f0b7c9f17569504695649", stats = Stats(1,1)),
            User("user2", "mail2", "8939293fdbf58224d24b40f364771de933ea13d82da067f0b755c35462a24d244b0b5b1cac7389f1e20eb26e40891f0b4c5e4804450f0b7c9f17569504695649")
    )

    fun getNormalUsers(): List<User> = getAll().filter { !it.isAdmin }

    // TODO FILTER AND SORT
    fun find(filters: List<String>, orderBys: List<String>): ArrayList<User> = usersInDB

    fun getUserByName(mail: String): Either<CustomException.NotFound, User> = getNormalUsers().find { it.mail == mail }.rightIfNotNull { CustomException.NotFound.UserNotFoundException() }

    fun getUserByLogin(loginData: LoginForm, hashedPassword: String): Either<CustomException.Unauthorized, User> = getAll().find {it.isUserByLoginData(loginData, hashedPassword)}.rightIfNotNull { CustomException.Unauthorized.BadLoginException() }

    fun getAdminById(id: Int): Either<CustomException.Forbidden, User> =  getAll().find { it.id == id && it.isAdmin}.rightIfNotNull { CustomException.Forbidden.OperationNotAuthorized() }

    override fun getAll(): List<User> = usersInDB

    override fun getById(id: Int): Either<CustomException.NotFound, User> = getNormalUsers().find { it.id == id }.rightIfNotNull { CustomException.NotFound.UserNotFoundException() }

    override fun filter(predicate: (obj: User) -> Boolean): List<User> = getAll().filter { predicate(it) }

    override fun insert(obj: User) {
        usersInDB.add(obj)
    }
}
