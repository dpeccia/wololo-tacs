package com.grupox.wololo.errors

import com.grupox.wololo.model.helpers.DTO
import com.grupox.wololo.model.helpers.Requestable

sealed class CustomException(message: String) : Exception(message), Requestable {
    sealed class NotFound(message: String) : CustomException("Not found: $message") {
        class UserNotFoundException : NotFound("user not found in database")
        class GameNotFoundException : NotFound("game not found in database")
        class TownNotFoundException : NotFound("town was not found")
        class MemberNotFoundException : NotFound("there is no such member in the game")
    }

    sealed class Unauthorized(message: String) : CustomException("Unauthorized: $message") {
        class TokenException(message: String) : Unauthorized(message)
        class BadLoginException : Unauthorized("wrong username or password")
    }

    sealed class Service(message: String) : CustomException("Service exception: $message") {
        class UnsuccessfulExternalRequestException(apiName: String, statusCode: Int) : Service("request to $apiName API servers returned status code: $statusCode")
        class InvalidExternalResponseException(message: String) : Service(message)
        class ProvincePropertiesNotAvailableException : Service("there are no provinces available")
    }

    sealed class BadRequest(message: String) : CustomException("Bad request: $message") {
        class IllegalGameException(message: String) : BadRequest(message)
        class IllegalUserException(message: String) : BadRequest(message)
        class NotEnoughGauchosException(toMoveQty: Int, actualQty: Int) : BadRequest("you want to move $toMoveQty gauchos, when there are only $actualQty in this com.grupox.wololo.model.Town")
        class IllegalGauchosQtyException : BadRequest("the number of gauchos to move has to be > 0")
        class NotEnoughTownsException(province: String, townAmount: Int, realTownAmount: Int) : BadRequest("$province has only $realTownAmount towns and you want $townAmount towns")
    }

    sealed class Forbidden(message: String) : CustomException("Forbidden: $message") {
        class NotYourTurnException : Forbidden("it´s not your Turn to play")
        class NotAMemberException : Forbidden("you are not a member of this Game")
        class IllegalGauchoMovement(message: String) : Forbidden(message)
        class IllegalAttack(message: String) : Forbidden(message)
        class NotYourTownException : Forbidden("this is not one of your towns")
        class FinishedGameException : Forbidden("this game is already Finished, you cannot play anymore")
        class OperationNotAuthorized : Forbidden("operation not allowed")
    }

    sealed class InternalServer(message: String) : CustomException("Internal server: $message") {
        class TurnManagerParticipantException(id: String) : InternalServer("turn manager couldn't find a participant with id: $id")
    }

    override fun dto(): DTO.ExceptionDTO = DTO.ExceptionDTO(message) // Default
}

