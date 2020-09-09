package com.grupox.wololo.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class TownGeoRef(
        @JsonProperty("id") val id: Int,
        @JsonProperty("nombre") val name: String,
        @JsonProperty("centroide") val coordinates: Coordinates
)

data class Town(
        val id: Int,
        val name: String,
        val coordinates: Coordinates = Coordinates(0f,0f), val specialization : Specialization, val stats : TownStats)

