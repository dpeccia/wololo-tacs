package com.grupox.wololo.model.helpers

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.grupox.wololo.model.Coordinates
import com.grupox.wololo.model.Game

data class UserForm @JsonCreator constructor(val mail: String, val username: String, val password: String)
data class LoginForm @JsonCreator constructor(val mail: String, val password: String)
data class UserPublicInfoWithoutStats @JsonCreator constructor(val id: Int, val mail: String)
data class UserPublicInfo @JsonCreator constructor(val mail: String, val gamesWon: Int, val gamesLost: Int)
data class GamePublicInfo @JsonCreator constructor(val gamesNew: Int, val gamesOnGoing: Int, val gamesFinished: Int, val gamesCanceled: Int, val games: List<Game>)

data class GameForm(val provinceName: String, val townAmount: Int, val participantsIds: List<Int>)
data class TownForm @JsonCreator constructor(val specialization: String)
data class MovementForm @JsonCreator constructor(val from: Int, val to: Int, val gauchosQty: Int)
data class AttackForm @JsonCreator constructor(val from: Int, val to: Int)
data class TownInfo @JsonCreator constructor(val gauchosGeneratedByDefense: Int, val gauchosGeneratedByProduction: Int)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProvinceGeoRef(@JsonProperty("nombre") val name: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TownGeoRef(
        @JsonProperty("id") val id: Int,
        @JsonProperty("nombre") val name: String,
        @JsonProperty("centroide") val coordinates: Coordinates
)