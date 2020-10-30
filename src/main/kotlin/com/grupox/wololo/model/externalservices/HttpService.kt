package com.grupox.wololo.model.externalservices

import arrow.core.*
import arrow.core.extensions.list.foldable.foldLeft
import com.grupox.wololo.errors.CustomException
import io.github.rybalkinsd.kohttp.client.client
import io.github.rybalkinsd.kohttp.client.defaultHttpClient
import io.github.rybalkinsd.kohttp.client.fork
import io.github.rybalkinsd.kohttp.dsl.httpGet
import io.github.rybalkinsd.kohttp.ext.httpGet
import io.github.rybalkinsd.kohttp.ext.url
import io.github.rybalkinsd.kohttp.interceptors.RetryInterceptor
import io.github.rybalkinsd.kohttp.jackson.ext.toType

abstract class HttpService(val apiName: String) {
    protected inline fun <reified DataT : Any>requestData(url: String, queryParams: Map<String, String>): Either<CustomException, DataT> {
        val finalUrl = appendQueryParams(url, queryParams)
        return Right(httpGet {
            url(finalUrl)
            client {
                defaultHttpClient.fork {
                    interceptors {
                        +RetryInterceptor()
                    }
                }
            }
        }).filterOrOther({ it.isSuccessful }, { CustomException.Service.UnsuccessfulExternalRequestException(apiName, it.code()) })
                .flatMap { it.toType<DataT>().rightIfNotNull { CustomException.Service.InvalidExternalResponseException("Request: GET $finalUrl returned with null") } }
    }

    protected fun appendQueryParams(url: String, queryParams: Map<String, String>): String =
            queryParams.toList()
                    .foldLeft("$url?") { unf, (key, value) -> "$unf$key=$value&" }
                    .removeSuffix("&")
}