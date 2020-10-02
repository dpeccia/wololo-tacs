package com.grupox.wololo.model.repos

import arrow.core.Either
import arrow.core.rightIfNotNull
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.User
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface RepoUsers : MongoRepository<User, String> {
    fun findAllByIsAdminFalse(): List<User>
    fun findByIsAdminFalseAndMail(mail: String): Optional<User>
    fun findByIsAdminFalseAndId(id: ObjectId): Optional<User>
}
