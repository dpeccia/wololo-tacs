package com.grupox.wololo.integration_tests

import arrow.core.extensions.list.foldable.forAll
import com.grupox.wololo.MockitoHelper
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Game
import com.grupox.wololo.model.Town
import com.grupox.wololo.model.User
import com.grupox.wololo.model.externalservices.ProvincesService
import com.grupox.wololo.model.externalservices.TownGeoJSONProperties
import com.grupox.wololo.model.externalservices.TownGeoJSONWithBordering
import com.grupox.wololo.model.helpers.LoginForm
import com.grupox.wololo.model.helpers.SHA512Hash
import com.grupox.wololo.model.helpers.formatTownName
import com.grupox.wololo.model.helpers.getOrThrow
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers
import com.grupox.wololo.services.GamesControllerService
import io.mockk.every
import io.mockk.mockkObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
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

    lateinit var games: ArrayList<Game>

    @SpyBean
    lateinit var repoUsers: RepoUsers

    @SpyBean
    lateinit var repoGames: RepoGames

    @Autowired
    lateinit var sha512: SHA512Hash

    @Autowired
    lateinit var gamesControllerService: GamesControllerService

    @Autowired
    lateinit var provincesService: ProvincesService

    @BeforeEach
    fun beforeEach() {
        users = arrayListOf(
            User("example_admin", "example_admin",sha512.getSHA512("example_admin"), isAdmin = true),
            User("example_not_admin", "example_not_admin",sha512.getSHA512("example_not_admin"))
        )
        games = arrayListOf()
        webClient = WebClient.builder().baseUrl("http://localhost:${port}").build()
        doReturn(users).`when`(repoUsers).findAll()
        doReturn(games).`when`(repoGames).findAll()
        doReturn(Optional.of(users[0])).`when`(repoUsers).findByIsAdminTrueAndId(users[0].id)
        doReturn(Optional.empty<User>()).`when`(repoUsers).findByIsAdminTrueAndId(users[1].id)
        doReturn(Optional.of(users[0])).`when`(repoUsers).findByMailAndPassword("example_admin", sha512.getSHA512("example_admin"))
        doReturn(Optional.of(users[1])).`when`(repoUsers).findByMailAndPassword("example_not_admin", sha512.getSHA512("example_not_admin"))
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
    inner class TownGeoJSONs {
        var alreadyEvaluatedPairs: List<Pair<String,String>> = listOf()

        @Test
        fun `getRandomBorderingTowns of 9 towns in Buenos Aires returns 9 Towns`() {
            assertThat(provincesService.getRandomBorderingTowns("BUENOS AIRES", 9).getOrThrow().size).isEqualTo(9)
        }

        @Test
        fun `getRandomBorderingTowns twice of the same province returns different towns`() {
            val firstTime = provincesService.getRandomBorderingTowns("BUENOS AIRES", 8).getOrThrow()
            val secondTime = provincesService.getRandomBorderingTowns("BUENOS AIRES", 8).getOrThrow()
            assertThat(firstTime.map { it.town }.sorted()).isNotEqualTo(secondTime.map { it.town }.sorted())
        }

        @Test
        fun `getRandomBorderingTowns of 200 towns in Buenos Aires throws NotEnoughTownsException`() {
            assertThrows<CustomException.BadRequest.NotEnoughTownsException> {
                provincesService.getRandomBorderingTowns("BUENOS AIRES", 200).getOrThrow()
            }
        }

        @Test
        fun `getRandomBorderingTowns returns a list of towns with a path between all of them (because of the bordering)`() {
            val towns = provincesService.getRandomBorderingTowns("BUENOS AIRES", 10).getOrThrow()
            assertTrue(towns.all { hasPath(it, towns) })
        }

        private fun hasPath(town: TownGeoJSONWithBordering, towns: List<TownGeoJSONWithBordering>): Boolean {
            alreadyEvaluatedPairs = listOf()
            return towns.all { hasPath(town, it, towns) }
        }

        private fun hasPath(town1: TownGeoJSONWithBordering, town2: TownGeoJSONWithBordering, towns: List<TownGeoJSONWithBordering>): Boolean {
            alreadyEvaluatedPairs = alreadyEvaluatedPairs + listOf(Pair(town1.town, town2.town))
            return town1 == town2 || town1.borderingTowns.contains(town2.town) ||
                    town1.borderingTowns.any { bt -> !alreadyEvaluatedPairs.contains(Pair(bt, town2.town)) &&
                            hasPath(TownGeoJSONWithBordering(bt, towns.find { it.town == bt }!!.borderingTowns), town2, towns)}
        }

        @Test
        fun `Can get the GeoJSON of a town with accentuation`() {
            val province = "Misiones"
            val town = "Apóstoles"
            val result = gamesControllerService.getTownsGeoJSONs(province, town)
            assertThat(result.any { json -> json.features.any { it.properties.province == "MISIONES" && it.properties.town == "APOSTOLES" } })
        }

        @Test
        fun `Can get the GeoJSON of a town with more than 1 word in their name`() {
            val province = "Misiones"
            val town = "25 de mayo"
            val result = gamesControllerService.getTownsGeoJSONs(province, town)
            assertThat(result.any { json -> json.features.any { it.properties.province == "MISIONES" && it.properties.town == "25 DE MAYO" } })
        }

        @Test
        fun `Can get the GeoJSON of a town with 1 word`() {
            val province = "Misiones"
            val town = "capital"
            val result = gamesControllerService.getTownsGeoJSONs(province, town)
            assertThat(result.any { json -> json.features.any { it.properties.province == "MISIONES" && it.properties.town == "CAPITAL" } })
        }

        @Test
        fun `Can get the GeoJSON of a town with punctuation characters word`() {
            val province = "Buenos Aires"
            val town = "Jose C. Paz"
            val result = gamesControllerService.getTownsGeoJSONs(province, town)
            assertThat(result.any { json -> json.features.any { it.properties.province == "BUENOS AIRES" && it.properties.town == "JOSE C PAZ" } })
        }

        @Test
        fun `Can get the GeoJSON of multiple towns by separating them with a '|'`() {
            val province = "Misiones"
            val town = " capital |  25 de mayo   | apóstoles"  // Arbitrary amount of white spaces
            val result = gamesControllerService.getTownsGeoJSONs(province, town)
            assertThat(result.size == 3)
        }
    }

    @Nested
    inner class AdminGamesOperations {
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

        @Test
        fun `gets all games stats when logged as admin and not specifying a date range`() {
            val loginResponse = webClient.post().uri("/users/tokens")
                    .bodyValue(LoginForm("example_admin", "example_admin")).exchange()
                    .block() ?: throw RuntimeException("Should have gotten a response")
            val responseCookies = loginResponse.cookies()
                    .map { it.key to it.value.map { cookie -> cookie.value } }
                    .toMap()

            val response = webClient.get().uri("/games/stats").cookies { it.addAll(LinkedMultiValueMap(responseCookies)) }
                    .exchange().block()

            assertThat(response?.statusCode()).isEqualTo(HttpStatus.OK)
        }

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