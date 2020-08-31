package com.grupox.wololo.model

data class User(val id: Int, var nombre: String, var mail: String, private var password: String, val esAdmin: Boolean) {
    // TODO: MAKE THIS COMPARATION WITH ENCRYPTED PASSWORDS
    fun isUserByLoginData(loginData: LoginModel): Boolean = this.mail == loginData.mail && this.password == loginData.password
}
