package com.grupox.wololo.model.services

import arrow.core.Either
import com.grupox.wololo.errors.CustomException


object Pixabay : HttpService(apiName = "Pixabay"){
    private const val baseUrl = "https://pixabay.com/api/"
    private const val apiKey = "18188600-5dcc119bdfe90fa9bca9531b9" // esto no deberia ir aca! Es solo de prueba
    private val configParams: Map<String, String> = mapOf(
                                                        "key" to apiKey,
                                                        "lang" to "es",
                                                        "image_type" to "photo",
                                                        "category" to "places"
                                                    )

    private fun requestImage(locationName: String): Either<CustomException, String> {
        val queryParams = configParams.toMutableMap()  // TODO: No me gusta esto :S
        queryParams.putIfAbsent("p", locationName)
        TODO()
    }

    private fun urlFormat(str: String) = str.replace(' ', '+')
}