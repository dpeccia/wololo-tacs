package com.grupox.wololo.errors

data class ExceptionJSON(val message: String?)

class NotFoundException(message: String) : Exception(message) {
    fun getJSON(): ExceptionJSON = ExceptionJSON(message)
}
