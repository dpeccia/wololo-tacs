package com.grupox.wololo.errors

data class ExceptionJSON(val message: String?)

sealed class CustomException(message: String?) : Exception(message){
    class NotFoundException(message: String) : CustomException(message)

    fun getJSON(): ExceptionJSON = ExceptionJSON(message) // Default
}

