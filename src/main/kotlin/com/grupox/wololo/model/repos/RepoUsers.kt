package com.grupox.wololo.model.repos

import com.grupox.wololo.model.User
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface RepoUsers : MongoRepository<User, String> {
    fun findAllByIsAdminFalse(): List<User>                                     // gets all users that aren't admin
    fun findAllByIsAdminFalseAndUsernameLike(username: String): List<User>      // gets all normal users that match
    fun findByIsAdminFalseAndMail(mail: String): Optional<User>                 // gets a user by mail only if it isn't admin
    fun findByIsAdminFalseAndId(id: ObjectId): Optional<User>                   // gets a user by id only if it isn´t admin
    fun findByIsAdminTrueAndId(id: ObjectId): Optional<User>                    // gets an admin by id
    fun findByMailAndPassword(mail: String, password: String): Optional<User>   // gets a user by mail and password
}
