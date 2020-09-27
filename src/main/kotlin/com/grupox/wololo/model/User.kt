package com.grupox.wololo.model


import com.grupox.wololo.model.helpers.DTO
import com.grupox.wololo.model.helpers.LoginForm
import com.grupox.wololo.model.helpers.Requestable
import java.util.concurrent.atomic.AtomicInteger

class User(val username: String, mail: String, private var password: String, val isAdmin: Boolean = false, val stats: Stats = Stats(0,0)) : Requestable {
    val id: Int = generateId()
    var mail: String = mail
        private set

    companion object {
        private val idGenerator: AtomicInteger = AtomicInteger(1000)
        fun generateId(): Int = idGenerator.incrementAndGet()
    }

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
