package com.grupox.wololo.model_tests

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.AttackForm
import com.grupox.wololo.model.helpers.MovementForm
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.function.Executable

class GameTests {
    val user1: User = User("a_user", "a_mail", "a_password")
    val user2: User = User("other_user", "other_mail", "other_password")
    val user3: User = User("other_user2", "other_mail2", "other_password2")
    val user4: User = User("other_user3", "other_mail3", "other_password3")
    val user99: User = User("user", "mail", "pass")

    val town1: Town = Town(name = "town1", coordinates = Coordinates((-65.3).toFloat(), (-22.4).toFloat()), elevation = 11.0)
    val town2: Town = Town(name = "town2", coordinates = Coordinates((-66.2).toFloat(), (2.0).toFloat()), elevation = 12.0)
    val town3: Town = Town(name = "town3", elevation = 13.0)
    val town4: Town = Town(name = "town4", elevation = 14.0)
    val town5: Town = Town(name = "town5", elevation = 15.0)

    val towns: List<Town> = listOf(town1, town2, town3, town4, town5)
    val twoPlayerList: List<User> = listOf(user1, user2)
    val fourPlayerList: List<User> = listOf(user1, user2, user3, user4)

    @Nested
    inner class GameCreation {
        @Test
        fun `creating a game distributes towns with the available players evenly`() {
            val game = Game(players = twoPlayerList, province = Province("a_province", ArrayList(towns)))

            val numberOfTownsAssignedToUser1: Int = game.province.towns.count { it.owner?.id == user1.id }
            val numberOfTownsAssignedToUser2: Int = game.province.towns.count { it.owner?.id == user2.id }

            assertEquals(numberOfTownsAssignedToUser1, numberOfTownsAssignedToUser2)
        }

        @Test
        fun `Attempting to create a game with more players than towns throws IlegalGameException`() {
            assertThrows<CustomException.BadRequest.IllegalGameException> { Game(players = twoPlayerList, province = Province("a_province", ArrayList(listOf(town1)))) }
        }

        @Test
        fun `Attempting to create a game without players throws IllegalGameException`() {
            assertThrows<CustomException.BadRequest.IllegalGameException> { Game(players = listOf(), province = Province("a_province", ArrayList(towns))) }
        }

        @Test
        fun `Attempting to create a game with only one player throws IllegalGameException`() {
            assertThrows<CustomException.BadRequest.IllegalGameException> { Game(players = listOf(user1), province = Province("a_province", ArrayList(towns))) }
        }

        @Test
        fun `Can successfully create a game with 2 players`() {
            assertDoesNotThrow { Game(players = twoPlayerList, province = Province("a_province", ArrayList(towns))) }
        }

        @Test
        fun `Can successfully create a game with 3 players`() {
            assertDoesNotThrow { Game(players = listOf(user1, user2, user3), province = Province("a_province", ArrayList(towns))) }
        }

        @Test
        fun `Can successfully create a game with 4 players`() {
            assertDoesNotThrow { Game(players = fourPlayerList, province = Province("a_province", ArrayList(towns))) }
        }

        @Test
        fun `Attempting to create a game with 5 players throws IllegalGameException`() {
            val fivePlayerList = listOf(user1, user2, user3, user4, user99)
            assertThrows<CustomException.BadRequest.IllegalGameException> { Game(players = fivePlayerList, province = Province("a_province", ArrayList(towns))) }
        }

        @Test
        fun `Towns specialization is PRODUCTION by default`() {
            val game = Game(players = twoPlayerList, province = Province("a_province", ArrayList(towns)))
            val aTown = game.province.towns[0]
            assertThat(aTown.specialization).isInstanceOf(Production::class.java)
        }

        @Test
        fun `Game status is OnGoing when creation finish`() {
            val game = Game(players = twoPlayerList, province = Province("a_province", ArrayList(towns)))
            assertThat(game.status).isEqualTo(Status.ONGOING)
        }

        @Test
        fun `Is someone turn when the Game Begins`() {
            val game = Game(players = twoPlayerList, province = Province("a_province", ArrayList(towns)))
            assertNotNull(game.turn)
        }

        @Test
        fun `There are gauchos in all the towns when the Game begins`() {
            val yavi = Town("Yavi", elevation =  3485.0263671875)
            val elCondor = Town("El Cóndor", elevation = 3609.618408203125)
            val abraPampa = Town("Abra Pampa", elevation = 3519.69287109375)
            val jujuy = Province("Jujuy", arrayListOf(yavi, elCondor, abraPampa))
            Game(players = twoPlayerList, province = jujuy)
            assertAll(
                    Executable { assertThat(yavi.gauchos).isEqualTo(15) },
                    Executable { assertThat(elCondor.gauchos).isEqualTo(8) },
                    Executable { assertThat(abraPampa.gauchos).isEqualTo(13) }
            )
        }
    }

    @Nested
    inner class ChangeTownSpecialization {
        @Test
        fun `Can change a towns specialization from PRODUCTION to DEFENSE`() {
            val game = Game(players = twoPlayerList, province = Province("a_province", ArrayList(towns)))
            val aTown = game.province.towns.find { it.owner?.id == game.turn.id }!!
            game.changeTownSpecialization(aTown.owner!!, aTown.id, Defense())
            assertThat(aTown.specialization).isInstanceOf(Defense::class.java)
        }

        @Test
        fun `Can change a towns specialization from DEFENSE to PRODUCTION`() {
            val game = Game(players = twoPlayerList, province = Province("a_province", ArrayList(towns)))
            val aTown = game.province.towns.filter { it.owner != null }.find { it.owner!!.id == game.turn.id }!!
            game.changeTownSpecialization(aTown.owner!!, aTown.id, Defense())
            // Change back to production
            game.changeTownSpecialization(aTown.owner!!, aTown.id, Production())
            assertThat(aTown.specialization).isInstanceOf(Production::class.java)
        }

        @Test
        fun `Attempting to change the specialization of a town that doesnt exist in the game will result in an exception`() {
            val game = Game(players = twoPlayerList, province = Province("a_province", ArrayList(towns)))
            val aTownThatDoesntExist = Town("a town that doesnt exist", elevation = 11.0)
            val aValidUser = game.turn
            assertThrows<CustomException.NotFound.TownNotFoundException> { game.changeTownSpecialization(aValidUser, aTownThatDoesntExist.id, Defense()) }
        }

        @Test
        fun `Attempting to change the specialization by an user that has not the turn will result in an exception`() {
            val game = Game(players = twoPlayerList, province = Province("a_province", ArrayList(towns)))
            val forbiddenUser = game.players.find { it.id != game.turn.id }!!
            val aTown = game.province.towns.filter { it.owner != null }.find { it.owner == forbiddenUser }!!
            assertThrows<CustomException.Forbidden.NotYourTurnException> { game.changeTownSpecialization(forbiddenUser, aTown.id, Defense()) }
        }

        @Test
        fun `Attempting to change the specialization of a town that doesnt belong to the user in turn results in an exception`() {
            val game = Game(players = twoPlayerList, province = Province("a_province", ArrayList(towns)))
            val aValidUser = game.turn
            val notUsersTown = game.province.towns.filter { it.owner != null }.find { it.owner != aValidUser }!!
            assertThrows<CustomException.Forbidden.NotYourTownException> { game.changeTownSpecialization(aValidUser, notUsersTown.id, Defense()) }
        }
    }

    @Nested
    inner class MoveGauchos {
        private val towns: List<Town> = listOf(town1, town2)
        private val game = Game(players = listOf(user1, user99), province = Province("a_province", ArrayList(towns)))

        @BeforeEach
        fun fixtureMoveGauchos() {
            game.turn = user1
            town1.owner = user1
            town2.owner = user1
        }

        @Test
        fun `trying to move gauchos from a finished game throws FinishedGameException`() {
            game.status = Status.FINISHED
            assertThrows<CustomException.Forbidden.FinishedGameException> { game.moveGauchosBetweenTowns(user1, MovementForm(town1.id, town2.id,2)) }
        }

        @Test
        fun `trying to move gauchos from a canceled game throws FinishedGameException`() {
            game.status = Status.CANCELED
            assertThrows<CustomException.Forbidden.FinishedGameException> { game.moveGauchosBetweenTowns(user1, MovementForm(town1.id, town2.id,2)) }
        }

        @Test
        fun `trying to move gauchos in a game that user doesnt participate throws NotAMemberException`() {
            assertThrows<CustomException.Forbidden.NotAMemberException> { game.moveGauchosBetweenTowns(user2, MovementForm(town1.id, town2.id,2)) }
        }

        @Test
        fun `trying to move gauchos when it is not your turn throws NotYourTurnException`() {
            val game2 = Game(players = listOf(user1, user2), province = Province("a_province", ArrayList(towns)))
            game2.turn = user1
            assertThrows<CustomException.Forbidden.NotYourTurnException> { game2.moveGauchosBetweenTowns(user2, MovementForm(town1.id, town2.id,2)) }
        }

        @Test
        fun `successfully moving gauchos doesnt throw an Exception`() {
            game.turn = user1
            town1.gauchos = 10
            assertDoesNotThrow { game.moveGauchosBetweenTowns(user1, MovementForm(town1.id, town2.id,1)) }
        }

    }

    @Nested
    inner class AttackTown {
        private val towns: List<Town> = listOf(town1, town2)
        private val game = Game(players = listOf(user1, user99), province = Province("a_province", ArrayList(towns)))

        @BeforeEach
        fun fixtureAttackTown() {
            game.turn = user1
            town1.owner = user1
            town2.owner = user1
        }

        @Test
        fun `trying to attack town from a finished game throws FinishedGameException`() {
            game.status = Status.FINISHED
            assertThrows<CustomException.Forbidden.FinishedGameException> { game.attackTown(user1, AttackForm(town1.id, town2.id)) }
        }

        @Test
        fun `trying to attack town from a canceled game throws FinishedGameException`() {
            game.status = Status.CANCELED
            assertThrows<CustomException.Forbidden.FinishedGameException> { game.attackTown(user1, AttackForm(town1.id, town2.id)) }
        }

        @Test
        fun `trying to attack town in a game that user doesnt participate throws NotAMemberException`() {
            assertThrows<CustomException.Forbidden.NotAMemberException> { game.attackTown(user2, AttackForm(town1.id, town2.id)) }
        }

        @Test
        fun `trying to attack town when it is not your turn throws NotYourTurnException`() {
            val game2 = Game(players = listOf(user1, user2), province = Province("a_province", ArrayList(towns)))
            game2.turn = user1
            assertThrows<CustomException.Forbidden.NotYourTurnException> { game2.attackTown(user2, AttackForm(town1.id, town2.id)) }
        }

        @Test
        fun `successfully attacking a town doesnt throw an Exception`() {
            val game2 = Game(players = listOf(user1, user2), province = Province("a_province", ArrayList(towns)))
            game2.turn = user1
            town1.owner = user1
            town2.owner = user2
            town1.gauchos = 10
            assertDoesNotThrow { game2.attackTown(user1, AttackForm(town1.id, town2.id)) }
        }
    }

    @Nested
    inner class Turn {
        private val towns: List<Town> = listOf(town1, town2)
        private val game = Game(players = listOf(user1, user99), province = Province("a_province", ArrayList(towns)))

        @BeforeEach
        fun fixtureTurn() {
            game.turn = user1
            town1.owner = user1
            town2.owner = user1
        }

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
            val game2 = Game(players = listOf(user1, user2), province = Province("a_province", ArrayList(towns)))
            game2.turn = user1
            assertThrows<CustomException.Forbidden.NotYourTurnException> { game2.finishTurn(user2) }
        }

        @Test
        fun `successfully finish turn unlocks all towns from User`() {
            val game2 = Game(players = listOf(user1, user2), province = Province("a_province", arrayListOf(town1, town2, town3, town4, town5)))
            game2.turn = user1
            game2.province.towns.forEach { it.isLocked = true }
            game2.finishTurn(user1)
            assertTrue(game2.province.towns.filter { it.owner == user1}.all { !it.isLocked })
        }

        @Test
        fun `if a user has all towns, he wins and game status changes to FINISHED`() {
            val game2 = Game(players = listOf(user1, user2), province = Province("a_province", arrayListOf(town1, town2, town3, town4, town5)))
            game2.turn = user1
            game2.province.towns.forEach { it.owner = user1 }
            game2.finishTurn(user1)
            assertThat(game2.status).isEqualTo(Status.FINISHED)
        }

        @Test
        fun `if a user has all towns from enemy and some towns are still rebel, he wins and game status changes to FINISHED`() {
            val game2 = Game(players = listOf(user1, user2), province = Province("a_province", arrayListOf(town1, town2, town3, town4, town5)))
            game2.turn = user1
            game2.province.towns.forEach { it.owner = user1 }
            game2.province.towns[0].owner = null // rebel town
            game2.finishTurn(user1)
            assertThat(game2.status).isEqualTo(Status.FINISHED)
        }

        @Test
        fun `if a user wins, his GamesWonStats are updated`() {
            val game2 = Game(players = listOf(user1, user2), province = Province("a_province", arrayListOf(town1, town2, town3, town4, town5)))
            game2.turn = user1
            game2.province.towns.forEach { it.owner = user1 }
            game2.finishTurn(user1)
            assertAll(
                    Executable { assertThat(user1.stats.gamesWon).isEqualTo(1) },
                    Executable { assertThat(user1.stats.gamesLost).isEqualTo(0) }
            )
        }

        @Test
        fun `if a user wins, the GameLostStats of the other users are updated`() {
            val game2 = Game(players = listOf(user1, user2), province = Province("a_province", arrayListOf(town1, town2, town3, town4, town5)))
            game2.turn = user1
            game2.province.towns.forEach { it.owner = user1 }
            game2.finishTurn(user1)
            assertAll(
                    Executable { assertThat(user2.stats.gamesWon).isEqualTo(0) },
                    Executable { assertThat(user2.stats.gamesLost).isEqualTo(1) }
            )
        }

        @Test
        fun `if the first player finishes his turn and he didnt win, turn is changed to next player`() {
            val user3 = User("user3", "new_mail", "sdaddraf")
            val game2 = Game(players = listOf(user1, user2, user3), province = Province("a_province", arrayListOf(town1, town2, town3, town4, town5)))
            game2.turn = user1
            game2.finishTurn(user1)
            assertThat(game2.turn).isEqualTo(user2)
        }

        @Test
        fun `if the last player of the first round, finishes his turn and didnt win, turn is changed to first player again`() {
            val user3 = User("user3", "new_mail", "sdaddraf")
            val game2 = Game(players = listOf(user1, user2, user3), province = Province("a_province", arrayListOf(town1, town2, town3, town4, town5)))
            game2.turn = user3
            game2.finishTurn(user3)
            assertThat(game2.turn).isEqualTo(user1)
        }

        @Test
        fun `if the turn has changed, the gauchos quantity of the towns from the next player are updated`() {
            val user3 = User("user3", "new_mail", "sdaddraf")
            val game2 = Game(players = listOf(user1, user2, user3), province = Province("a_province", arrayListOf(town1, town2, town3, town4, town5)))
            game2.turn = user1
            val gauchosQtysOfUser2BeforeHisTurnStarts = game2.province.towns.filter { it.owner == user2 }.map { it.gauchos }
            game2.finishTurn(user1)
            val gauchosQtysOfUser2AfterHisTurnStarts = game2.province.towns.filter { it.owner == user2 }.map { it.gauchos }
            assertThat(gauchosQtysOfUser2BeforeHisTurnStarts).isNotEqualTo(gauchosQtysOfUser2AfterHisTurnStarts)
        }
    }
}