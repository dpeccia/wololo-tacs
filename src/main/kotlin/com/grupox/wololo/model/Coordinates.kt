package com.grupox.wololo.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Coordinates(@JsonProperty("lon") val longitude: Float, @JsonProperty("lat") val latitude: Float)