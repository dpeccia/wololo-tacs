package com.grupox.wololo.model.helpers

import com.fasterxml.jackson.annotation.JsonCreator
import org.bson.types.ObjectId

data class UserForm @JsonCreator constructor(val mail: String, val username: String, val password: String)
data class LoginForm @JsonCreator constructor(val mail: String, val password: String)
data class GameForm(val provinceName: String, val townAmount: Int, val participantsIds: List<String>, val difficulty: String)
data class MovementForm @JsonCreator constructor(val from: Int, val to: Int, val gauchosQty: Int)
data class AttackForm @JsonCreator constructor(val from: Int, val to: Int)

data class GamePublicInfo @JsonCreator constructor(val gamesNew: Int, val gamesOnGoing: Int, val gamesFinished: Int, val gamesCanceled: Int)
data class GameMode(val description: String, val multGauchosForDefense: Double, val multGauchosForProduction: Double, val multDefenseForDefense: Double, val multDefenseForProduction: Double)



