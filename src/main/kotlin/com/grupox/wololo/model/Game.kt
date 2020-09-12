package com.grupox.wololo.model

class Game(val id: Int, val province: Province, status: Status = Status.NEW) {
    var status: Status = status
        private set

    fun changeStatus(newStatus: Status){
        status = newStatus
    }
    fun getTownById(idTown: Int): Town ? = province.getTownById(idTown)
}
