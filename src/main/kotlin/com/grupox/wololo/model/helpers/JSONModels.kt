package com.grupox.wololo.model.helpers

import com.fasterxml.jackson.annotation.JsonCreator
import java.util.*

data class UserForm @JsonCreator constructor(val mail: String, val username: String, val password: String)
data class LoginForm @JsonCreator constructor(val mail: String, val password: String)
data class GameForm(val provinceName: String, val townAmount: Int, val participantsIds: List<UUID>)
data class MovementForm @JsonCreator constructor(val from: UUID, val to: UUID, val gauchosQty: Int)
data class AttackForm @JsonCreator constructor(val from: UUID, val to: UUID)

data class GamePublicInfo @JsonCreator constructor(val gamesNew: Int, val gamesOnGoing: Int, val gamesFinished: Int, val gamesCanceled: Int)



