package com.grupox.wololo.model_tests

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.collections.ArrayList

class GameTests {
    val user1: User = User(1,"a_mail", "a_password", false)
    val user2: User = User(2,"other_mail", "other_password", false)

    val town1: Town = Town(id = 1, name = "town1", elevation = 10.0)
    val town2: Town = Town(id = 2, name = "town2", elevation = 10.0)
    val town3: Town = Town(id = 3, name = "town3", elevation = 10.0)
    val town4: Town = Town(id = 4, name = "town4", elevation = 10.0)
    val town5: Town = Town(id = 5, name = "town5", elevation = 10.0)

    val towns: List<Town> = listOf(town1, town2, town3, town4, town5)
    val players: List<User> = listOf(user1, user2)

    @Nested
    @DisplayName("Game Creation")
    inner class GameCreationTest {
        @Test
        fun `creating a game distributes towns with the available players evenly`() {
            val game = Game(id = 1, players = players, province = Province(0, "a_province", ArrayList(towns)))

            val numberOfTownsAssignedToUser1: Int = game.province.towns.count { it.owner?.id == user1.id }
            val numberOfTownsAssignedToUser2: Int = game.province.towns.count { it.owner?.id == user2.id }

            assertEquals(numberOfTownsAssignedToUser1, numberOfTownsAssignedToUser2)
        }

        @Test
        fun `Attempting to create a game with more players than towns throws IlegalGameException`() {
            assertThrows<CustomException.BadRequest.IllegalGameException> { Game(id = 1, players = players, province = Province(0, "a_province", ArrayList(listOf(town1)))) }
        }

        @Test
        fun `Attempting to create a game without players throws IlegalGameException`() {
            assertThrows<CustomException.BadRequest.IllegalGameException> { Game(id = 1, players = listOf(), province = Province(0, "a_province", ArrayList(towns))) }
        }

        @Test
        fun `Can successfully create a game with none empty list of players`() {
            assertDoesNotThrow { Game(id = 1, players = players, province = Province(0, "a_province", ArrayList(towns))) }
        }

        @Test
        fun `Towns specialization is PRODUCTION by default`() {
            val game = Game(id = 1, players = players, province = Province(0, "a_province", ArrayList(towns)))
            val aTown = game.province.towns[0]
            assertThat(aTown.specialization).isInstanceOf(Production::class.java)
        }

        @Test
        fun `Can change a towns specialization from PRODUCTION to DEFENSE`() {
            val game = Game(id = 1, players = players, province = Province(0, "a_province", ArrayList(towns)))
            val aTown = game.province.towns.find { it.owner?.id == game.turn.id }
            game.changeTownSpecialization(aTown?.owner?.id!!, aTown.id, Defense())
            assertThat(aTown.specialization).isInstanceOf(Defense::class.java)
        }

        @Test
        fun `Can change a towns specialization from DEFENSE to PRODUCTION`() {
            val game = Game(id = 1, players = players, province = Province(0, "a_province", ArrayList(towns)))
            val aTown = game.province.towns.find { it.owner?.id == game.turn.id }
            game.changeTownSpecialization(aTown?.owner?.id!!, aTown.id, Defense())
            // Change back to production
            game.changeTownSpecialization(aTown.owner?.id!!, aTown.id, Production())
            assertThat(aTown.specialization).isInstanceOf(Production::class.java)
        }

        @Test
        fun `Attempting to change the specialization of a town that doesnt exist in the game will result in an exception`() {
            val game = Game(id = 1, players = players, province = Province(0, "a_province", ArrayList(towns)))
            val aTownThatDoesntExistId = 9999
            val aValidUserID = game.turn.id
            assertThrows<CustomException.NotFound.TownNotFoundException> { game.changeTownSpecialization(aValidUserID, aTownThatDoesntExistId, Defense()) }
        }
    }
}