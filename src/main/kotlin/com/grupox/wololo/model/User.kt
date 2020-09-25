package com.grupox.wololo.model

import com.grupox.wololo.model.helpers.*

class User(val id: Int, val username: String, mail: String, private var password: String, val esAdmin: Boolean, val stats: Stats = Stats(0,0)) : Requestable {
    var mail: String = mail
        private set

    // TODO: MAKE THIS COMPARATION WITH ENCRYPTED PASSWORDS
    fun isUserByLoginData(loginData: LoginForm, hashedPassword: String): Boolean = this.mail == loginData.mail && this.password == hashedPassword

    fun changePassword(newPass: String) {
        this.password = newPass
    }

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
