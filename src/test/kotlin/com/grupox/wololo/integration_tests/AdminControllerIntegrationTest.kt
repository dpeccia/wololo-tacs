package com.grupox.wololo.integration_tests

import arrow.core.Some
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Stats
import com.grupox.wololo.model.User

import com.grupox.wololo.model.helpers.*

import com.grupox.wololo.model.helpers.JwtSigner
import com.grupox.wololo.model.helpers.LoginForm
import com.grupox.wololo.model.helpers.getOrThrow

import com.grupox.wololo.model.repos.RepoUsers
import com.grupox.wololo.services.AdminControllerService
import io.mockk.every
import io.mockk.mockkObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

import org.springframework.http.HttpStatus
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminControllerIntegrationTest {
    @LocalServerPort
    var port: Int? = null
    lateinit var webClient: WebClient

    lateinit var adminUser: User
    lateinit var notAdminUser: User
    lateinit var users: ArrayList<User>

    @Autowired
    val sha512: SHA512Hash = SHA512Hash()

    @Autowired
    lateinit var adminControllerService: AdminControllerService

    @BeforeEach
    fun beforeEach() {
        webClient = WebClient.builder().baseUrl("http://localhost:${port}").build()
    }

    @BeforeEach
    fun fixture() {

        adminUser = User(1, "", "example_admin",sha512.getSHA512("example_admin"), true, Stats(0, 0))
        notAdminUser = User(1, "", "example_not_admin",sha512.getSHA512("example_not_admin"), false, Stats(0, 0))
        users = arrayListOf(notAdminUser, adminUser)

        webClient = WebClient.builder().baseUrl("http://localhost:${port}").build()

        mockkObject(RepoUsers)
        every { RepoUsers.getAll() } returns users
    }


    @Test
    fun `can't obtain users details when logged in`() {
        val loginResponse = webClient.post().uri("/users/tokens")
                .bodyValue(LoginForm("example_not_admin", "example_not_admin")).exchange()
                .block() ?: throw RuntimeException("Should have gotten a response")
        val responseCookies = loginResponse.cookies()
                .map { it.key to it.value.map { cookie -> cookie.value } }
                .toMap()

//        assertThrows<CustomException.Unauthorized.OperationNotAuthorized> { adminControllerService. }

        val response = webClient.get().uri("/admin/scoreboard").cookies { it.addAll(LinkedMultiValueMap(responseCookies)) }
                .exchange().block()



        assertThat(response?.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
    }


}