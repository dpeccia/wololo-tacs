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
private data class ElevationData(val elevation: Double)

interface ITopoData {
    fun requestElevation(coordinates: Coordinates): Either<CustomException, Double>
}

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
class TopoData : HttpService("TopoData"), ITopoData {
    private val baseUrl: String = "http://api.opentopodata.org/v1/test-dataset"

    @Cacheable("withTimeToLive")
    override fun requestElevation(coordinates: Coordinates): Either<CustomException, Double> {
        val queryResponse: Either<CustomException, TopoDataResponse> =
            requestData(baseUrl, mapOf("locations" to "${coordinates.latitude},${coordinates.longitude}"))

        return queryResponse
                .filterOrOther({ it.results.isNotEmpty() }, { CustomException.Service.InvalidExternalResponseException("Theres is no data for coordinates: $coordinates in $apiName API") })
                .map { it.results.first().elevation }
    }
}