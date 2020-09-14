package com.grupox.wololo.errors

data class ExceptionJSON(val message: String?)

sealed class CustomException(message: String) : Exception(message){
    sealed class NotFound(message: String) : CustomException("Not found: $message") {
        class UserNotFoundException : NotFound("User not found in database")
        class GameNotFoundException : NotFound("Game not found in database")
        class TownNotFoundException : NotFound("Town was not found")
    }

    sealed class Unauthorized(message: String) : CustomException("Unauthorized: $message") {
        class TokenException(message: String) : Unauthorized(message)
        class BadLoginException(message: String) : Unauthorized(message)
    }

    sealed class Service(message: String) : CustomException("Service exception: $message") {
        class UnsuccessfulExternalRequestException(apiName: String, statusCode: Int)
            : Service("Request to $apiName API servers returned status code: $statusCode")
        class InvalidExternalResponseException(message: String)
            : Service(message)
    }

    sealed class BadRequest(message: String) : CustomException("Bad request: $message") {
        class IllegalGameException(message: String) : BadRequest(message)
        class IllegalUserException(message: String) : BadRequest(message)
        class NotEnoughGauchosException(toMoveQty: Int, actualQty: Int)
            : BadRequest("You want to move $toMoveQty gauchos, when there are only $actualQty in this Town")
    }

    sealed class Forbidden(message: String) : CustomException("Forbidden: $message") {
        class NotYourTurnException : Forbidden("It´s not your Turn to play")
        class NotAMemberException : Forbidden("You are not a member of this Game")
        class IllegalGauchoMovement(message: String) : Forbidden(message)
        class IllegalAttack(message: String) : Forbidden(message)
    }

    fun getJSON(): ExceptionJSON = ExceptionJSON(message) // Default
}

