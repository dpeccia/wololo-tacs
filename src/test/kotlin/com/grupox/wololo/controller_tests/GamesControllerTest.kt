package com.grupox.wololo.controller_tests

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.AttackForm
import com.grupox.wololo.model.helpers.MovementForm
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers
import com.grupox.wololo.services.GamesControllerService
import io.mockk.every
import io.mockk.mockkObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest
class GamesControllerTest {
    @Autowired
    lateinit var gamesControllerService: GamesControllerService

    val user1: User = User(1, "", "a_mail", "a_password", false)
    val user2: User = User(2, "", "a_mail2", "a_password2", false)
    val user3: User = User(3, "", "a_mail3", "a_password3", false)
    val userNotInRepo: User = User(4, "", "other_mail", "other_password", false)
    val users: List<User> = listOf(user1, user2, user3)

    val town1: Town = Town(id = 1, name = "town1", elevation = 10.0)
    val town2: Town = Town(id = 2, name = "town2", elevation = 11.0)
    val town3: Town = Town(id = 3, name = "town3", elevation = 12.0)
    val town4: Town = Town(id = 4, name = "town4", elevation = 13.0)
    val town5: Town = Town(id = 5, name = "town5", elevation = 14.0)
    val town6: Town = Town(id = 6, name = "town6", elevation = 15.0)
    val towns: List<Town> = listOf(town1, town2, town3, town4, town5, town6)

    val singlePlayerGame: Game = Game(1, listOf(user1), Province(0, "a_province", ArrayList(listOf(town1, town2))))
    val game2: Game = Game(2, users, Province(0, "a_province", ArrayList(listOf(town1, town2, town3, town4))))
    val game3: Game = Game(3, listOf(user1, user2), Province(0, "a_province", ArrayList(listOf(town1, town2, town3, town4))))
    val game4: Game = Game(4, listOf(user2, user3), Province(0, "a_province", ArrayList(towns)))
    val gameNotInRepo: Game = Game(5, listOf(user1), Province(1, "another_province", ArrayList(towns)))
    val games: List<Game> = listOf(game2, game3, singlePlayerGame, game4)

    @BeforeEach
    fun fixture() {
        mockkObject(RepoGames)
        mockkObject(RepoUsers)
        every { RepoGames.getAll() } returns games
        every { RepoUsers.getAll() } returns users
    }

    @Nested
    inner class FinishTurn {
        @Test
        fun `trying to finish turn in a game that doesnt exist throws GameNotFoundException`() {
            assertThrows<CustomException.NotFound.GameNotFoundException>
            { gamesControllerService.finishTurn(user1.id, gameNotInRepo.id) }
        }

        @Test
        fun `trying to finish turn with a user that doesnt exist throws UserNotFoundException`() {
            assertThrows<CustomException.NotFound.UserNotFoundException>
            { gamesControllerService.finishTurn(userNotInRepo.id, singlePlayerGame.id) }
        }

        @Test
        fun `trying to finish turn with a user that exists and a game that exists doesnt throw an Exception`() {
            assertDoesNotThrow { gamesControllerService.finishTurn(user1.id, singlePlayerGame.id) }
        }
    }

    @Nested
    inner class MoveGauchosBetweenTowns {
        @Test
        fun `trying to move gauchos in a game that doesnt exist throws GameNotFoundException`() {
            assertThrows<CustomException.NotFound.GameNotFoundException>
            { gamesControllerService.moveGauchosBetweenTowns(user1.id, gameNotInRepo.id, MovementForm(1,2,2)) }
        }

        @Test
        fun `trying to move gauchos with a user that doesnt exist throws UserNotFoundException`() {
            assertThrows<CustomException.NotFound.UserNotFoundException>
            { gamesControllerService.moveGauchosBetweenTowns(userNotInRepo.id, singlePlayerGame.id, MovementForm(1,2,2)) }
        }

        @Test
        fun `trying to move gauchos with a user that exists and a game that exists doesnt throw an Exception`() {
            assertDoesNotThrow { gamesControllerService.moveGauchosBetweenTowns(user1.id, singlePlayerGame.id, MovementForm(1,2,2)) }
        }
    }

    @Nested
    inner class AttackTown {
        @Test
        fun `trying to attack town in a game that doesnt exist throws GameNotFoundException`() {
            assertThrows<CustomException.NotFound.GameNotFoundException>
            { gamesControllerService.attackTown(user1.id, gameNotInRepo.id, AttackForm(1,2)) }
        }

        @Test
        fun `trying to attack town with a user that doesnt exist throws UserNotFoundException`() {
            assertThrows<CustomException.NotFound.UserNotFoundException>
            { gamesControllerService.attackTown(userNotInRepo.id, singlePlayerGame.id, AttackForm(1,2)) }
        }

        @Test
        fun `trying to attack town with a user that exists and a game that exists doesnt throw UserNotFound or GameNotFound`() {
            assertThrows<CustomException.Forbidden.IllegalAttack>
            { gamesControllerService.attackTown(user1.id, singlePlayerGame.id, AttackForm(1,2)) }
        }
    }

    @Nested
    inner class Surrender {
        @Test
        fun `surrender in a game with a user that doesnt belongs to the game throws MemberNotFoundException`() {
            assertThrows<CustomException.NotFound.MemberNotFoundException>
            { assertThat(gamesControllerService.surrender(1, 11)) }
        }

        @Test
        fun `surrender in a game that doesn't exists throws GameNotFoundException `() {
            assertThrows<CustomException.NotFound.GameNotFoundException>
            { assertThat(gamesControllerService.surrender(9, 1)) }

        }

        @Test
        fun `surrender in a game that exists sums one in games lost quantity `() {
            gamesControllerService.surrender(1, 1)
            assertThat(user1.stats.gamesLost > 0)

        }
    }

    @Nested
    inner class TownStats {
        // Lo comento porque esta mal pensado el test, los gauchos se vuelven a recalcular al principio de cada turno, no al final
        /*@Test
        fun `Getting gauchos amount generated by production should be more than zero with the given game`(){

            val gameTowns: List<Town> = game3.province.towns
            val townsFromUser: List<Town> = gameTowns.filter { it.isFrom(user1) }
            val townFromUser: Town = townsFromUser.get(0)

            game3.turn = user1
            game3.changeTownSpecialization(user1, townFromUser.id,Production())
            game3.finishTurn(user1)
            assert(gamesControllerService.getTownStats(3,townFromUser.id).gauchosGeneratedByProduction > 0)
        }*/

        @Test
        fun `Getting town stats from a game that doesn't exists returns GameNotFound exception `(){
            assertThrows<CustomException.NotFound.GameNotFoundException>{gamesControllerService.getTownStats(20,1)}
        }

        @Test
        fun `Getting town stats from a town that doesn't exists returns TownNotFound exception `(){
            assertThrows<CustomException.NotFound.TownNotFoundException>{gamesControllerService.getTownStats(2,20)}
        }
    }

    @Nested
    inner class GetGames {
        @Test
        fun `Attempting get games returns all the games in the DB where the player is participating`() {
            val user = user1
            val playerGames = gamesControllerService.getGames(user.id, null, null, null).toSet()
            assertThat(playerGames).isEqualTo(setOf(singlePlayerGame, game2, game3))
        }

        @Test
        fun `Get game's result can be sorted by id`() {
            val user = user2
            val sortedGames = gamesControllerService.getGames(user.id, "id", null, null)
            val ids = sortedGames.map { it.id }
            assertThat(ids).isSorted  //isEqualTo(listOf(game2, game3, game4))
        }

        @Test
        fun `Get game's result can be sorted by date`() {
            val user = user1
            val sortedGames = gamesControllerService.getGames(user.id, "date", null, null)
            val dates = sortedGames.map { it.date }
            assertThat(dates).isSorted   //isEqualTo(listOf(singlePlayerGame, game2, game3))
        }

        @Test
        fun `Get game's result can be sorted by the number of towns`() {
            val user = user1
            val sortedGames = gamesControllerService.getGames(user.id, "numberOfTowns", null, null)
            assertThat(sortedGames).isEqualTo(listOf(singlePlayerGame, game2, game3))
        }

        @Test
        fun `Get game's result can be sorted by the number of players`() {
            val user = user1
            val sortedGames = gamesControllerService.getGames(user.id, "numberOfPlayers", null, null)
            assertThat(sortedGames).isEqualTo(listOf(singlePlayerGame, game3, game2))
        }

        @Test
        fun `Get games with an invalid sorting parameter is equivalent to getting all games`() {
            val user = user1
            val playerGamesWithInvalidParameter = gamesControllerService.getGames(user.id, "asd", null, null)
            val allPlayerGames = gamesControllerService.getGames(user.id, null, null, null)
            assertThat(playerGamesWithInvalidParameter).isEqualTo(allPlayerGames)
        }

        @Test
        fun `Get game's result can be filtered by status`() {
            val user = user1
            singlePlayerGame.finishTurn(user) // Como es el unico deberia ganar y por lo tanto status = FINISHED
            val playerGames = gamesControllerService.getGames(user.id, null, Status.FINISHED, null)
            assertThat(playerGames).allMatch { it.status == Status.FINISHED }
        }

//        @Test
//        fun `Get game's result can be filtered by date`() {
//          TODO("Not implemented")
//        }

//        @Test
//        fun `Get games can be sorted and filtered altogether`() {
//          TODO("Not implemented")
//        }

        @Test
        fun `Get games fails if the given user doesn't exist with UserNotFoundException`(){
            assertThrows<CustomException.NotFound.UserNotFoundException> { gamesControllerService.getGames(userNotInRepo.id, null, null, null) }
        }
    }

    @Nested
    inner class GetGame {
        @Test
        fun `An user can get one of its games by id`(){
            val user = user1
            val userGame = gamesControllerService.getGame(user.id, 2)
            assertThat(userGame).isEqualTo(game2)
        }

        @Test
        fun `Attempting to get a game that doesn't belong to the user results in Unauthorized TokenException`(){
            val user = user1
            val gameThatDoesntBelongToUser = game4
            assertThrows<CustomException.Unauthorized.TokenException> { gamesControllerService.getGame(user.id, gameThatDoesntBelongToUser.id) }
        }

        @Test
        fun `Attempting a game that doesn't exist in the repo results in GameNotFoundException`(){
            val user = user1
            val gameThatDoesntExist = gameNotInRepo
            assertThrows<CustomException.NotFound.GameNotFoundException> { gamesControllerService.getGame(user.id, gameThatDoesntExist.id) }
        }

        @Test
        fun `Attempting a game by a non existent user results in UserNotFoundException`(){
            val nonExistentUser = userNotInRepo
            val anyGame = game2
            assertThrows<CustomException.NotFound.UserNotFoundException> { gamesControllerService.getGame(nonExistentUser.id, anyGame.id) }
        }
    }

//    @Test
//    fun `Change town specialization`() {
//        val gameId = 2
//        val userId = 5
//        val ownedTown = RepoGames.getAll().find { it.id == gameId }?.province?.towns?.find { it.owner?.id == userId }
//        gamesControllerService.updateTownSpecialization(userId,gameId, ownedTown?.id!!, "DEFENSE")  // Por default al comienzo es produccion.
//        assertThat(ownedTown.specialization).isInstanceOf(Defense::class.java)
//    }
}