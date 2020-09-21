package com.grupox.wololo.model

import com.grupox.wololo.model.helpers.LoginForm
import com.grupox.wololo.model.helpers.UserForm
import com.grupox.wololo.model.helpers.UserPublicInfo
import com.grupox.wololo.model.helpers.UserPublicInfoWithoutStats

class User(val id: Int, val username: String, mail: String, private var password: String, val esAdmin: Boolean, val stats: Stats = Stats(0,0)) {
    var mail: String = mail
        private set

    // TODO: MAKE THIS COMPARATION WITH ENCRYPTED PASSWORDS
    fun isUserByLoginData(loginData: LoginForm, hashedPassword: String): Boolean = this.mail == loginData.mail && this.password == hashedPassword

    fun publicInfoWithoutStats(): UserPublicInfoWithoutStats = UserPublicInfoWithoutStats(this.id, this.mail)

    fun changePassword(newPass: String) {
        this.password = newPass
    }

    fun publicInfo(): UserPublicInfo = UserPublicInfo(this.mail,this.stats.gamesWon,this.stats.gamesLost)

    fun updateGamesWonStats(){
        this.stats.increaseGamesWon()
    }
    fun updateGamesLostStats(){
        this.stats.increaseGamesLost()
    }

}
