package com.grupox.wololo.model_tests

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.GameMode
import com.grupox.wololo.model.helpers.MailService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean

@SpringBootTest
class ChangeTests {
    lateinit var user1: User
    lateinit var user2: User
    lateinit var twoUserList: List<User>
    lateinit var townNames: List<String>
    lateinit var town1: Town
    lateinit var town2: Town
    lateinit var town3: Town
    lateinit var town4: Town
    lateinit var town5: Town
    lateinit var towns: ArrayList<Town>
    lateinit var normalMode: GameMode
    lateinit var game1: Game

    @SpyBean
    @Autowired
    lateinit var mailSender: MailService

    @BeforeEach
    fun fixture() {
        user1 = User("a_user", "a_mail", "a_password")
        user2 = User("other_user", "other_mail", "other_password")
        twoUserList = arrayListOf(user1, user2)

        townNames = listOf("town1, town2, town3, town4, town5")
        town1 = Town.new("town1", 11.0, townNames, Coordinates((-65.3).toFloat(), (-22.4).toFloat()))
        town2 = Town.new("town2", 12.0, townNames,Coordinates((-66.2).toFloat(), (2.0).toFloat()))
        town3 = Town.new("town3", 13.0, townNames)
        town4 = Town.new("town4", 14.0, townNames)
        town5 = Town.new("town5", 15.0, townNames)
        towns = arrayListOf(town1, town2, town3, town4, town5)

        normalMode = GameMode("NORMAL", 10.0, 15.0, 1.25, 1.0)
        game1 = Game.new(twoUserList, Province("a_province", towns), normalMode, mailSender)
        Mockito.doNothing().`when`(mailSender).sendMail("a_mail")
        Mockito.doNothing().`when`(mailSender).sendMail("other_mail")
    }

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