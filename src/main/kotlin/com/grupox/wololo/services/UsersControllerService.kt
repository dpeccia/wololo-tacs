package com.grupox.wololo.services

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Stats
import com.grupox.wololo.model.User
import com.grupox.wololo.model.helpers.*
import com.grupox.wololo.model.repos.RepoUsers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UsersControllerService {

    @Autowired
    private lateinit var sha512: SHA512Hash

    fun createUser(newUser: UserForm) : DTO.UserDTO {
        if(RepoUsers.getUserByName(newUser.mail).isRight())
            throw CustomException.BadRequest.IllegalUserException("There is already an user under that email")

        val user = User(3, newUser.username, newUser.mail, sha512.getSHA512(newUser.password), false, Stats(0,0)) // TODO el id se tiene que autoincrementar

        RepoUsers.insert(user)
        return user.dto()
    }

    fun checkUserCredentials(user: LoginForm): DTO.UserDTO {
        return RepoUsers.getUserByLogin(user, sha512.getSHA512(user.password)).getOrThrow().dto()
    }

    fun getUsers(_username: String?): List<DTO.UserDTO> {
        val username = _username ?: return RepoUsers.filter { !it.isAdmin }.map { it.dto() }
        return RepoUsers.filter { !it.isAdmin && it.username.startsWith(username) }.map { it.dto() }

    }
}