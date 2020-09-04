package com.grupox.wololo.model.services

import arrow.core.*
import arrow.core.extensions.list.foldable.foldLeft
import com.grupox.wololo.errors.CustomException
import io.github.rybalkinsd.kohttp.ext.httpGet
import io.github.rybalkinsd.kohttp.jackson.ext.toType

abstract class HttpService(val apiName: String) {
    protected inline fun <reified DataT : Any>getData(url: String, queryParams: Map<String, String>): Either<CustomException, DataT> {
        val finalUrl = appendQueryParams(url, queryParams)
        return Right(finalUrl.httpGet())
                .filterOrOther({ it.isSuccessful }, { CustomException.UnsuccessfulExternalRequest(apiName, it.code()) })
                .flatMap { it.toType<DataT>().rightIfNotNull { CustomException.NotFoundException("Request: GET $finalUrl returned with null") } }
    }

    protected fun appendQueryParams(url: String, queryParams: Map<String, String>): String =
            queryParams.toList()
                    .foldLeft("$url?") { unf, (key, value) -> "$unf$key=$value&" }
                    .removeSuffix("&")
}