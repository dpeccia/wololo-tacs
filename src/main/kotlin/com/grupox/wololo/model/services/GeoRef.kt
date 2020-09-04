package com.grupox.wololo.model.services

import arrow.core.*
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Coordinates
import com.grupox.wololo.model.Province
import com.grupox.wololo.model.Town

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

object GeoRef : HttpService("GeoRef"){
    private const val provinceDataUrl = "https://apis.datos.gob.ar/georef/api/provincias"
    private const val townsDataUrl = "https://apis.datos.gob.ar/georef/api/municipios"
    private const val exactValue: Boolean = true // Las busquedas por nombres buscan el match exacto
    private const val maxMatches: Int = 200 // Numero maximo de resultados que devuelve una query a georef

    fun generateProvince(provinceName: String): Either<CustomException, Province> {
        return requestProvinceData(provinceName).flatMap { provinceData ->
            requestTownsData(provinceData.id).map { townsData ->
                Province (
                    id = provinceData.id,
                    name = provinceData.name,
                    coordinates = provinceData.coordinates,
                    towns = ArrayList(townsData.map { Town(it.id, it.name, it.coordinates) })
                )
            }
        }
    }

    private fun requestProvinceData(name: String): Either<CustomException, LocationData> {
        val responseData: Either<CustomException, GeoRefResponse.ProvinceQuery> =
            requestData(provinceDataUrl, mapOf("nombre" to name, "exacto" to exactValue.toString()))

        return responseData
                .filterOrOther({ it.matches.isNotEmpty() },{ CustomException.BadDataFromExternalRequestException("There are no matches for provinces with name: $name") })
                .map { it.matches.first() }
    }

    private fun requestTownsData(provinceId: Int): Either<CustomException, List<LocationData>> {
        val responseData: Either<CustomException, GeoRefResponse.TownsQuery> =
            requestData(townsDataUrl, mapOf("provincia" to provinceId.toString(), "max" to maxMatches.toString()))

        return responseData.map { it.matches }
    }
}
