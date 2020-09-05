package com.grupox.wololo.model

data class Town(
        val id: Int,
        val name: String,
        val coordinates: Coordinates = Coordinates(0f,0f),
        val elevation: Float = 0f
)
