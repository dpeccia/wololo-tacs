package com.grupox.wololo.model.helpers

import com.fasterxml.jackson.annotation.JsonCreator

data class UserCredentials @JsonCreator constructor(val mail: String, val password: String)
data class UserWithoutStats @JsonCreator constructor(val id: Int, val mail: String)
data class GameData @JsonCreator constructor(val id: Int, val status: String)