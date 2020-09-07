package com.grupox.wololo.model

import com.grupox.wololo.model.helpers.UserCredentials

class User(mail: String, private var password: String, val esAdmin: Boolean, val stats: Stats) {
    var mail: String = mail
        private set

    // TODO: MAKE THIS COMPARATION WITH ENCRYPTED PASSWORDS
    fun isUserByLoginData(loginData: UserCredentials): Boolean = this.mail == loginData.mail && this.password == loginData.password
}
