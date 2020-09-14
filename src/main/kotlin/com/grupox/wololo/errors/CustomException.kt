package com.grupox.wololo.errors

data class ExceptionJSON(val message: String?)

sealed class CustomException(message: String) : Exception(message){
    sealed class NotFoundException(message: String) : CustomException("Not found exception: $message") {
        class UserNotFoundException() : NotFoundException("User not found in database")
        class GameNotFoundException() : NotFoundException("Game not found in database")
    }

    sealed class UnauthorizedException(message: String) : CustomException("Unauthorized exception: $message") {
        class TokenException(message: String) : UnauthorizedException(message)
        class BadLoginException(message: String) : UnauthorizedException(message)
    }

    sealed class ServiceException(message: String) : CustomException("Service exception: $message") {
        class UnsuccessfulExternalRequestException(apiName: String, statusCode: Int)
            : ServiceException("Request to $apiName API servers returned status code: $statusCode")
        class InvalidExternalResponseException(message: String)
            : ServiceException(message)
    }

    sealed class ModelException(message: String) : CustomException("Model exception: $message") {
        class IllegalGameException(message: String) : ModelException(message)
        class IllegalUserException(message: String) : ModelException(message)
    }

    fun getJSON(): ExceptionJSON = ExceptionJSON(message) // Default
}

