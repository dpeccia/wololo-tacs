package com.grupox.wololo.model.helpers

import com.fasterxml.jackson.annotation.JsonCreator
import com.grupox.wololo.model.Game

data class UserForm @JsonCreator constructor(val mail: String, val username: String, val password: String)
data class LoginForm @JsonCreator constructor(val mail: String, val password: String)
data class UserPublicInfoWithoutStats @JsonCreator constructor(val id: Int, val mail: String)
data class UserPublicInfo @JsonCreator constructor(val mail: String, val gamesWon: Int, val gamesLost: Int)
data class GamePublicInfo @JsonCreator constructor(val gamesNew: Int, val gamesOnGoing: Int, val gamesFinished: Int, val gamesCanceled: Int, val games: List<Game>)
data class TownInfo @JsonCreator constructor(val gauchosGeneratedByDefense: Int, val gauchosGeneratedByProduction: Int, val image: String)

data class GameForm(val provinceName: String, val townAmount: Int, val participantsIds: List<Int>)
data class MovementForm @JsonCreator constructor(val from: Int, val to: Int, val gauchosQty: Int)
data class AttackForm @JsonCreator constructor(val from: Int, val to: Int)


