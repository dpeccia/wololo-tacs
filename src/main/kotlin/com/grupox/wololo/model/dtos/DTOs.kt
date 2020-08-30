package com.grupox.wololo.model.dtos

import com.grupox.wololo.model.Status

sealed class DTO

data class GameDTO(val id: Int, val status: Status) : DTO()

