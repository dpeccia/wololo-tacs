package com.grupox.wololo.controller_tests

import arrow.core.toOption
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.AttackForm
import com.grupox.wololo.model.helpers.DTO
import com.grupox.wololo.model.helpers.MovementForm
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers
import com.grupox.wololo.services.GamesControllerService
import io.mockk.every
import io.mockk.mockkObject
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.*
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import java.time.Duration
import java.time.Instant
import java.util.*

@SpringBootTest
class GamesControllerTest {
    @Autowired
    lateinit var gamesControllerService: GamesControllerService

    @SpyBean
    lateinit var repoUsers: RepoUsers

    val user1: User = User("a_username", "a_mail", "a_password")
    val user2: User = User("a_username2", "a_mail2", "a_password2")
    val user3: User = User("a_username3", "a_mail3", "a_password3")
    val userNotInRepo: User = User("other_username", "other_mail", "other_password")
    val users: List<User> = listOf(user1, user2, user3)

    val town1: Town = Town(name = "town1", elevation = 10.0)
    val town2: Town = Town(name = "town2", elevation = 11.0)
    val town3: Town = Town(name = "town3", elevation = 12.0)
    val town4: Town = Town(name = "town4", elevation = 13.0)
    val town5: Town = Town(name = "town5", elevation = 14.0)
    val town6: Town = Town(name = "town6", elevation = 15.0)
    val townNotInRepo = Town(name = "not in repo", elevation = 15.0)
    val towns: List<Town> = listOf(town1, town2, town3, town4, town5, town6)

    val game1: Game = Game(listOf(user1, user3), Province("a_province", ArrayList(listOf(town1, town2))))
    val game2: Game = Game(users, Province("a_province", ArrayList(listOf(town1, town2, town3, town4))))
    val game3: Game = Game(listOf(user1, user2), Province("a_province", ArrayList(listOf(town1, town2, town3, town4))))
    val game4: Game = Game(listOf(user2, user3), Province("a_province", ArrayList(towns)))
    val gameNotInRepo: Game = Game(listOf(user1, user3), Province("another_province", ArrayList(towns)))
    val games: List<Game> = listOf(game2, game3, game1, game4)

    @BeforeEach
    fun fixture() {
        mockkObject(RepoGames)
        every { RepoGames.getAll() } returns games
        doReturn(users).`when`(repoUsers).findAll()
        doReturn(Optional.of(user1)).`when`(repoUsers).findByIsAdminFalseAndId(user1.id)
        doReturn(Optional.of(user2)).`when`(repoUsers).findByIsAdminFalseAndId(user2.id)
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
            { gamesControllerService.finishTurn(userNotInRepo.id, game1.id) }
        }

        @Test
        fun `trying to finish turn with a user that exists and a game that exists doesnt throw an Exception`() {
            game1.turn = user1
            assertDoesNotThrow { gamesControllerService.finishTurn(user1.id, game1.id) }
        }
    }

    @Nested
    inner class MoveGauchosBetweenTowns {
        @BeforeEach
        fun fixtureMoveGauchos() {
            game1.turn = user1
            town1.owner = user1
            town2.owner = user1
        }

        @Test
        fun `trying to move gauchos in a game that doesnt exist throws GameNotFoundException`() {
            assertThrows<CustomException.NotFound.GameNotFoundException>
            { gamesControllerService.moveGauchosBetweenTowns(user1.id, gameNotInRepo.id, MovementForm(town1.id,town2.id,2)) }
        }

        @Test
        fun `trying to move gauchos with a user that doesnt exist throws UserNotFoundException`() {
            assertThrows<CustomException.NotFound.UserNotFoundException>
            { gamesControllerService.moveGauchosBetweenTowns(userNotInRepo.id, game1.id, MovementForm(town1.id,town2.id,2)) }
        }

        @Test
        fun `trying to move gauchos with a user that exists and a game that exists doesnt throw an Exception`() {
            assertDoesNotThrow { gamesControllerService.moveGauchosBetweenTowns(user1.id, game1.id, MovementForm(town1.id,town2.id,2)) }
        }
    }

    @Nested
    inner class AttackTown {
        @BeforeEach
        fun fixtureAttackTown() {
            game1.turn = user1
            town1.owner = user1
            town2.owner = user1
        }

        @Test
        fun `trying to attack town in a game that doesnt exist throws GameNotFoundException`() {
            assertThrows<CustomException.NotFound.GameNotFoundException>
            { gamesControllerService.attackTown(user1.id, gameNotInRepo.id, AttackForm(town1.id, town2.id)) }
        }

        @Test
        fun `trying to attack town with a user that doesnt exist throws UserNotFoundException`() {
            assertThrows<CustomException.NotFound.UserNotFoundException>
            { gamesControllerService.attackTown(userNotInRepo.id, game1.id, AttackForm(town1.id, town2.id)) }
        }

        @Test
        fun `trying to attack town with a user that exists and a game that exists doesnt throw UserNotFound or GameNotFound`() {
            assertThrows<CustomException.Forbidden.IllegalAttack>
            { gamesControllerService.attackTown(user1.id, game1.id, AttackForm(town1.id, town2.id)) }
        }
    }

    @Nested
    inner class Surrender {
        @Test
        fun `surrender in a game with a user that doesnt belongs to the game throws MemberNotFoundException`() {
            assertThrows<CustomException.NotFound.MemberNotFoundException>
            { assertThat(gamesControllerService.surrender(game4.id, user1.id)) }
        }

        @Test
        fun `surrender in a game that doesn't exists throws GameNotFoundException `() {
            assertThrows<CustomException.NotFound.GameNotFoundException>
            { assertThat(gamesControllerService.surrender(gameNotInRepo.id, user1.id)) }
        }

        @Test
        fun `surrender in a game that exists sums one in games lost quantity `() {
            gamesControllerService.surrender(game2.id, user1.id)
            assertThat(user1.stats.gamesLost > 0)
        }
    }

    @Nested
    inner class TownStats {
        @Test
        fun `Getting gauchos amount generated by production should be more than zero when gauchos are added after a game starts`(){
            assert(gamesControllerService.getTownStats(game3.id,town1.id).gauchosGeneratedByProduction > 0)
        }

        @Test
        fun `Getting town stats from a game that doesn't exists returns GameNotFound exception `(){
            assertThrows<CustomException.NotFound.GameNotFoundException>{gamesControllerService.getTownStats(gameNotInRepo.id,town1.id)}
        }

        @Test
        fun `Getting town stats from a town that doesn't exists returns TownNotFound exception `(){
            assertThrows<CustomException.NotFound.TownNotFoundException>{gamesControllerService.getTownStats(game2.id, townNotInRepo.id)}
        }
    }

    @Nested
    inner class GetGames {
        @Test
        fun `Attempting get games returns all the games in the DB where the player is participating`() {
            val user = user1
            val playerGames = gamesControllerService.getGames(user.id, null, null, null).toSet()
            assertThat(playerGames).isEqualTo(listOf(game1, game2, game3).map { it.dto() }.toSet())
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
        fun `Get game's dto can be filtered by a range of dates`() {
            val gamesDTO: List<DTO.GameDTO> = gamesControllerService.getGamesInADateRange(Date.from(Instant.now().minus(Duration.ofDays(5))), Date.from(Instant.now().plus(Duration.ofDays(20))))
            assertThat(gamesDTO).isEqualTo(games.map { it.dto() })
        }

        @Test
        fun `Get game's result can be sorted by the number of towns`() {
            val user = user1
            val sortedGames = gamesControllerService.getGames(user.id, "numberOfTowns", null, null)
            assertThat(sortedGames).isEqualTo(listOf(game1, game2, game3).map { it.dto() })
        }

        @Test
        fun `Get game's result can be sorted by the number of players`() {
            val user = user1
            val sortedGames = gamesControllerService.getGames(user.id, "numberOfPlayers", null, null)
            assertThat(sortedGames).isEqualTo(listOf(game3, game1, game2).map { it.dto() })
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
            game1.turn = user
            town1.owner = user1
            town2.owner = user2
            game1.finishTurn(user)
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
            val userGame = gamesControllerService.getGame(user.id, game2.id)
            assertThat(userGame).isEqualTo(game2.dto())
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