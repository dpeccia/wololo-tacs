package com.grupox.wololo.model

data class GameForm(val provinceName: String, val townAmount: Int, val participantsIds: List<Int>)

class Game(val id: Int, val province: Province, status: Status = Status.NEW) {
    var status: Status = status
        private set

    fun getTownById(idTown: Int): Town ? = province.getTownById(idTown)
}
