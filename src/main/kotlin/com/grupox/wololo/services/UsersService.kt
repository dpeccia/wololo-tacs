package com.grupox.wololo.services

import arrow.core.getOrElse
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Stats
import com.grupox.wololo.model.User
import com.grupox.wololo.model.helpers.UserCredentials
import com.grupox.wololo.model.repos.RepoUsers
import com.grupox.wololo.model.helpers.UserWithoutStats
import org.springframework.stereotype.Service

@Service
class UsersService {
    fun createUser(newUser: UserCredentials) {
        if(RepoUsers.getUserByName(newUser.mail).nonEmpty())
            throw CustomException.BadRequest.IllegalUserException("User already exists")
        val user = User(3, newUser.mail, newUser.password, false, Stats(0,0)) // TODO el id se tiene que autoincrementar
        RepoUsers.insert(user)
    }

    fun checkUserCredentials(user: UserCredentials): User =
            RepoUsers.getUserByLogin(user) ?: throw CustomException.Unauthorized.BadLoginException("Bad Login")

    fun getUsers(_username: String?): List<UserWithoutStats> {
        val username = _username ?: return RepoUsers.getUsersWithoutStats()
        val user = ArrayList<UserWithoutStats>()
        user.add(RepoUsers.getUserByName(username)
                .getOrElse { throw CustomException.NotFound.UserNotFoundException() }.toUserWithoutStats())
        return user
    }
}