package com.grupox.wololo.model_tests

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.AttackForm
import com.grupox.wololo.model.helpers.MovementForm
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class GameTests {
    val user1: User = User(1, "", "a_mail", "a_password", false)
    val user2: User = User(2, "", "other_mail", "other_password", false)

    val town1: Town = Town(id = 1, name = "town1", elevation = 10.0)
    val town2: Town = Town(id = 2, name = "town2", elevation = 10.0)
    val town3: Town = Town(id = 3, name = "town3", elevation = 10.0)
    val town4: Town = Town(id = 4, name = "town4", elevation = 10.0)
    val town5: Town = Town(id = 5, name = "town5", elevation = 10.0)

    val towns: List<Town> = listOf(town1, town2, town3, town4, town5)
    val players: List<User> = listOf(user1, user2)

    @Nested
    inner class GameCreation {

        // TODO test addGauchosToAllTowns
        // TODO test status sea ONGOING
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
    }

    @Nested
    inner class ChangeTownSpecialization {
        @Test
        fun `Can change a towns specialization from PRODUCTION to DEFENSE`() {
            val game = Game(id = 1, players = players, province = Province(0, "a_province", ArrayList(towns)))
            val aTown = game.province.towns.find { it.owner?.id == game.turn.id }!!
            game.changeTownSpecialization(aTown.owner!!, aTown.id, Defense())
            assertThat(aTown.specialization).isInstanceOf(Defense::class.java)
        }

        @Test
        fun `Can change a towns specialization from DEFENSE to PRODUCTION`() {
            val game = Game(id = 1, players = players, province = Province(0, "a_province", ArrayList(towns)))
            val aTown = game.province.towns.filter { it.owner != null }.find { it.owner!!.id == game.turn.id }!!
            game.changeTownSpecialization(aTown.owner!!, aTown.id, Defense())
            // Change back to production
            game.changeTownSpecialization(aTown.owner!!, aTown.id, Production())
            assertThat(aTown.specialization).isInstanceOf(Production::class.java)
        }

        @Test
        fun `Attempting to change the specialization of a town that doesnt exist in the game will result in an exception`() {
            val game = Game(id = 1, players = players, province = Province(0, "a_province", ArrayList(towns)))
            val aTownThatDoesntExistId = 9999
            val aValidUser = game.turn
            assertThrows<CustomException.NotFound.TownNotFoundException> { game.changeTownSpecialization(aValidUser, aTownThatDoesntExistId, Defense()) }
        }

        @Test
        fun `Attempting to change the specialization by an user that has not the turn will result in an exception`() {
            val game = Game(id = 1, players = players, province = Province(0, "a_province", ArrayList(towns)))
            val forbiddenUser = game.players.find { it.id != game.turn.id }!!
            val aTown = game.province.towns.filter { it.owner != null }.find { it.owner == forbiddenUser }!!
            assertThrows<CustomException.Forbidden.NotYourTurnException> { game.changeTownSpecialization(forbiddenUser, aTown.id, Defense()) }
        }

        @Test
        fun `Attempting to change the specialization of a town that doesnt belong to the user in turn results in an exception`() {
            val game = Game(id = 1, players = players, province = Province(0, "a_province", ArrayList(towns)))
            val aValidUser = game.turn
            val notUsersTown = game.province.towns.filter { it.owner != null }.find { it.owner != aValidUser }!!
            assertThrows<CustomException.Forbidden.NotYourTownException> { game.changeTownSpecialization(aValidUser, notUsersTown.id, Defense()) }
        }
    }

    @Nested
    inner class MoveGauchos {
        private val towns: List<Town> = listOf(town1, town2)
        private val game = Game(id = 1, players = listOf(user1), province = Province(0, "a_province", ArrayList(towns)))

        @Test
        fun `trying to move gauchos from a finished game throws FinishedGameException`() {
            game.status = Status.FINISHED
            assertThrows<CustomException.Forbidden.FinishedGameException> { game.moveGauchosBetweenTowns(user1, MovementForm(1,2,2)) }
        }

        @Test
        fun `trying to move gauchos from a canceled game throws FinishedGameException`() {
            game.status = Status.CANCELED
            assertThrows<CustomException.Forbidden.FinishedGameException> { game.moveGauchosBetweenTowns(user1, MovementForm(1,2,2)) }
        }

        @Test
        fun `trying to move gauchos in a game that user doesnt participate throws NotAMemberException`() {
            assertThrows<CustomException.Forbidden.NotAMemberException> { game.moveGauchosBetweenTowns(user2, MovementForm(1,2,2)) }
        }

        @Test
        fun `trying to move gauchos when it is not your turn throws NotYourTurnException`() {
            val game2 = Game(id = 1, players = listOf(user1, user2), province = Province(0, "a_province", ArrayList(towns)))
            game2.turn = user1
            assertThrows<CustomException.Forbidden.NotYourTurnException> { game2.moveGauchosBetweenTowns(user2, MovementForm(1,2,2)) }
        }
    }

    @Nested
    inner class AttackTown {
        private val towns: List<Town> = listOf(town1, town2)
        private val game = Game(id = 1, players = listOf(user1), province = Province(0, "a_province", ArrayList(towns)))

        @Test
        fun `trying to attack town from a finished game throws FinishedGameException`() {
            game.status = Status.FINISHED
            assertThrows<CustomException.Forbidden.FinishedGameException> { game.attackTown(user1, AttackForm(1,2)) }
        }

        @Test
        fun `trying to attack town from a canceled game throws FinishedGameException`() {
            game.status = Status.CANCELED
            assertThrows<CustomException.Forbidden.FinishedGameException> { game.attackTown(user1, AttackForm(1,2)) }
        }

        @Test
        fun `trying to attack town in a game that user doesnt participate throws NotAMemberException`() {
            assertThrows<CustomException.Forbidden.NotAMemberException> { game.attackTown(user2, AttackForm(1,2)) }
        }

        @Test
        fun `trying to attack town when it is not your turn throws NotYourTurnException`() {
            val game2 = Game(id = 1, players = listOf(user1, user2), province = Province(0, "a_province", ArrayList(towns)))
            game2.turn = user1
            assertThrows<CustomException.Forbidden.NotYourTurnException> { game2.attackTown(user2, AttackForm(1,2)) }
        }
    }

    @Nested
    inner class Turn {
        private val towns: List<Town> = listOf(town1, town2)
        private val game = Game(id = 1, players = listOf(user1), province = Province(0, "a_province", ArrayList(towns)))

        // TODO tests change turn and user won
        @Test
        fun `trying to finished turn from a finished game throws FinishedGameException`() {
            game.status = Status.FINISHED
            assertThrows<CustomException.Forbidden.FinishedGameException> { game.finishTurn(user1) }
        }

        @Test
        fun `trying to finished turn from a canceled game throws FinishedGameException`() {
            game.status = Status.CANCELED
            assertThrows<CustomException.Forbidden.FinishedGameException> { game.finishTurn(user1) }
        }

        @Test
        fun `trying to finished turn in a game that user doesnt participate throws NotAMemberException`() {
            assertThrows<CustomException.Forbidden.NotAMemberException> { game.finishTurn(user2) }
        }

        @Test
        fun `trying to finished turn when it is not your turn throws NotYourTurnException`() {
            val game2 = Game(id = 1, players = listOf(user1, user2), province = Province(0, "a_province", ArrayList(towns)))
            game2.turn = user1
            assertThrows<CustomException.Forbidden.NotYourTurnException> { game2.finishTurn(user2) }
        }
    }
}