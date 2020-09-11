package com.grupox.wololo

import arrow.core.Some
import arrow.core.getOrHandle
import com.grupox.wololo.model.helpers.JwtSigner
import com.grupox.wololo.model.helpers.UserCredentials
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.springframework.http.HttpStatus
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {

    @LocalServerPort
    var serverPort: Int? = null

    @Autowired
    lateinit var jwtSigner: JwtSigner

    var webClient: WebClient? = null

    @BeforeEach
    fun initialize() {
        webClient = WebClient.builder().baseUrl("http://localhost:${serverPort}").build()
    }

    @Test
    fun `login with wrong username returns UNAUTHORIZED`() {
        val response = webClient!!.post().uri("/users/tokens")
                .bodyValue(UserCredentials("wrong_username", "admin")).exchange().block()
        assertThat(response?.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `login with wrong password returns UNAUTHORIZED`() {
        val response = webClient!!.post().uri("/users/tokens")
                .bodyValue(UserCredentials("admin", "wrong_password")).exchange().block()
        assertThat(response?.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `successful login returns OK`() {
        val response = webClient!!.post().uri("/users/tokens")
                .bodyValue(UserCredentials("admin", "admin")).exchange().block()
        assertThat(response?.statusCode()).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `successful login returns JWT cookie`() {
        val response = webClient!!.post().uri("/users/tokens")
                .bodyValue(UserCredentials("admin", "admin")).exchange().block()
        val jwtToken = response?.cookies()?.get("X-Auth")?.get(0)?.value ?: throw RuntimeException("No JWT Token in response")
        val validation = jwtSigner.validateJwt(Some(jwtToken))
        assertThat(validation.getOrHandle { throw it }.body.subject).isEqualTo("admin")
    }

    @Test
    fun `successful logout erases JWT cookie`() {
        val response = webClient!!.post().uri("/users/tokens")
                .bodyValue(UserCredentials("admin", "admin")).exchange().block()
        val jwtToken = response?.cookies()?.get("X-Auth")?.get(0)?.value ?: throw RuntimeException("No JWT Token in response")
        val validation = jwtSigner.validateJwt(Some(jwtToken))
        assertThat(validation.getOrHandle { throw it }.body.subject).isEqualTo("admin")

        val responseCookies = response.cookies()
                .map { it.key to it.value.map { cookie -> cookie.value } }
                .toMap()
        val logoutResponse = webClient!!.delete().uri("/users/tokens").cookies { it.addAll(LinkedMultiValueMap(responseCookies)) }
                .exchange().block()
        assertThat(logoutResponse?.statusCode()).isEqualTo(HttpStatus.OK)
        assertThat(logoutResponse?.cookies()?.get("X-Auth")?.get(0)?.maxAge).isEqualTo(Duration.ZERO)
    }

    @Test
    fun `sign up allows for subsequent login`() {
        val signupResponse = webClient!!.post().uri("/users")
                .bodyValue(UserCredentials("example_name", "example_password")).exchange().block()
        assertThat(signupResponse?.statusCode()).isEqualTo(HttpStatus.OK)

        val loginResponse = webClient!!.post().uri("/users/tokens")
                .bodyValue(UserCredentials("example_name", "example_password")).exchange().block()
        assertThat(loginResponse?.statusCode()).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `cannot obtain users details if not logged in`() {
        val response = webClient!!.get().uri("/users").exchange().block()
        assertThat(response?.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `can obtain users details when logged in`() {
        val loginResponse = webClient!!.post().uri("/users/tokens")
                .bodyValue(UserCredentials("admin", "admin")).exchange()
                .block() ?: throw RuntimeException("Should have gotten a response")
        val responseCookies = loginResponse.cookies()
                .map { it.key to it.value.map { cookie -> cookie.value } }
                .toMap()
        val response = webClient!!.get().uri("/users").cookies { it.addAll(LinkedMultiValueMap(responseCookies)) }
                .exchange().block()
        assertThat(response?.statusCode()).isEqualTo(HttpStatus.OK)
    }
}