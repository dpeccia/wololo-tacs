package com.grupox.wololo.model_tests

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ChangeTests {
    val user1: User = User("a_user", "a_mail", "a_password")
    val user2: User = User("other_user", "other_mail", "other_password")
    val twoUserList = arrayListOf(user1, user2)

    val town1: Town = Town.new("town1", 11.0, Coordinates((-65.3).toFloat(), (-22.4).toFloat()))
    val town2: Town = Town.new("town2", 12.0, Coordinates((-66.2).toFloat(), (2.0).toFloat()))
    val town3: Town = Town.new("town3", 13.0)
    val town4: Town = Town.new("town4", 14.0)
    val town5: Town = Town.new("town5", 15.0)
    val towns = arrayListOf(town1, town2, town3, town4, town5)

    val game1: Game = Game.new(twoUserList, Province("a_province", towns))

    @Test
    fun `the difference between the same game without any changes returns an empty deltaTowns list`() {
        val diff = game1.state().diff(game1.state())
        assertThat(diff.deltaTowns).isEmpty()
    }

    @Test
    fun `states of two different types can not be differentiated`() {
        assertThrows<CustomException.InternalServer.DiffException> { game1.state().diff(town1.state()) }
    }

    @Test
    fun `states of different ids can not be differentiated`() {
        assertThrows<CustomException.InternalServer.DiffException> { town1.state().diff(town2.state()) }
    }

    @Test
    fun `a change in a town's state results in a difference between both game states, before and after, with a deltaTowns of size one`() {
        val aTown = game1.province.towns.find { it.owner?.id == game1.turn.id }!!
        val initialState = game1.state()
        game1.changeTownSpecialization(aTown.owner!!, aTown.id, Defense())
        val finalState = game1.state()
        val diff = finalState.diff(initialState)
        assertThat(diff.deltaTowns).hasSize(1)
    }
}