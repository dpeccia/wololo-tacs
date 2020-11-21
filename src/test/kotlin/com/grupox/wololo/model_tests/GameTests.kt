package com.grupox.wololo.model_tests

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.AttackForm
import com.grupox.wololo.model.helpers.GameMode
import com.grupox.wololo.model.helpers.GameModeService
import com.grupox.wololo.model.helpers.MovementForm
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class GameTests {
    @Autowired
    lateinit var gameModeService: GameModeService

    private lateinit var user1: User
    private lateinit var user2: User
    private lateinit var user3: User
    private lateinit var user4: User
    private lateinit var user99: User
    private lateinit var town1: Town
    private lateinit var town2: Town
    private lateinit var town3: Town
    private lateinit var town4: Town
    private lateinit var town5: Town
    private lateinit var twoPlayerList: List<User>
    private lateinit var fourPlayerList: List<User>
    private lateinit var game1: Game
    private lateinit var normalMode: GameMode


    @BeforeEach
    fun fixture() {
        normalMode = gameModeService.getDifficultyMultipliers("NORMAL")
        user1 = User("a_user", "a_mail", "a_password")
        user2 = User("other_user", "other_mail", "other_password")
        user3 = User("other_user2", "other_mail2", "other_password2")
        user4 = User("other_user3", "other_mail3", "other_password3")
        user99 = User("user", "mail", "pass")

        town1 = Town.new("town1", 11.0, listOf("town2"), Coordinates((-65.3).toFloat(), (-22.4).toFloat()))
        town2 = Town.new("town2", 12.0, listOf("town1"), Coordinates((-66.2).toFloat(), (2.0).toFloat()))
        town3 = Town.new("town3", 13.0, listOf("town4"))
        town4 = Town.new("town4", 14.0, listOf("town3"))
        town5 = Town.new("town5", 15.0, listOf(""))

        twoPlayerList = listOf(user1, user2)
        fourPlayerList = listOf(user1, user2, user3, user4)


        game1 = Game.new(listOf(user1, user99), Province("a_province", ArrayList(listOf(town1.copy(), town2.copy()))), normalMode)
        game1.turn = user1
        game1.province.towns[0].owner = user1
        game1.province.towns[1].owner = user1
    }

    @Nested
    inner class GameCreation {
        @Test
        fun `creating a 2 player game with two towns distributes them 1 for each player`() {
            val game = Game.new(twoPlayerList, Province("a_province", ArrayList(listOf(town1.copy(), town2.copy()))), normalMode)
            val numberOfTownsAssignedToUser1: Int = game.province.towns.count { it.owner?.id == user1.id }
            assertEquals(numberOfTownsAssignedToUser1, 1)
        }

        @Test
        fun `creating a game distributes towns with the available players evenly`() {
            val game = Game.new(twoPlayerList, Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode)

            val numberOfTownsAssignedToUser1: Int = game.province.towns.count { it.owner?.id == user1.id }
            val numberOfTownsAssignedToUser2: Int = game.province.towns.count { it.owner?.id == user2.id }

            assertEquals(numberOfTownsAssignedToUser1, numberOfTownsAssignedToUser2)
        }

        @Test
        fun `Attempting to create a game with more players than towns throws IlegalGameException`() {
            assertThrows<CustomException.BadRequest.IllegalGameException> { Game.new(twoPlayerList, Province("a_province", ArrayList(listOf(town1.copy()))), normalMode) }
        }

        @Test
        fun `Attempting to create a game without players throws IlegalGameException`() {
            assertThrows<CustomException.BadRequest.IllegalGameException> { Game.new(listOf(), Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode) }
        }

        @Test
        fun `Attempting to create a game with one player throws IlegalGameException`() {
            assertThrows<CustomException.BadRequest.IllegalGameException> { Game.new(listOf(user1), Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode) }
        }

        @Test
        fun `Can successfully create a game with 2 players`() {
            assertDoesNotThrow { Game.new(twoPlayerList, Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode) }
        }

        @Test
        fun `Can successfully create a game with 3 players`() {
            assertDoesNotThrow { Game.new(listOf(user1, user2, user3), Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode) }
        }

        @Test
        fun `Can successfully create a game with 4 players`() {
            assertDoesNotThrow { Game.new(fourPlayerList, Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode) }
        }

        @Test
        fun `Attempting to create a game with 5 players throws IllegalGameException`() {
            val fivePlayerList = listOf(user1, user2, user3, user4, user99)
            assertThrows<CustomException.BadRequest.IllegalGameException> { Game.new(fivePlayerList, Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode) }
        }

        @Test
        fun `Towns specialization is PRODUCTION by default`() {
            val game = Game.new(twoPlayerList, Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode)
            val aTown = game.province.towns[0]
            assertThat(aTown.specialization).isInstanceOf(Production::class.java)
        }

        @Test
        fun `Game status is OnGoing when creation finish`() {
            val game = Game.new(twoPlayerList, Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode)
            assertThat(game.status).isEqualTo(Status.ONGOING)
        }

        @Test
        fun `Is someone turn when the Game Begins`() {
            val game = Game.new(twoPlayerList, Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode)
            assertNotNull(game.turn)
        }

        @Test
        fun `There are gauchos in all the towns when the Game begins`() {
            val yavi = Town.new("Yavi",3485.0263671875, listOf("El Cóndor", "Abra Pampa"))
            val elCondor = Town.new("El Cóndor",3609.618408203125, listOf("Yavi", "Abra Pampa"))
            val abraPampa = Town.new("Abra Pampa",3519.69287109375, listOf("El Cóndor", "Yavi"))
            val jujuy = Province("Jujuy", arrayListOf(yavi, elCondor, abraPampa))
            Game.new(twoPlayerList, jujuy, normalMode)
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
            val game = Game.new(twoPlayerList, Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode)
            val aTown = game.province.towns.find { it.owner?.id == game.turn.id }!!
            game.changeTownSpecialization(aTown.owner!!, aTown.id, Defense())
            assertThat(aTown.specialization).isInstanceOf(Defense::class.java)
        }

        @Test
        fun `Can change a towns specialization from DEFENSE to PRODUCTION`() {
            val game = Game.new(twoPlayerList, Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode)
            val aTown = game.province.towns.filter { it.owner != null }.find { it.owner!!.id == game.turn.id }!!
            game.changeTownSpecialization(aTown.owner!!, aTown.id, Defense())
            // Change back to production
            game.changeTownSpecialization(aTown.owner!!, aTown.id, Production())
            assertThat(aTown.specialization).isInstanceOf(Production::class.java)
        }

        @Test
        fun `Attempting to change the specialization of a town that doesnt exist in the game will result in an exception`() {
            val game = Game.new(twoPlayerList, Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode)
            val aTownThatDoesntExist = Town.new("a town that doesnt exist", 11.0, listOf())
            val aValidUser = game.turn
            assertThrows<CustomException.NotFound.TownNotFoundException> { game.changeTownSpecialization(aValidUser, aTownThatDoesntExist.id, Defense()) }
        }

        @Test
        fun `Attempting to change the specialization by an user that has not the turn will result in an exception`() {
            val game = Game.new(twoPlayerList, Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode)
            val forbiddenUser = game.players.find { it.id != game.turn.id }!!
            val aTown = game.province.towns.filter { it.owner != null }.find { it.owner == forbiddenUser }!!
            assertThrows<CustomException.Forbidden.NotYourTurnException> { game.changeTownSpecialization(forbiddenUser, aTown.id, Defense()) }
        }

        @Test
        fun `Attempting to change the specialization of a town that doesnt belong to the user in turn results in an exception`() {
            val game = Game.new(twoPlayerList, Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode)
            val aValidUser = game.turn
            val notUsersTown = game.province.towns.filter { it.owner != null }.find { it.owner != aValidUser }!!
            assertThrows<CustomException.Forbidden.NotYourTownException> { game.changeTownSpecialization(aValidUser, notUsersTown.id, Defense()) }
        }
    }

    @Nested
    inner class MoveGauchos {
        @Test
        fun `trying to move gauchos from a finished game throws FinishedGameException`() {
            game1.status = Status.FINISHED
            assertThrows<CustomException.Forbidden.FinishedGameException> { game1.moveGauchosBetweenTowns(user1, MovementForm(game1.province.towns[0].id, game1.province.towns[1].id,2)) }
        }

        @Test
        fun `trying to move gauchos from a canceled game throws FinishedGameException`() {
            game1.status = Status.CANCELED
            assertThrows<CustomException.Forbidden.FinishedGameException> { game1.moveGauchosBetweenTowns(user1, MovementForm(game1.province.towns[0].id, game1.province.towns[1].id,2)) }
        }

        @Test
        fun `trying to move gauchos in a game that user doesnt participate throws NotAMemberException`() {
            assertThrows<CustomException.Forbidden.NotAMemberException> { game1.moveGauchosBetweenTowns(user2, MovementForm(game1.province.towns[0].id, game1.province.towns[1].id,2)) }
        }

        @Test
        fun `trying to move gauchos when it is not your turn throws NotYourTurnException`() {
            val game2 = Game.new(listOf(user1, user2), Province("a_province", ArrayList(listOf(town1.copy(), town2.copy()))), normalMode)
            game2.turn = user1
            assertThrows<CustomException.Forbidden.NotYourTurnException> { game2.moveGauchosBetweenTowns(user2, MovementForm(game2.province.towns[0].id, game2.province.towns[1].id,2)) }
        }

        @Test
        fun `successfully moving gauchos doesnt throw an Exception`() {
            game1.turn = user1
            game1.province.towns[0].gauchos = 10
            assertDoesNotThrow { game1.moveGauchosBetweenTowns(user1, MovementForm(game1.province.towns[0].id, game1.province.towns[1].id,1)) }
        }
    }

    @Nested
    inner class AttackTown {
        @Test
        fun `trying to attack town from a finished game throws FinishedGameException`() {
            game1.status = Status.FINISHED
            assertThrows<CustomException.Forbidden.FinishedGameException> { game1.attackTown(user1, AttackForm(game1.province.towns[0].id, game1.province.towns[1].id)) }
        }

        @Test
        fun `trying to attack town from a canceled game throws FinishedGameException`() {
            game1.status = Status.CANCELED
            assertThrows<CustomException.Forbidden.FinishedGameException> { game1.attackTown(user1, AttackForm(game1.province.towns[0].id, game1.province.towns[1].id)) }
        }

        @Test
        fun `trying to attack town in a game that user doesnt participate throws NotAMemberException`() {
            assertThrows<CustomException.Forbidden.NotAMemberException> { game1.attackTown(user2, AttackForm(game1.province.towns[0].id, game1.province.towns[1].id)) }
        }

        @Test
        fun `trying to attack town when it is not your turn throws NotYourTurnException`() {
            val game2 = Game.new(listOf(user1, user2), Province("a_province", ArrayList(listOf(town1.copy(), town2.copy()))), normalMode)
            game2.turn = user1
            assertThrows<CustomException.Forbidden.NotYourTurnException> { game2.attackTown(user2, AttackForm(game2.province.towns[0].id, game2.province.towns[1].id)) }
        }

        @Test
        fun `successfully attacking a town doesnt throw an Exception`() {
            val game2 = Game.new(listOf(user1, user2), Province("a_province", ArrayList(listOf(town1.copy(), town2.copy()))), normalMode)
            game2.turn = user1
            game2.province.towns[0].owner = user1
            game2.province.towns[1].owner = user2
            game2.province.towns[0].gauchos = 10
            assertDoesNotThrow { game2.attackTown(user1, AttackForm(game2.province.towns[0].id, game1.province.towns[1].id)) }
        }
    }

    @Nested
    inner class Surrender {
        @Test
        fun `surrender a game leaving only one player remaining cancels the game`() {
            val twoPlayerGame = Game.new(twoPlayerList, Province("a_province", ArrayList(listOf(town1.copy(), town2.copy()))), normalMode)
            twoPlayerGame.surrender(user1)
            assertThat(twoPlayerGame.status).isEqualTo(Status.CANCELED)
        }

        @Test
        fun `surrender a game leaving more than on players remaining continues the game`() {
            val multiplePlayersGame = Game.new(fourPlayerList, Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy()))), normalMode)
            multiplePlayersGame.surrender(user1)
            assertThat(multiplePlayersGame.status).isEqualTo(Status.ONGOING)
        }

        @Test
        fun `surrender a game leaving more than on players remaining makes the quiting player's towns become neutral`() {
            val multiplePlayersGame = Game.new(fourPlayerList, Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy()))), normalMode)
            val shouldBeNeutralTowns = multiplePlayersGame.province.townsFrom(user1)
            multiplePlayersGame.surrender(user1)
            assertThat(shouldBeNeutralTowns).allMatch { it.owner == null }
        }
    }

    @Nested
    inner class Turn {
        @Test
        fun `trying to finished turn from a finished game throws FinishedGameException`() {
            game1.status = Status.FINISHED
            assertThrows<CustomException.Forbidden.FinishedGameException> { game1.finishTurn(user1) }
        }

        @Test
        fun `trying to finished turn from a canceled game throws FinishedGameException`() {
            game1.status = Status.CANCELED
            assertThrows<CustomException.Forbidden.FinishedGameException> { game1.finishTurn(user1) }
        }

        @Test
        fun `trying to finished turn in a game that user doesnt participate throws NotAMemberException`() {
            assertThrows<CustomException.Forbidden.NotAMemberException> { game1.finishTurn(user2) }
        }

        @Test
        fun `trying to finished turn when it is not your turn throws NotYourTurnException`() {
            val game2 = Game.new(listOf(user1, user2), Province("a_province", ArrayList(listOf(town1.copy(), town2.copy()))), normalMode)
            game2.turn = user1
            assertThrows<CustomException.Forbidden.NotYourTurnException> { game2.finishTurn(user2) }
        }

        @Test
        fun `successfully finish turn unlocks all towns from User`() {
            val game2 = Game.new(listOf(user1, user2), Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode)
            game2.turn = user1
            game2.province.towns.forEach { it.isLocked = true }
            game2.finishTurn(user1)
            assertTrue(game2.province.towns.filter { it.owner == user1}.all { !it.isLocked })
        }

        @Test
        fun `if a user has all towns, he wins and game status changes to FINISHED`() {
            val game2 = Game.new(listOf(user1, user2), Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode)
            game2.turn = user1
            game2.province.towns.forEach { it.owner = user1 }
            game2.finishTurn(user1)
            assertThat(game2.status).isEqualTo(Status.FINISHED)
        }

        @Test
        fun `if a user has all towns from enemy and some towns are still rebel, he wins and game status changes to FINISHED`() {
            val game2 = Game.new(listOf(user1, user2), Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode)
            game2.turn = user1
            game2.province.towns.forEach { it.owner = user1 }
            game2.province.towns[0].owner = null // rebel town
            game2.finishTurn(user1)
            assertThat(game2.status).isEqualTo(Status.FINISHED)
        }

        @Test
        fun `if a user wins, his GamesWonStats are updated`() {
            val game2 = Game.new(listOf(user1, user2), Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode)
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
            val game2 = Game.new(listOf(user1, user2), Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode)
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
            val game2 = Game.new(listOf(user1, user2), Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode)
            game2.turn = user1
            game2.finishTurn(user1)
            assertThat(game2.turn).isEqualTo(user2)
        }

        @Test
        fun `in a 2 player game, finishing turn twice results in the initial player's turn`() {
            val twoPlayerGame = Game.new(listOf(user1, user2), Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy()))), normalMode)
            val initialPlayer = twoPlayerGame.turn
            twoPlayerGame.finishTurn(twoPlayerGame.turn)
            twoPlayerGame.finishTurn(twoPlayerGame.turn)
            assertEquals(initialPlayer, twoPlayerGame.turn)
        }


        @Test
        fun `in a 2 player me, finishing turn three times results in the second player's turn` () {
            val twoPlayerGame = Game.new(listOf(user1, user2), Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy()))), normalMode)
            val initialPlayer = twoPlayerGame.turn
            twoPlayerGame.finishTurn(twoPlayerGame.turn)
            twoPlayerGame.finishTurn(twoPlayerGame.turn)
            twoPlayerGame.finishTurn(twoPlayerGame.turn)
            assertNotEquals(initialPlayer, twoPlayerGame.turn)
        }

        @Test
        fun `if the last player of the first round, finishes his turn and didnt win, turn is changed to first player again`() {
            val user3 = User("user3", "new_mail", "sdaddraf")
            val game2 = Game.new(listOf(user1, user2, user3), Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode)
            val firstPlayer = game2.turn
            game2.finishTurn(firstPlayer)
            game2.finishTurn(game2.turn)
            val lastPlayer = game2.turn
            game2.finishTurn(lastPlayer)
            assertThat(game2.turn).isEqualTo(firstPlayer)
        }

        @Test
        fun `if the turn has changed, the gauchos quantity of the towns of the next player are updated`() {
            val game2 = Game.new(listOf(user1, user2), Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy()))), normalMode)
            game2.turn = user1
            val gauchosQtysOfUser2BeforeHisTurnStarts = game2.province.towns.filter { it.owner == user2 }.map { it.gauchos }
            game2.finishTurn(user1)
            val gauchosQtysOfUser2AfterHisTurnStarts = game2.province.towns.filter { it.owner == user2 }.map { it.gauchos }
            assertThat(gauchosQtysOfUser2BeforeHisTurnStarts).isNotEqualTo(gauchosQtysOfUser2AfterHisTurnStarts)
        }
    }
}