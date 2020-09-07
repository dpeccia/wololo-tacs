package com.grupox.wololo.model.services

import arrow.core.*
import arrow.core.extensions.either.applicativeError.catch
import arrow.core.extensions.list.foldable.foldLeft
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import io.github.rybalkinsd.kohttp.ext.httpGet
import io.github.rybalkinsd.kohttp.jackson.ext.toType

@JsonIgnoreProperties(ignoreUnknown = true)
private sealed class GeoRefResponse() {
    data class ProvinceQuery(
        @JsonProperty("provincias") val matches: List<LocationData>
    ) : GeoRefResponse()

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TownsQuery(
        @JsonProperty("municipios") val matches: List<LocationData>
    ) : GeoRefResponse()
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class LocationData(
        @JsonProperty("centroide") val coordinates: Coordinates,
        @JsonProperty("id") val id: Int,
        @JsonProperty("nombre") val name: String
)

object GeoRef {
    private const val apiName = "GeoRef"
    private const val provinceDataUrl = "https://apis.datos.gob.ar/georef/api/provincias"
    private const val townsDataUrl = "https://apis.datos.gob.ar/georef/api/municipios"
    private const val exactValue: Boolean = true // Las busquedas por nombres buscan el match exacto
    private const val maxMatches: Int = 200 // Numero maximo de resultados que devuelve una query a georef

    fun generateProvince(provinceName: String): Either<CustomException, Province> {
        return httpGetProvinceData(provinceName).flatMap { provinceData ->
            httpGetTownsData(provinceData.id).map { townsData ->
                Province (
                    id = provinceData.id,
                    name = provinceData.name,
                    coordinates = provinceData.coordinates,
                    towns = ArrayList(townsData.map { Town(it.id, it.name, it.coordinates, Production(), TownStats()) })
                )
            }
        }
    }

    private fun httpGetProvinceData(name: String): Either<CustomException, LocationData> {
        val responseData: Either<CustomException, GeoRefResponse.ProvinceQuery> =
            httpGetQueryData(provinceDataUrl, mapOf("nombre" to name, "exacto" to exactValue.toString()))

        return responseData.flatMap {
            it.matches.firstOrNull().rightIfNotNull { CustomException.NotFoundException("There are no matches for provinces with name: $name") }
        }
    }

    private fun httpGetTownsData(provinceId: Int): Either<CustomException, List<LocationData>> {
        val responseData: Either<CustomException, GeoRefResponse.TownsQuery> =
            httpGetQueryData(townsDataUrl, mapOf("provincia" to provinceId.toString(), "max" to maxMatches.toString()))

        return responseData.map { it.matches }
    }

    private inline fun <reified QueryDataT : GeoRefResponse>httpGetQueryData(url: String, queryParams: Map<String, String>): Either<CustomException, QueryDataT> {
        val finalUrl = appendQueryParams(url, queryParams)
        return Right(finalUrl.httpGet())
                .filterOrOther({ it.isSuccessful }, { CustomException.UnsuccessfulExternalRequest(apiName, it.code()) })
                .flatMap { it.toType<QueryDataT>().rightIfNotNull { CustomException.NotFoundException("Request: GET $finalUrl returned with null") } }
    }

    private fun appendQueryParams(url: String, queryParams: Map<String, String>): String =
        queryParams.toList()
                .foldLeft("$url?") { unf, (key, value) -> "$unf$key=$value&" }
                .removeSuffix("&")
}
