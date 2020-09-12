package com.grupox.wololo.services

import arrow.core.getOrElse
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.RepoUsers
import com.grupox.wololo.model.helpers.UserWithoutStats
import org.springframework.stereotype.Service

@Service
class UserService {
    fun getUsers(_username: String?): List<UserWithoutStats> {
        val username = _username ?: return RepoUsers.getUsersWithoutStats()
        val user = ArrayList<UserWithoutStats>()
        user.add(RepoUsers.getUserByName(username)
                .getOrElse { throw CustomException.NotFoundException("No user with such name") }.toUserWithoutStats())
        return user
    }
}