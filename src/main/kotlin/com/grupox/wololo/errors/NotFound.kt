package com.grupox.wololo.errors

data class NotFound(var message: String?)

class NotFoundException(message: String): Exception(message)