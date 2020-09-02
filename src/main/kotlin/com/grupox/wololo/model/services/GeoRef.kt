package com.grupox.wololo.model.services

import arrow.core.*
import arrow.core.Option
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.grupox.wololo.model.Coordinates
import com.grupox.wololo.model.Province
import com.grupox.wololo.model.Town
import khttp.get
import khttp.responses.Response
import org.json.JSONObject

sealed class QueryData {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ProvinceData(@JsonProperty("cantidad") val numberOfMatches: Int, @JsonProperty("provincias") val matches: List<LocationData>) : QueryData()

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TownData(@JsonProperty("cantidad") val numberOfMatches: Int, @JsonProperty("municipios") val matches: List<LocationData>) : QueryData()
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class LocationData (
        @JsonProperty("centroide") val coordinates: Coordinates,
        @JsonProperty("id") val id: Int,
        @JsonProperty("nombre") val name: String
)

object GeoRef {
    private val provinceDataUrl = "https://apis.datos.gob.ar/georef/api/provincias"
    private val townsDataUrl = "https://apis.datos.gob.ar/georef/api/municipios"

    fun httpGetProvinceData(name: String): Option<LocationData> {
        val queryData: Option<QueryData.ProvinceData> =
                httpGetQueryData(provinceDataUrl, mapOf("nombre" to name), "provincias")

        return queryData.filter { it.numberOfMatches == 1 }.map { it.matches[0] }
    }

    fun httpGetTownsData(provinceId: Int): List<LocationData> {
        val queryData: Option<QueryData.TownData> =
            httpGetQueryData(townsDataUrl, mapOf("provincia" to provinceId.toString()), "municipios")

        return queryData.toList().flatMap { it.matches }
    }

    private inline fun <reified QueryDataT : QueryData>httpGetQueryData(url: String, params: Map<String, String>, resultListKey: String): Option<QueryDataT> {
        val queryResponse: Response = get(url = url, params = params)

        val dataJSONString: Option<String> =
                Some(queryResponse).filter { it.statusCode == 200 }.map { it.text }

        val mapper = ObjectMapper().registerKotlinModule()
        return dataJSONString.map { mapper.readValue(it, QueryDataT::class.java) }
    }
}
