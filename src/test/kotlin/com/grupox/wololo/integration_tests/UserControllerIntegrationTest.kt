package com.grupox.wololo.integration_tests

import arrow.core.Some
import com.grupox.wololo.model.User
import com.grupox.wololo.model.helpers.JwtSigner
import com.grupox.wololo.model.helpers.LoginForm
import com.grupox.wololo.model.helpers.SHA512Hash
import com.grupox.wololo.model.helpers.getOrThrow
import com.grupox.wololo.model.repos.RepoUsers
import io.mockk.every
import io.mockk.mockkObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {

    @LocalServerPort
    var serverPort: Int? = null

    @Autowired
    lateinit var jwtSigner: JwtSigner

    lateinit var webClient: WebClient

    @Autowired
    val sha512: SHA512Hash = SHA512Hash()

    lateinit var user: User
    lateinit var user2: User
    lateinit var users: ArrayList<User>

    @SpyBean
    lateinit var repoUsers: RepoUsers

    @BeforeEach
    fun fixture() {
        user = User("example_admin", "example_admin", sha512.getSHA512("example_admin"), isAdmin = true)
        user2 = User("example_not_admin", "example_not_admin", sha512.getSHA512("example_not_admin"), isAdmin = false)
        users = arrayListOf(user, user2)

        webClient = WebClient.builder().baseUrl("http://localhost:${serverPort}").build()
        doReturn(users).`when`(repoUsers).findAll()
        doReturn(users.filter { !it.isAdmin }).`when`(repoUsers).findAllByIsAdminFalse()
        doReturn(Optional.of(user)).`when`(repoUsers).findByMailAndPassword("example_admin", sha512.getSHA512("example_admin"))
        doReturn(Optional.empty<User>()).`when`(repoUsers).findByMailAndPassword("example_admin", sha512.getSHA512("wrong_password"))
        doReturn(Optional.empty<User>()).`when`(repoUsers).findByMailAndPassword("wrong_username", sha512.getSHA512("example_admin"))
        doReturn(Optional.of(users[0])).`when`(repoUsers).findByIsAdminTrueAndId(users[0].id)
        doReturn(Optional.empty<User>()).`when`(repoUsers).findByIsAdminTrueAndId(users[1].id)
        doReturn(Optional.of(users[0])).`when`(repoUsers).findByMailAndPassword("example_admin", sha512.getSHA512("example_admin"))
        doReturn(Optional.of(users[1])).`when`(repoUsers).findByMailAndPassword("example_not_admin", sha512.getSHA512("example_not_admin"))
    }

    @Test
    fun `login with wrong username returns UNAUTHORIZED`() {
        val response = webClient.post().uri("/users/tokens")
                .bodyValue(LoginForm("wrong_username", "example_admin")).exchange().block()
        assertThat(response?.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `login with wrong password returns UNAUTHORIZED`() {
        val response = webClient.post().uri("/users/tokens")
                .bodyValue(LoginForm("example_admin", "wrong_password")).exchange().block()
        assertThat(response?.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `successful login returns OK`() {
        val response = webClient.post().uri("/users/tokens")
                .bodyValue(LoginForm("example_admin", "example_admin")).exchange().block()
        assertThat(response?.statusCode()).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `successful login returns JWT cookie`() {
        val response = webClient.post().uri("/users/tokens")
                .bodyValue(LoginForm("example_admin", "example_admin")).exchange().block()
        val jwtToken = response?.cookies()?.get("X-Auth")?.get(0)?.value ?: throw RuntimeException("No JWT Token in response")
        val validation = jwtSigner.validateJwt(Some(jwtToken))
        val id = validation.getOrThrow().body.subject
        assertThat(id).isEqualTo(user.id.toString())
    }

    @Test
    fun `successful logout erases JWT cookie`() {
        val response = webClient.post().uri("/users/tokens")
                .bodyValue(LoginForm("example_admin", "example_admin")).exchange().block()
        val jwtToken = response?.cookies()?.get("X-Auth")?.get(0)?.value ?: throw RuntimeException("No JWT Token in response")
        val validation = jwtSigner.validateJwt(Some(jwtToken))
        val id = validation.getOrThrow().body.subject
        assertThat(id).isEqualTo(user.id.toString())

        val responseCookies = response.cookies()
                .map { it.key to it.value.map { cookie -> cookie.value } }
                .toMap()
        val logoutResponse = webClient.delete().uri("/users/tokens").cookies { it.addAll(LinkedMultiValueMap(responseCookies)) }
                .exchange().block()
        assertThat(logoutResponse?.statusCode()).isEqualTo(HttpStatus.OK)
        assertThat(logoutResponse?.cookies()?.get("X-Auth")?.get(0)?.maxAge).isEqualTo(Duration.ZERO)
    }

    @Test
    fun `cannot obtain users details if not logged in`() {
        val response = webClient.get().uri("/users").exchange().block()
        assertThat(response?.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `can obtain users details when logged in`() {
        val loginResponse = webClient.post().uri("/users/tokens")
                .bodyValue(LoginForm("example_admin", "example_admin")).exchange()
                .block() ?: throw RuntimeException("Should have gotten a response")
        val responseCookies = loginResponse.cookies()
                .map { it.key to it.value.map { cookie -> cookie.value } }
                .toMap()
        val response = webClient.get().uri("/users").cookies { it.addAll(LinkedMultiValueMap(responseCookies)) }
                .exchange().block()
        assertThat(response?.statusCode()).isEqualTo(HttpStatus.OK)
    }

    @Nested
    inner class AdminUsersOperations {
        @Test
        fun `can't get scoreboard when not logged as admin`() {
            val loginResponse = webClient.post().uri("/users/tokens")
                    .bodyValue(LoginForm("example_not_admin", "example_not_admin")).exchange()
                    .block() ?: throw RuntimeException("Should have gotten a response")
            val responseCookies = loginResponse.cookies()
                    .map { it.key to it.value.map { cookie -> cookie.value } }
                    .toMap()

            val response = webClient.get().uri("/users/scoreboard").cookies { it.addAll(LinkedMultiValueMap(responseCookies)) }
                    .exchange().block()

            assertThat(response?.statusCode()).isEqualTo(HttpStatus.FORBIDDEN)
        }

        @Test
        fun `can get scoreboard when logged as admin`() {
            val loginResponse = webClient.post().uri("/users/tokens")
                    .bodyValue(LoginForm("example_admin", "example_admin")).exchange()
                    .block() ?: throw RuntimeException("Should have gotten a response")
            val responseCookies = loginResponse.cookies()
                    .map { it.key to it.value.map { cookie -> cookie.value } }
                    .toMap()

            val response = webClient.get().uri("/users/scoreboard").cookies { it.addAll(LinkedMultiValueMap(responseCookies)) }
                    .exchange().block()

            assertThat(response?.statusCode()).isEqualTo(HttpStatus.OK)
        }
    }

}