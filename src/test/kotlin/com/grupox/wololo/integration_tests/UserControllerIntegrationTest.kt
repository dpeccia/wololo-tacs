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
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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
    lateinit var users: ArrayList<User>

    @BeforeEach
    fun fixture() {
        user = User("example_admin", "example_admin", sha512.getSHA512("example_admin"), isAdmin = true)
        users = arrayListOf(user)
        webClient = WebClient.builder().baseUrl("http://localhost:${serverPort}").build()
        mockkObject(RepoUsers)
        every { RepoUsers.getAll() } returns users
    }
/*
    @BeforeAll
    fun initUsers() {
        user1 = User(1, "", "example_admin",usersControllerService.hashPassword("example_admin"), true, Stats(0, 0))
        users = arrayListOf(user1)
        //     users= arrayListOf(User(1, "", "example_admin", usersControllerService.hashPassword("example_admin"), true, Stats(0, 0)))
    }
*/
    @Test
    fun `login with wrong username returns UNAUTHORIZED`() {
        val response = webClient.post().uri("/users/tokens")
                .bodyValue(LoginForm("wrong_username", "example_admin")).exchange().block()
    println(sha512.getSHA512("admin"))
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
        val id = UUID.fromString(validation.getOrThrow().body.subject)
        assertThat(id).isEqualTo(user.id)
    }

    @Test
    fun `successful logout erases JWT cookie`() {
        val response = webClient.post().uri("/users/tokens")
                .bodyValue(LoginForm("example_admin", "example_admin")).exchange().block()
        val jwtToken = response?.cookies()?.get("X-Auth")?.get(0)?.value ?: throw RuntimeException("No JWT Token in response")
        val validation = jwtSigner.validateJwt(Some(jwtToken))
        val id = UUID.fromString(validation.getOrThrow().body.subject)
        assertThat(id).isEqualTo(user.id)

        val responseCookies = response.cookies()
                .map { it.key to it.value.map { cookie -> cookie.value } }
                .toMap()
        val logoutResponse = webClient.delete().uri("/users/tokens").cookies { it.addAll(LinkedMultiValueMap(responseCookies)) }
                .exchange().block()
        assertThat(logoutResponse?.statusCode()).isEqualTo(HttpStatus.OK)
        assertThat(logoutResponse?.cookies()?.get("X-Auth")?.get(0)?.maxAge).isEqualTo(Duration.ZERO)
    }

    // TODO arreglar test (que insertUser sea a un mock
    /*@Test
    fun `sign up allows for subsequent login`() {
        val signupResponse = webClient!!.post().uri("/users")
                .bodyValue(UserCredentials("example_name", "example_password")).exchange().block()
        assertThat(signupResponse?.statusCode()).isEqualTo(HttpStatus.OK)

        val loginResponse = webClient!!.post().uri("/users/tokens")
                .bodyValue(UserCredentials("example_name", "example_password")).exchange().block()
        assertThat(loginResponse?.statusCode()).isEqualTo(HttpStatus.OK)
    }*/

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

}