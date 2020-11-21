package com.grupox.wololo.controller_tests

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.*
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers
import com.grupox.wololo.services.GamesControllerService
import com.grupox.wololo.services.UsersControllerService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.mockito.Mockito.doReturn
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
    @Autowired
    lateinit var usersControllerService: UsersControllerService

    @SpyBean
    lateinit var repoUsers: RepoUsers
    @SpyBean
    lateinit var repoGames: RepoGames

    private lateinit var user1: User
    private lateinit var user2: User
    private lateinit var user3: User
    private lateinit var userNotInRepo: User
    private lateinit var users: List<User>
    private lateinit var town1: Town
    private lateinit var town2: Town
    private lateinit var town3: Town
    private lateinit var town4: Town
    private lateinit var town5: Town
    private lateinit var town6: Town
    private lateinit var townNotInRepo: Town
    private lateinit var game1: Game
    private lateinit var game2: Game
    private lateinit var game3: Game
    private lateinit var game4: Game
    private lateinit var gameNotInRepo: Game
    private lateinit var games: List<Game>
    private lateinit var normalMode: GameMode


    @BeforeEach
    fun fixture() {
        user1 = User("a_username", "a_mail", "a_password")
        user2 = User("a_username2", "a_mail2", "a_password2")
        user3 = User("a_username3", "a_mail3", "a_password3")
        userNotInRepo = User("other_username", "other_mail", "other_password")
        users = listOf(user1, user2, user3)

        town1 = Town.new("town1", 10.0, listOf("town2"))
        town2 = Town.new("town2", 11.0, listOf("town1"))
        town3 = Town.new("town3", 12.0, listOf("town4"))
        town4 = Town.new("town4", 13.0, listOf("town3"))
        town5 = Town.new("town5", 14.0, listOf("town6"))
        town6 = Town.new("town6", 15.0, listOf("town5"))
        townNotInRepo = Town.new("not in repo", 16.0, listOf("town2"))

        normalMode = GameMode("NORMAL", 10.0, 15.0, 1.25, 1.0)

        game1 = Game.new(listOf(user1, user3), Province("a_province", ArrayList(listOf(town1.copy(), town2.copy()))), normalMode)
        game2 = Game.new(users, Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy()))), normalMode)
        game3 = Game.new(listOf(user1, user2), Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy()))), normalMode)
        game4 = Game.new(listOf(user2, user3), Province("a_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy(), town6.copy()))), normalMode)
        gameNotInRepo = Game.new(listOf(user1, user3), Province("another_province", ArrayList(listOf(town1.copy(), town2.copy(), town3.copy(), town4.copy(), town5.copy(), town6.copy()))), normalMode)
        games = listOf(game2, game3, game1, game4)


        doReturn(games).`when`(repoGames).findAll()
        doReturn(users).`when`(repoUsers).findAllByIsAdminFalse()
        doReturn(users).`when`(repoUsers).findAll()
        doReturn(Optional.of(user1)).`when`(repoUsers).findByIsAdminFalseAndId(user1.id)
        doReturn(Optional.of(user2)).`when`(repoUsers).findByIsAdminFalseAndId(user2.id)
        doReturn(Optional.of(user3)).`when`(repoUsers).findByIsAdminFalseAndId(user3.id)
        doReturn(Optional.of(game1)).`when`(repoGames).findById(game1.id)
        doReturn(Optional.of(game2)).`when`(repoGames).findById(game2.id)
        doReturn(Optional.of(game3)).`when`(repoGames).findById(game3.id)
        doReturn(Optional.of(game4)).`when`(repoGames).findById(game4.id)
        doReturn(game1).`when`(repoGames).save(game1)
        doReturn(game2).`when`(repoGames).save(game2)
        doReturn(user1).`when`(repoUsers).save(user1)
        doReturn(user2).`when`(repoUsers).save(user2)
        doReturn(user3).`when`(repoUsers).save(user3)
        doReturn(games.filter { it.isParticipating(user1) }).`when`(repoGames).findAllByPlayersContains(user1)
        doReturn(games.filter { it.isParticipating(user2) }).`when`(repoGames).findAllByPlayersContains(user2)
        doReturn(Optional.empty<Game>()).`when`(repoGames).findById(gameNotInRepo.id)
        doReturn(Optional.empty<User>()).`when`(repoUsers).findByIsAdminFalseAndId(userNotInRepo.id)
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

        @Test
        fun `A valid turn finish should change the turn to the next player`() {
            val initialPlayer = user3
            game1.turn = initialPlayer
            val gameDTO = gamesControllerService.finishTurn(initialPlayer.id, game1.id)
            assertThat(gameDTO.turnId).isEqualTo(user1.id.toHexString())
        }

        @Test
        fun `in a 2 player game, finishing turn twice results in the initial player's turn`() {
            val twoPlayerGame = game1
            val initialPlayer = twoPlayerGame.turn
            gamesControllerService.finishTurn(twoPlayerGame.turn.id, twoPlayerGame.id)
            val dto = gamesControllerService.finishTurn(twoPlayerGame.turn.id, twoPlayerGame.id)
            assertThat(initialPlayer.id.toHexString()).isEqualTo(dto.turnId)
        }

        @Test
        fun `in a 2 player me, finishing turn three times results in the second player's turn` () {
            val twoPlayerGame = game1
            val initialPlayer = twoPlayerGame.turn
            gamesControllerService.finishTurn(twoPlayerGame.turn.id, twoPlayerGame.id)
            gamesControllerService.finishTurn(twoPlayerGame.turn.id, twoPlayerGame.id)
            val dto = gamesControllerService.finishTurn(twoPlayerGame.turn.id, twoPlayerGame.id)
            assertThat(initialPlayer.id.toHexString()).isNotEqualTo(dto.turnId)
        }
    }

    @Nested
    inner class MoveGauchosBetweenTowns {
        @BeforeEach
        fun fixtureMoveGauchos() {
            game1.turn = user1
            game1.province.towns[0].owner = user1
            game1.province.towns[1].owner = user1
        }

        @Test
        fun `trying to move gauchos in a game that doesnt exist throws GameNotFoundException`() {
            assertThrows<CustomException.NotFound.GameNotFoundException>
            { gamesControllerService.moveGauchosBetweenTowns(user1.id, gameNotInRepo.id, MovementForm(game1.province.towns[0].id, game1.province.towns[1].id,2)) }
        }

        @Test
        fun `trying to move gauchos with a user that doesnt exist throws UserNotFoundException`() {
            assertThrows<CustomException.NotFound.UserNotFoundException>
            { gamesControllerService.moveGauchosBetweenTowns(userNotInRepo.id, game1.id, MovementForm(game1.province.towns[0].id, game1.province.towns[1].id,2)) }
        }

        @Test
        fun `trying to move gauchos with a user that exists and a game that exists doesnt throw an Exception`() {
            assertDoesNotThrow { gamesControllerService.moveGauchosBetweenTowns(user1.id, game1.id, MovementForm(game1.province.towns[0].id, game1.province.towns[1].id,2)) }
        }
    }

    @Nested
    inner class AttackTown {
        @BeforeEach
        fun fixtureAttackTown() {
            game1.turn = user1
            game1.province.towns[0].owner = user1
            game1.province.towns[1].owner = user1
        }

        @Test
        fun `trying to attack town in a game that doesnt exist throws GameNotFoundException`() {
            assertThrows<CustomException.NotFound.GameNotFoundException>
            { gamesControllerService.attackTown(user1.id, gameNotInRepo.id, AttackForm(game1.province.towns[0].id, game1.province.towns[1].id)) }
        }

        @Test
        fun `trying to attack town with a user that doesnt exist throws UserNotFoundException`() {
            assertThrows<CustomException.NotFound.UserNotFoundException>
            { gamesControllerService.attackTown(userNotInRepo.id, game1.id, AttackForm(game1.province.towns[0].id, game1.province.towns[1].id)) }
        }

        @Test
        fun `trying to attack town with a user that exists and a game that exists doesnt throw UserNotFound or GameNotFound`() {
            assertThrows<CustomException.Forbidden.IllegalAttack>
            { gamesControllerService.attackTown(user1.id, game1.id, AttackForm(game1.province.towns[0].id, game1.province.towns[1].id)) }
        }
    }

    @Nested
    inner class Surrender {
        @Test
        fun `surrender in a game with a user that doesnt belongs to the game throws NotAMemberException`() {
            assertThrows<CustomException.Forbidden.NotAMemberException>
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
            assert(gamesControllerService.getTownStats(game3.id, game3.province.towns[0].id).gauchosGeneratedByProduction > 0)
        }

        @Test
        fun `Getting town stats from a game that doesn't exists returns GameNotFound exception `(){
            assertThrows<CustomException.NotFound.GameNotFoundException>{gamesControllerService.getTownStats(gameNotInRepo.id, gameNotInRepo.province.towns[0].id)}
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
            assertThat(ids).isSorted
        }

        @Test
        fun `Get game's result can be sorted by date`() {
            val user = user1
            val sortedGames = gamesControllerService.getGames(user.id, "date", null, null)
            val dates = sortedGames.map { it.date }
            assertThat(dates).isSorted
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
            game1.province.towns[0].owner = user1
            game1.province.towns[1].owner = user2
            game1.finishTurn(user)
            val playerGames = gamesControllerService.getGames(user.id, null, Status.FINISHED, null)
            assertThat(playerGames).allMatch { it.status == Status.FINISHED }
        }

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

    @Nested
    inner class Admin {

        @BeforeEach
        fun fixtureAdmin() {
            user1.stats.gamesWon = 2
            user1.stats.gamesLost = 1

            user2.stats.gamesWon = 4
            user2.stats.gamesLost = 3

            user3.stats.gamesWon = 3
            user3.stats.gamesLost = 2
        }

        @Test
        fun `Scoreboard sorted by games won `() {
            val sortedUsers = usersControllerService.getScoreboard("gamesWon")
            assertThat(sortedUsers).isEqualTo(listOf(user2, user3, user1).map { it.dto() })
        }

        @Test
        fun `Scoreboard sorted by games lost `() {
            val sortedUsers = usersControllerService.getScoreboard("gamesLost")
            assertThat(sortedUsers).isEqualTo(listOf(user2, user3, user1).map { it.dto() })
        }

        @Test
        fun `Gets game stats from a date range`() {
            val filteredGames = gamesControllerService.getGamesInADateRange(Date.from(Instant.now().minus(Duration.ofDays(5))), Date.from(Instant.now().plus(Duration.ofDays(100))))
            assertThat(gamesControllerService.getGamesStats(filteredGames)).isNotNull
        }

        @Test
        fun `Gets all games stats when not specifying a date range`() {
            val filteredGames = gamesControllerService.getAllGamesDTO()
            assertThat(gamesControllerService.getGamesStats(filteredGames)).isEqualTo(gamesControllerService.getGamesStats(games.map{it.dto()}))
        }
    }
}