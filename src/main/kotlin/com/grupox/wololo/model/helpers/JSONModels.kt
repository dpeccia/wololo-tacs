package com.grupox.wololo.model.helpers

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.grupox.wololo.model.Coordinates

data class UserCredentials @JsonCreator constructor(val mail: String, val password: String)
data class UserWithoutStats @JsonCreator constructor(val id: Int, val mail: String)
data class GameData @JsonCreator constructor(val id: Int, val status: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProvinceGeoRef(@JsonProperty("nombre") val name: String)

data class GameForm(val provinceName: String, val townAmount: Int, val participantsIds: List<Int>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TownGeoRef(
        @JsonProperty("id") val id: Int,
        @JsonProperty("nombre") val name: String,
        @JsonProperty("centroide") val coordinates: Coordinates
)