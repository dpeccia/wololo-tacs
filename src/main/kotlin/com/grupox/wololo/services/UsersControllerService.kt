package com.grupox.wololo.services

import com.grupox.wololo.errors.CustomException
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

        val user = User(newUser.username, newUser.mail, sha512.getSHA512(newUser.password))

        RepoUsers.insert(user)
        return user.dto()
    }

    fun checkUserCredentials(user: LoginForm): DTO.UserDTO {
        return RepoUsers.getUserByLogin(user, sha512.getSHA512(user.password)).getOrThrow().dto()
    }

    fun getUsers(_username: String?): List<DTO.UserDTO> {
        val username = _username ?: return RepoUsers.getNormalUsers().map { it.dto() }
        return RepoUsers.getNormalUsers().filter { it.username.startsWith(username) }.map { it.dto() }
    }
}