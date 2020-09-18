package com.grupox.wololo

import com.grupox.wololo.model.Game
import com.grupox.wololo.model.Province
import com.grupox.wololo.model.Town
import com.grupox.wololo.model.User
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers
import com.grupox.wololo.services.AdminControllerService
import io.mockk.every
import io.mockk.mockkObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.*

class AdminControllerTest {
    val adminControllerService: AdminControllerService = AdminControllerService()

    val user1: User = User(1, "", "a_mail", "a_password", false)
  //  val users: List<User> = listOf(user1)
    val town1: Town = Town(id = 1, name = "town1", elevation = 10.0)
    val towns: List<Town> = listOf(town1)

    val game1: Game = Game(1, listOf(user1), Province(0, "a_province", ArrayList(towns)))
    val game2: Game = Game(2, listOf(user1), Province(1, "a_province", ArrayList(towns)))

    val games: List<Game> = listOf(game1, game2)

    @BeforeEach
    fun fixture() {
        mockkObject(RepoGames)
        every { RepoGames.getAll() } returns games
    }
    /*@Test
    fun `get scoreboard (user stats)`() {
        assertThat(adminService.getScoreBoard()).isNotEmpty
    }*/


    @Test
    fun `get games by date`() {
        assertThat(adminControllerService.getGamesStats(Date.from(Instant.now().minus(Duration.ofDays(5))), Date.from(Instant.now().plus(Duration.ofDays(20)))).gamesOnGoing).isEqualTo(2)
    }

}