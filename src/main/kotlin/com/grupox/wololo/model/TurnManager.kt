package com.grupox.wololo.model

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.helpers.MailService
import org.springframework.beans.factory.annotation.Autowired

class TurnManager<T>(private var participantIds: List<T>) {
    @Autowired
    lateinit var mailSender: MailService

    private var currentIndex: Int = 0

    var current
        get() = this.participantIds[currentIndex]
        set(value) {
            if(!this.participantIds.contains(value)) throw CustomException.InternalServer.TurnManagerParticipantException(value.toString())
            this.currentIndex = this.participantIds.indexOf(value)
        }

    fun changeTurn() {
        this.currentIndex = if(this.currentIsLastElement()) 0 else this.currentIndex + 1
    }

    fun removeParticipant(id: T) {
        if(this.isLastID(id))
            this.currentIndex = 0

        this.participantIds = this.participantIds.filter { it != id }
    }

    private fun isLastID(id: T) = this.participantIds.indexOf(id) == this.participantIds.size -1
    private fun currentIsLastElement(): Boolean = this.currentIndex + 1 == this.participantIds.size
}