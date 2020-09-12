package com.grupox.wololo.integration_tests

import arrow.core.Some
import arrow.core.getOrHandle
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.helpers.JwtSigner
import com.grupox.wololo.model.helpers.TownForm
import com.grupox.wololo.model.helpers.UserCredentials
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import arrow.core.*
import org.springframework.http.ResponseCookie
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GamesControllerIntegrationTest {

    @LocalServerPort
    var serverPort: Int? = null

    @Autowired
    lateinit var jwtSigner: JwtSigner

    var webClient: WebClient? = null

    @BeforeEach
    fun fixture() {
        webClient = WebClient.builder().baseUrl("http://localhost:${serverPort}").build()
    }

    @Test
    fun `can change town specialization when logged in`() {

        val loginResponse = webClient!!.post().uri("/users/tokens")
                .bodyValue(UserCredentials("admin", "admin")).exchange()
                .block() ?: throw RuntimeException("Should have gotten a response")
        val responseCookies = loginResponse.cookies()
                .map { it.key to it.value.map { cookie -> cookie.value } }
                .toMap()

            val response = webClient!!.put().uri("/games/2/towns/3").bodyValue(TownForm("PRODUCTION")).cookies { it.addAll(LinkedMultiValueMap(responseCookies)) }
                    .exchange().block()
            assertThat(response?.statusCode()).isEqualTo(HttpStatus.OK)

    }



}