package com.grupox.wololo.services

import arrow.core.getOrHandle
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Stats
import com.grupox.wololo.model.User
import com.grupox.wololo.model.helpers.UserForm
import com.grupox.wololo.model.repos.RepoUsers
import com.grupox.wololo.model.helpers.UserPublicInfoWithoutStats
import org.springframework.stereotype.Service

@Service
class UsersControllerService {
    fun createUser(newUser: UserForm) {
        if(RepoUsers.getUserByName(newUser.mail).isRight())
            throw CustomException.BadRequest.IllegalUserException("There is already an user under that email")
        val user = User(3, newUser.mail, newUser.password, false, Stats(0,0)) // TODO el id se tiene que autoincrementar
        RepoUsers.insert(user)
    }

    fun checkUserCredentials(user: UserForm): User =
            RepoUsers.getUserByLogin(user).getOrHandle { throw it }

    fun getUsers(_username: String?): List<UserPublicInfoWithoutStats> {
        val username = _username ?: return RepoUsers.getUsersWithoutStats()
        val user = ArrayList<UserPublicInfoWithoutStats>()
        user.add(RepoUsers.getUserByName(username).getOrHandle { throw it }.publicInfoWithoutStats())
        return user
    }
}