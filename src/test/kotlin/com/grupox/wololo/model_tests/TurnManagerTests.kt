package com.grupox.wololo.model_tests

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.TurnManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class TurnManagerTests {
    private val ids: List<Int> = listOf(1,2,3,4,5,6,7)
    private val turnManager: TurnManager<Int> = TurnManager(this.ids)

    @Test
    fun `after not changing any turn, turnManager#current is equal to the first element in the id list`() {
        assertThat(turnManager.current).isEqualTo(this.ids.first())
    }

    @Test
    fun `changing turn once makes turnManager#current to be equal to the second element of the id list`() {
        turnManager.changeTurn()
        assertThat(turnManager.current).isEqualTo(this.ids[1])
    }

    @Test
    fun `changing turn twice makes turnManager#current to be equal to the third element of the id list`() {
        turnManager.changeTurn()
        turnManager.changeTurn()
        assertThat(turnManager.current).isEqualTo(this.ids[2])
    }

    @Test
    fun `turnManager#current can be manually set to any id of the id list`() {
        val setId = 5
        turnManager.current = setId
        assertThat(turnManager.current).isEqualTo(setId)
    }

    @Test
    fun `attempting to set current with an id not included in the id list will result in an exception`() {
        val noneExistentId = 10
        assertThrows<CustomException.InternalServer.TurnManagerParticipantException> { turnManager.current = noneExistentId }
    }

    @Test
    fun `when current is the last element of the id list, if the turn is changed, turnManager#current will be equal to the first element`() {
        turnManager.current = this.ids.last()
        turnManager.changeTurn()
        assertThat(turnManager.current).isEqualTo(this.ids.first())
    }
    
    @Test
    fun `when current is the last element of the id list, if the turn is changed twice, turnManager#current will be equal to the second element`() {
        turnManager.current = this.ids.last()
        turnManager.changeTurn()
        turnManager.changeTurn()
        assertThat(turnManager.current).isEqualTo(this.ids[1])
    }

    @Test
    fun `if an element is removed from the id list, changing turn with never reach that id as turnManager#current`() {
        turnManager.removeParticipant(this.ids[1])
        turnManager.changeTurn()
        assertThat(turnManager.current).isEqualTo(this.ids[2])
    }

    @Test
    fun `if turnManager#current is removed, the new turnManager#current will be the next element in the id list`() {
        turnManager.removeParticipant(turnManager.current)
        assertThat(turnManager.current).isEqualTo(this.ids[1])
    }
    
    @Test
    fun `if turnManager#current is removed and it is the last element of the id list, the new turnManager#current will be the first element of the list`() {
        val lastId = this.ids.last()
        turnManager.current = lastId
        turnManager.removeParticipant(lastId)
        assertThat(turnManager.current).isEqualTo(this.ids.first())
    }
}