package com.grupox.wololo.model.externalservices

import arrow.core.*
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Coordinates
import io.github.rybalkinsd.kohttp.dsl.httpPost
import io.github.rybalkinsd.kohttp.ext.url
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeoRefTownRequest (
        @JsonProperty("departamentos") val towns: List<GeoRefTownBodyParams>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeoRefTownBodyParams(
        @JsonProperty("provincia") val province: String,
        @JsonProperty("nombre") val name: String,
        @JsonProperty("max") val max: Int = 1
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeoRefTownResponse(
        @JsonProperty("resultados") val results: List<GeoRefTownResults>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeoRefTownResults (
        @JsonProperty("departamentos") val towns: List<TownGeoRef>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TownGeoRef(
        @JsonProperty("id") val id: Int,
        @JsonProperty("nombre") val name: String,
        @JsonProperty("centroide") val coordinates: Coordinates
)

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
class GeoRef {
    private val townsDataUrl = "https://apis.datos.gob.ar/georef/api/departamentos"
    private val mapper = jacksonObjectMapper()

    fun requestTownsData(provinceName: String, townsNames: List<String>): Either<CustomException, List<TownGeoRef>> {
        val res = httpPost {
            url(townsDataUrl)
            body {
                json(createBody(provinceName, townsNames))
            }
        }
        if(!res.isSuccessful) return Left(CustomException.Service.UnsuccessfulExternalRequestException("GeoRef", res.code()))
        if(res.body() == null) return Left(CustomException.Service.InvalidExternalResponseException("Request: POST $townsDataUrl returned with null"))
        val georefResponse = mapper.readValue<GeoRefTownResponse>(res.body()!!.string())
        if(georefResponse.results.map { it.towns }.any { it.isEmpty() }) return Left(CustomException.Service.InvalidExternalResponseException("Request: POST $townsDataUrl returned with null"))
        return Right(georefResponse.results.map { it.towns.first() })
    }

    private fun createBody(province: String, townsNames: List<String>): String {
        val towns = townsNames.map { GeoRefTownBodyParams(province, it) }
        return mapper.writeValueAsString(GeoRefTownRequest(towns))
    }


}