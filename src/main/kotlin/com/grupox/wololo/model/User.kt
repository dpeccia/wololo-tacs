package com.grupox.wololo.model


import com.grupox.wololo.model.helpers.*
import java.util.*

class User(val username: String, mail: String, private var password: String, val isAdmin: Boolean = false, val stats: Stats = Stats(0,0)) : Requestable {
    val id: UUID = UUID.randomUUID()
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
