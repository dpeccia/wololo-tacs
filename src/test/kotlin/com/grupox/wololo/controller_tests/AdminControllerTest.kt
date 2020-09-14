package com.grupox.wololo

import com.grupox.wololo.services.AdminService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.*

class AdminControllerTest {

    val adminService: AdminService = AdminService()

    @BeforeEach
    fun fixture() {
    }

    /*@Test
    fun `get scoreboard (user stats)`() {
        assertThat(adminService.getScoreBoard()).isNotEmpty
    }*/

    @Test
    fun `get games by date`() {
        assertThat(adminService.getGamesStats(Date.from(Instant.now().minus(Duration.ofDays(5))), Date.from(Instant.now().plus(Duration.ofDays(20)))).gamesNew).isEqualTo(1)
    }

}