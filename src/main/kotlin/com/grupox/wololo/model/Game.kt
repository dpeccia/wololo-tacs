package com.grupox.wololo.model

import arrow.core.Either
import arrow.core.extensions.fx
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.helpers.GameForm
import com.grupox.wololo.model.services.GeoRef
import org.springframework.beans.factory.annotation.Autowired

class Game(val players: List<User> = listOf(), val province: Province, var status: Status = Status.NEW) {
    val id: Int = 0 // TODO: Autogenerada
    fun getTownById(idTown: Int): Town? = province.towns.find { it.id == id }
}
