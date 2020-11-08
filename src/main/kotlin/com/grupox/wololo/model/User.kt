package com.grupox.wololo.model

import com.grupox.wololo.model.helpers.DTO
import com.grupox.wololo.model.helpers.Requestable
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "Users")
class User(val username: String, mail: String, private var password: String, val isAdmin: Boolean = false, val stats: Stats = Stats(0,0)) : Requestable {
    @Id
    var id: ObjectId = ObjectId.get()

    var mail: String = mail
        private set

    fun updateGamesWonStats(){
        this.stats.increaseGamesWon()
    }
    fun updateGamesLostStats(){
        this.stats.increaseGamesLost()
    }

    override fun dto(): DTO.UserDTO =
        DTO.UserDTO(
            id = id.toString(),
            username = username,
            stats = stats
        )

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is User) {
            return false
        }
        return id == other.id
    }
}
