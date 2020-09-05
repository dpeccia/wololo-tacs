package com.grupox.wololo.model.services

import arrow.core.*
import arrow.core.extensions.either.monad.flatMap
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Coordinates

@JsonIgnoreProperties(ignoreUnknown = true)
private data class TopoDataResponse(val results: List<ElevationData>)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class ElevationData(val elevation: Float)

object TopoData : HttpService("TopoData") {
    private const val baseUrl: String = "http://api.opentopodata.org/v1/test-dataset"

    fun requestElevation(coordinates: Coordinates): Either<CustomException, Float> {
        val queryResponse: Either<CustomException, TopoDataResponse> =
            requestData(baseUrl, mapOf("locations" to "${coordinates.latitude},${coordinates.longitude}"))

        return queryResponse
                .filterOrOther({ it.results.isNotEmpty() }, { CustomException.BadDataFromExternalRequestException("Theres is no data for coordinates: $coordinates in $apiName API") })
                .map { it.results.first().elevation }
    }
}