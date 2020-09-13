package com.grupox.wololo.model

import com.grupox.wololo.model.helpers.UserCredentials
import com.grupox.wololo.model.helpers.UserStats
import com.grupox.wololo.model.helpers.UserWithoutStats

class User(val id: Int, mail: String, private var password: String, val esAdmin: Boolean, val stats: Stats) {
    var mail: String = mail
        private set

    // TODO: MAKE THIS COMPARATION WITH ENCRYPTED PASSWORDS
    fun isUserByLoginData(loginData: UserCredentials): Boolean = this.mail == loginData.mail && this.password == loginData.password

    fun toUserWithoutStats(): UserWithoutStats = UserWithoutStats(this.id, this.mail)


    fun toUserStats(): UserStats = UserStats(this.mail,this.stats.gamesWon,this.stats.gamesLost)

    fun updateGamesWonStats(){
        this.stats.increaseGamesWon()
    }
    fun updateGamesLostStats(){
        this.stats.increaseGamesLost()
    }

}
