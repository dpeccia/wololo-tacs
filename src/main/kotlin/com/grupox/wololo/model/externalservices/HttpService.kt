package com.grupox.wololo.model.externalservices

import arrow.core.*
import arrow.core.extensions.list.foldable.foldLeft
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.grupox.wololo.errors.CustomException
import io.github.rybalkinsd.kohttp.client.client
import io.github.rybalkinsd.kohttp.client.defaultHttpClient
import io.github.rybalkinsd.kohttp.client.fork
import io.github.rybalkinsd.kohttp.dsl.httpGet
import io.github.rybalkinsd.kohttp.ext.url
import io.github.rybalkinsd.kohttp.interceptors.RetryInterceptor
import okhttp3.Response

class HttpService(val apiName: String) {
    inline fun <reified DataT : Any>requestData(url: String, queryParams: Map<String, String>): Either<CustomException, DataT> {
        val finalUrl = appendQueryParams(url, queryParams)
        val mapper = jacksonObjectMapper()
        val res = getData(finalUrl)
        if(!res.isSuccessful) return Left(CustomException.Service.UnsuccessfulExternalRequestException(apiName, res.code()))
        if(res.body() == null) return Left(CustomException.Service.InvalidExternalResponseException("Request: GET $finalUrl returned with null"))
        val response = mapper.readValue<DataT>(res.body()!!.string())
        res.close()
        return Right(response)
    }

    fun getData(finalUrl: String): Response =
            httpGet {
                url(finalUrl)
                client {
                    defaultHttpClient.fork {
                        interceptors {
                            +RetryInterceptor()
                        }
                    }
                }
            }

    fun appendQueryParams(url: String, queryParams: Map<String, String>): String =
            queryParams.toList()
                    .foldLeft("$url?") { unf, (key, value) -> "$unf$key=$value&" }
                    .removeSuffix("&")
}