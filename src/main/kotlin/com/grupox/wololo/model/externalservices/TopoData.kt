package com.grupox.wololo.model.externalservices

import arrow.core.Either
import arrow.core.filterOrOther
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Coordinates
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Service

@JsonIgnoreProperties(ignoreUnknown = true)
private data class TopoDataResponse(val results: List<ElevationData>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ElevationData(val elevation: Double, val location: LocationData)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LocationData(val lat: Float, val lng: Float)

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
class TopoData {
    private val baseUrl: String = "https://api.opentopodata.org/v1/test-dataset"
    var httpService = HttpService("TopoData")

    @Cacheable("withTimeToLive")
    fun requestElevation(coordinatesList: List<Coordinates>): Either<CustomException, List<ElevationData>> {
        val coordinates = coordinatesList.joinToString("|") { "${it.latitude},${it.longitude}" }
        val queryResponse: Either<CustomException, TopoDataResponse> = httpService.requestData(baseUrl, mapOf("locations" to coordinates))

        return queryResponse
                .filterOrOther({ it.results.isNotEmpty() }, { CustomException.Service.InvalidExternalResponseException("Theres is no data for coordinates: $coordinates in TopoData API") })
                .map { it.results }
    }
}