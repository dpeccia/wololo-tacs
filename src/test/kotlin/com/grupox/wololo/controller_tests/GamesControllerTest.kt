package com.grupox.wololo.controller_tests

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.AttackForm
import com.grupox.wololo.model.helpers.MovementForm
import com.grupox.wololo.model.helpers.UserForm
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers
import com.grupox.wololo.services.GamesControllerService
import com.grupox.wololo.services.UsersControllerService
import io.mockk.every
import io.mockk.mockkObject
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.not
import org.junit.jupiter.api.*
import org.mockito.ArgumentMatchers.any
import java.util.ArrayList

class GamesControllerTest {
    val gamesControllerService: GamesControllerService = GamesControllerService()

    val user1: User = User(1,"a_mail", "a_password", false)
    val user_not_in_repo: User = User(2,"other_mail", "other_password", false)
    val users: List<User> = listOf(user1)

    val town1: Town = Town(id = 1, name = "town1", elevation = 10.0)
    val town2: Town = Town(id = 2, name = "town2", elevation = 10.0)
    val towns: List<Town> = listOf(town1, town2)

    val game1: Game = Game(1, listOf(user1), Province(0, "a_province", ArrayList(towns)))
    val game_not_in_repo: Game = Game(2, listOf(user1), Province(1, "another_province", ArrayList(towns)))
    val games: List<Game> = listOf(game1)

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
            { gamesControllerService.finishTurn(user1.id, game_not_in_repo.id) }
        }

        @Test
        fun `trying to finish turn with a user that doesnt exist throws UserNotFoundException`() {
            assertThrows<CustomException.NotFound.UserNotFoundException>
            { gamesControllerService.finishTurn(user_not_in_repo.id, game1.id) }
        }

        @Test
        fun `trying to finish turn with a user that exists and a game that exists doesnt throw an Exception`() {
            assertDoesNotThrow { gamesControllerService.finishTurn(user1.id, game1.id) }
        }
    }

    @Nested
    inner class MoveGauchosBetweenTowns {
        @Test
        fun `trying to move gauchos in a game that doesnt exist throws GameNotFoundException`() {
            assertThrows<CustomException.NotFound.GameNotFoundException>
            { gamesControllerService.moveGauchosBetweenTowns(user1.id, game_not_in_repo.id, MovementForm(1,2,2)) }
        }

        @Test
        fun `trying to move gauchos with a user that doesnt exist throws UserNotFoundException`() {
            assertThrows<CustomException.NotFound.UserNotFoundException>
            { gamesControllerService.moveGauchosBetweenTowns(user_not_in_repo.id, game1.id, MovementForm(1,2,2)) }
        }

        @Test
        fun `trying to move gauchos with a user that exists and a game that exists doesnt throw an Exception`() {
            assertDoesNotThrow { gamesControllerService.moveGauchosBetweenTowns(user1.id, game1.id, MovementForm(1,2,2)) }
        }
    }

    @Nested
    inner class AttackTown {
        @Test
        fun `trying to attack town in a game that doesnt exist throws GameNotFoundException`() {
            assertThrows<CustomException.NotFound.GameNotFoundException>
            { gamesControllerService.attackTown(user1.id, game_not_in_repo.id, AttackForm(1,2)) }
        }

        @Test
        fun `trying to attack town with a user that doesnt exist throws UserNotFoundException`() {
            assertThrows<CustomException.NotFound.UserNotFoundException>
            { gamesControllerService.attackTown(user_not_in_repo.id, game1.id, AttackForm(1,2)) }
        }

        @Test
        fun `trying to attack town with a user that exists and a game that exists doesnt throw UserNotFound or GameNotFound`() {
            assertThrows<CustomException.Forbidden.IllegalAttack>
            { gamesControllerService.attackTown(user1.id, game1.id, AttackForm(1,2)) }
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


//    @Test
//    fun `Change town specialization`() {
//        val gameId = 2
//        val userId = 5
//        val ownedTown = RepoGames.getAll().find { it.id == gameId }?.province?.towns?.find { it.owner?.id == userId }
//        gamesControllerService.updateTownSpecialization(userId,gameId, ownedTown?.id!!, "DEFENSE")  // Por default al comienzo es produccion.
//        assertThat(ownedTown.specialization).isInstanceOf(Defense::class.java)
//    }
}