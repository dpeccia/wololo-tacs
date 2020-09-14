package com.grupox.wololo.controller_tests

import com.grupox.wololo.model.*
import com.grupox.wololo.services.GamesService
import com.grupox.wololo.services.UsersService
import io.mockk.every
import io.mockk.mockkObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.ArrayList
import java.util.*

class GamesControllerTest {
    val gamesService: GamesService = GamesService()
    val usersService: UsersService = UsersService()

    //podrían queda acá y los sacamos del repo games
    private val games: ArrayList<Game> = arrayListOf(
            Game(
                    id = 1, date = Date.from(Instant.now()),
                    players = listOf(User(5, "mail", "password", false), User(6, "mail2", "password2", false)),
                    province = Province( id = 1,
                            name = "Santiago del Estero",
                            towns = arrayListOf(Town(1, "Termas de Río Hondo", Coordinates(0f,0f), 0.0, null), Town(2, "La Banda", Coordinates(0f,0f), 0.0, null))
                    ),
                    status= Status.NEW
            ),
                    Game(id= 2, date = Date.from(Instant.now().plus(Duration.ofDays(10))),
                    players = listOf(User(5, "mail", "password", false), User(6, "mail2", "password2", false)),

                    province = Province( id =2,
                            name = "Córdoba",
                            towns = arrayListOf(Town(3, "Cipolletti", Coordinates(0f,0f), 0.0, null), Town(2, "La Banda", Coordinates(0f,0f), 0.0, null))
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