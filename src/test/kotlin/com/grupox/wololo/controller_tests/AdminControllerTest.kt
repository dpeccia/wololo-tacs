package com.grupox.wololo

import com.grupox.wololo.services.AdminControllerService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.*

class AdminControllerTest {
    val adminControllerService: AdminControllerService = AdminControllerService()

    @BeforeEach
    fun fixture() {
    }

    /*@Test
    fun `get scoreboard (user stats)`() {
        assertThat(adminService.getScoreBoard()).isNotEmpty
    }*/


    @Test
    fun `get games by date`() {
        assertThat(adminControllerService.getGamesStats(Date.from(Instant.now()), Date.from(Instant.now().plus(Duration.ofDays(20)))).gamesOnGoing).isEqualTo(2)
    }

}