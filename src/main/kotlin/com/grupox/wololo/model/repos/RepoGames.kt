package com.grupox.wololo.model.repos

import com.grupox.wololo.model.Game
import com.grupox.wololo.model.User
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface RepoGames : MongoRepository<Game, String> {
    fun findById(id: ObjectId): Optional<Game>
    fun findAllByPlayersContains(user: User): List<Game>
}


