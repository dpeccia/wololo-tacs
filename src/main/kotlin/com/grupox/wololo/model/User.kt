package com.grupox.wololo.model

class User(val id: Int, nombre: String, mail: String, private var password: String, val esAdmin: Boolean, val stats: Stats) {
    var nombre: String = nombre
        private set

    var mail: String = mail
        private set

    // TODO: MAKE THIS COMPARATION WITH ENCRYPTED PASSWORDS
    fun isUserByLoginData(loginData: LoginModel): Boolean = this.mail == loginData.mail && this.password == loginData.password
}
