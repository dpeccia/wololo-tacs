package com.grupox.wololo.integration_tests

import com.grupox.wololo.model.User
import com.grupox.wololo.model.helpers.LoginForm
import com.grupox.wololo.model.helpers.SHA512Hash
import com.grupox.wololo.model.repos.RepoUsers
import io.mockk.every
import io.mockk.mockkObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GamesControllerIntegrationTest {
    @LocalServerPort
    var port: Int? = null
    lateinit var webClient: WebClient

    lateinit var users: ArrayList<User>

    @Autowired
    val sha512: SHA512Hash = SHA512Hash()

    @BeforeEach
    fun beforeEach() {
        users = arrayListOf(
            User("example_admin", "example_admin",sha512.getSHA512("example_admin"), isAdmin = true),
            User("example_not_admin", "example_not_admin",sha512.getSHA512("example_not_admin"))
        )
        webClient = WebClient.builder().baseUrl("http://localhost:${port}").build()
        mockkObject(RepoUsers)
        every { RepoUsers.getAll() } returns users
    }

    @Nested
    inner class GetGames {
        @Test
        fun `Attempting to get games without being logged in will result in status code UNAUTHORIZED`() {
            val response = webClient.get().uri("/games").exchange().block()
            assertThat(response?.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
        }

        @Test
        fun `Can get games by previously logging in`() {
            //TODO: not implemented.
        }
    }

    @Nested
    inner class AdminOperations {
        @Test
        fun `can't get games stats when not logged as admin`() {
            val loginResponse = webClient.post().uri("/users/tokens")
                    .bodyValue(LoginForm("example_not_admin", "example_not_admin")).exchange()
                    .block() ?: throw RuntimeException("Should have gotten a response")
            val responseCookies = loginResponse.cookies()
                    .map { it.key to it.value.map { cookie -> cookie.value } }
                    .toMap()

            val response = webClient.get().uri("/games/stats?from=2020/09/25&to=2020/12/25").cookies { it.addAll(LinkedMultiValueMap(responseCookies)) }
                    .exchange().block()

            assertThat(response?.statusCode()).isEqualTo(HttpStatus.FORBIDDEN)
        }

        @Test
        fun `can get games stats when logged as admin`() {
            val loginResponse = webClient.post().uri("/users/tokens")
                    .bodyValue(LoginForm("example_admin", "example_admin")).exchange()
                    .block() ?: throw RuntimeException("Should have gotten a response")
            val responseCookies = loginResponse.cookies()
                    .map { it.key to it.value.map { cookie -> cookie.value } }
                    .toMap()

            val response = webClient.get().uri("/games/stats?from=2020/09/25&to=2020/12/25").cookies { it.addAll(LinkedMultiValueMap(responseCookies)) }
                    .exchange().block()

            assertThat(response?.statusCode()).isEqualTo(HttpStatus.OK)
        }


    }


}