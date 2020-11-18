package com.grupox.wololo.model.externalservices

import arrow.core.*
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Coordinates
import io.github.rybalkinsd.kohttp.client.client
import io.github.rybalkinsd.kohttp.client.defaultHttpClient
import io.github.rybalkinsd.kohttp.client.fork
import io.github.rybalkinsd.kohttp.dsl.httpPost
import io.github.rybalkinsd.kohttp.ext.url
import io.github.rybalkinsd.kohttp.interceptors.RetryInterceptor
import okhttp3.Response
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody

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
        @JsonProperty("nombre") var name: String,
        @JsonProperty("centroide") val coordinates: Coordinates
)

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
class GeoRef {
    private val townsDataUrl = "https://apis.datos.gob.ar/georef/api/departamentos"
    private val mapper = jacksonObjectMapper()

    @Cacheable("withTimeToLive")
    fun requestTownsData(provinceName: String, townsNames: List<String>): Either<CustomException, List<TownGeoRef>> {
        val res = postDataToGeoRef(provinceName, townsNames)

        if(!res.isSuccessful) return Left(CustomException.Service.UnsuccessfulExternalRequestException("GeoRef", res.code()))
        if(res.body() == null) return Left(CustomException.Service.InvalidExternalResponseException("Request: POST $townsDataUrl returned with null"))
        val georefResponse = mapper.readValue<GeoRefTownResponse>(res.body()!!.string())
        res.close()
        if(georefResponse.results.map { it.towns }.any { it.isEmpty() }) return Left(CustomException.Service.InvalidExternalResponseException("Request: POST $townsDataUrl returned with null"))
        return Right(georefResponse.results.map { it.towns.first() })
    }

    fun postDataToGeoRef(provinceName: String, townsNames: List<String>): Response =
            httpPost { url(townsDataUrl)
                body {
                    json(createBody(provinceName, townsNames))
                }
                client {
                    defaultHttpClient.fork {
                        interceptors {
                            +RetryInterceptor()
                        }
                    }
                }
            }

    private fun createBody(province: String, townsNames: List<String>): String {
        val towns = townsNames.map { GeoRefTownBodyParams(province, it) }
        return mapper.writeValueAsString(GeoRefTownRequest(towns))
    }


}