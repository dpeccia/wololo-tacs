package com.grupox.wololo.model.helpers

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.grupox.wololo.model.Coordinates
import com.grupox.wololo.model.Game

data class UserCredentials @JsonCreator constructor(val mail: String, val password: String)
data class UserWithoutStats @JsonCreator constructor(val id: Int, val mail: String)
data class UserStats @JsonCreator constructor(val mail: String, val gamesWon: Int, val gamesLost: Int)
data class GameStats @JsonCreator constructor(val gamesNew: Int, val gamesOnGoing: Int, val gamesFinished: Int, val gamesCanceled: Int, val games: List<Game>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProvinceGeoRef(@JsonProperty("nombre") val name: String)
data class GameForm(val provinceName: String, val townAmount: Int, val participantsIds: List<Int>)
data class TownForm @JsonCreator constructor(val specialization: String)


@JsonIgnoreProperties(ignoreUnknown = true)
data class TownGeoRef(
        @JsonProperty("id") val id: Int,
        @JsonProperty("nombre") val name: String,
        @JsonProperty("centroide") val coordinates: Coordinates
)