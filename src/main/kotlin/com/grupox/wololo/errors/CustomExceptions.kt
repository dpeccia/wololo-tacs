package com.grupox.wololo.errors

data class ExceptionData(var message: String?)

class NotFoundException(message: String) : Exception(message)
