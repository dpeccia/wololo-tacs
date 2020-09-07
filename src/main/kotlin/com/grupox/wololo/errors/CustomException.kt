package com.grupox.wololo.errors

data class ExceptionJSON(val message: String?)

sealed class CustomException(message: String?) : Exception(message){
    class NotFoundException(message: String) : CustomException(message)
    class TokenException(message: String) : CustomException(message)
    class UnsuccessfulExternalRequest(apiName: String, statusCode: Int) : CustomException("Request to $apiName API servers returned status code: $statusCode")

    fun getJSON(): ExceptionJSON = ExceptionJSON(message) // Default
}

