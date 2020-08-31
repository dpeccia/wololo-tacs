package com.grupox.wololo.model

import com.grupox.wololo.model.dtos.DTO
import com.grupox.wololo.model.dtos.Entity
import com.grupox.wololo.model.dtos.UserDTO
import java.util.*

class User(
    val id: Int,
    var nombre: String,
    var mail: String,
    private var password: String,
    val esAdmin: Boolean
) : Entity {
    override fun getDTO(): DTO = UserDTO(id, nombre, mail, esAdmin)

    // TODO: MAKE THIS COMPARATION WITH ENCRYPTED PASSWORDS
    fun isUserByLoginData(loginData: LoginModel): Boolean = this.mail == loginData.mail && this.password == loginData.password
}
