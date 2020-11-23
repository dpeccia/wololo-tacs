package com.grupox.wololo.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.grupox.wololo.model.externalservices.LocationData

data class Coordinates(@JsonProperty("lon") val longitude: Float, @JsonProperty("lat") val latitude: Float) {
    fun isEqualTo(location: LocationData): Boolean = longitude == location.lng && latitude == location.lat
}