package com.grupox.wololo.errors

data class UserNotFound(var message: String)

class UserNotFoundException(): Exception()