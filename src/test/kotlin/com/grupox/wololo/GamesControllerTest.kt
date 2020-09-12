package com.grupox.wololo

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.services.GamesService
import com.grupox.wololo.services.UserService
import io.mockk.every
import io.mockk.mockkObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.ArrayList

class GamesControllerTest {
    val gamesService: GamesService = GamesService()
    val userService: UserService = UserService()

    //podrían queda acá y los sacamos del repo games
    private val games: ArrayList<Game> = arrayListOf(
            Game(
                    id = 1,
                    province = Province(
                            id = 1,
                            name = "Santiago del Estero",
                            towns = arrayListOf(Town(1, "Termas de Río Hondo", Coordinates(0f,0f), Production(), TownStats()), Town(2, "La Banda", Coordinates(0f,0f), Defense(), TownStats()))
                    ),
                    status= Status.NEW
            ),
            Game(
                    id= 2,
                    province = Province(
                            id = 3,
                            name = "Córdoba",
                            towns = arrayListOf(Town(3, "Cipolletti", Coordinates(0f,0f), Production(), TownStats()))
                    ),
                    status = Status.FINISHED
            )
    )

    private val users: ArrayList<User> = arrayListOf(
            User(1,"example_admin", "example_admin", true, Stats(0, 0)),
            User(2,"example_normal_user", "example_normal_user", false, Stats(1, 1)),
            User(3,"example_normal_user2", "example_normal_user2", false, Stats(1, 1))
    )

    val participantIDs : List<Int> = arrayListOf(2,3)

    @BeforeEach
    fun fixture() {
        mockkObject(RepoUsers)
        mockkObject(RepoGames)
        every { RepoUsers.getUsers() } returns users
        every { RepoGames.getGames() } returns games
    }

    @Test
    fun `surrender in a game`() {
        assertThat(gamesService.surrender(1, participantIDs, "example_normal_user")).isEqualTo(2)
    }
    @Test
    fun `Change town specialization`() {
        assertThat(gamesService.changeSpecialization("PRODUCTION",2, 3))
    }
}