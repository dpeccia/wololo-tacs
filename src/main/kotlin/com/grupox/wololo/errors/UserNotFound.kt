package com.grupox.wololo.errors

data class UserNotFound(var message: String?)

class UserNotFoundException(message: String): Exception(message)