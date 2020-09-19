package com.grupox.wololo.model.externalservices

import arrow.core.Either
import arrow.core.mapOf
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Coordinates
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Service

@JsonIgnoreProperties(ignoreUnknown = true)
private sealed class GeoRefResponse() {
    data class ProvinceQuery(
        @JsonProperty("provincias") val matches: List<ProvinceGeoRef>
    ) : GeoRefResponse()

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TownsQuery(
        @JsonProperty("municipios") val matches: List<TownGeoRef>
    ) : GeoRefResponse()
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class ProvinceGeoRef(@JsonProperty("nombre") val name: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TownGeoRef(
        @JsonProperty("id") val id: Int,
        @JsonProperty("nombre") val name: String,
        @JsonProperty("centroide") val coordinates: Coordinates
)

interface IGeoRef {
    fun requestAvailableProvinces(): Either<CustomException, List<String>>
    fun requestTownsData(provinceName: String): Either<CustomException, List<TownGeoRef>>
}

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
class GeoRef(@Autowired private val self: GeoRef?) : HttpService("GeoRef"), IGeoRef{
    private val provinceDataUrl = "https://apis.datos.gob.ar/georef/api/provincias"
    private val townsDataUrl = "https://apis.datos.gob.ar/georef/api/municipios"
    private val maxMatches = "200"

    @Cacheable(cacheNames = ["withTimeToLive"])
    override fun requestTownsData(provinceName: String): Either<CustomException, List<TownGeoRef>> {
        return requestData<GeoRefResponse.TownsQuery>(townsDataUrl, mapOf("provincia" to provinceName, "max" to maxMatches)).map { it.matches }
    }

    fun requestTownsData(provinceName: String, amount: Int): Either<CustomException, List<TownGeoRef>> =
        self!!.requestTownsData(provinceName).map { it.take(amount) }

    @Cacheable(cacheNames = ["withTimeToLive"])
    override fun requestAvailableProvinces(): Either<CustomException, List<String>> {
        return requestData<GeoRefResponse.ProvinceQuery>(provinceDataUrl, mapOf()).map { list -> list.matches.map { it.name } }
    }

}