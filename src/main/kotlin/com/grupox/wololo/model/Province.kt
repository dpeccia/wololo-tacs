package com.grupox.wololo.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProvinceGeoRef(@JsonProperty("nombre") val name: String)

class Province(val id: Int, val name: String, val coordinates: Coordinates = Coordinates(0f, 0f), val towns: ArrayList<Town>) {
    fun getTownById(id: Int): Town? = towns.find { it.id == id }
}
