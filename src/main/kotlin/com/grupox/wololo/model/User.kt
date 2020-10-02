package com.grupox.wololo.model


import com.grupox.wololo.model.helpers.DTO
import com.grupox.wololo.model.helpers.LoginForm
import com.grupox.wololo.model.helpers.Requestable
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.concurrent.atomic.AtomicInteger

@Document
class User(val username: String, mail: String, private var password: String, val isAdmin: Boolean = false, val stats: Stats = Stats(0,0)) : Requestable {
    @Id
    var id: ObjectId = ObjectId.get()

    var mail: String = mail
        private set

    // TODO: MAKE THIS COMPARATION WITH ENCRYPTED PASSWORDS
    fun isUserByLoginData(loginData: LoginForm, hashedPassword: String): Boolean = this.mail == loginData.mail && this.password == hashedPassword

    fun updateGamesWonStats(){
        this.stats.increaseGamesWon()
    }
    fun updateGamesLostStats(){
        this.stats.increaseGamesLost()
    }

    override fun dto(): DTO.UserDTO =
        DTO.UserDTO(
            id = id,
            username = username,
            stats = stats
        )
}
