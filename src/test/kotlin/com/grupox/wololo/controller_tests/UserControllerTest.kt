package com.grupox.wololo.controller_tests

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Stats
import com.grupox.wololo.model.User
import com.grupox.wololo.model.helpers.LoginForm
import com.grupox.wololo.model.helpers.SHA512Hash
import com.grupox.wololo.model.repos.RepoUsers
import com.grupox.wololo.services.UsersControllerService
import io.mockk.every
import io.mockk.mockkObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest
class UserControllerTest {
    @Autowired
    lateinit var usersControllerService: UsersControllerService

    @Autowired
    val sha512: SHA512Hash = SHA512Hash()

    lateinit var users: ArrayList<User>

    @BeforeEach
    fun fixture() {
        users = arrayListOf(User(1, "example_admin", "example_admin", sha512.getSHA512("example_admin"), true, Stats(0, 0)), User(2, "example_normal_user", "example_normal_user",sha512.getSHA512("example_admin"), false, Stats(1, 1)), User(3, "example_normal_user2", "example_normal_user2",sha512.getSHA512("example_admin"), false, Stats(1, 1)))
        mockkObject(RepoUsers)
        every { RepoUsers.getAll() } returns users
    }

    @Nested
    inner class LoginTest {
        @Test
        fun `login with wrong username throws BadLoginException`() {
            assertThrows<CustomException.Unauthorized.BadLoginException>
                {usersControllerService.checkUserCredentials(LoginForm("wrong_name", "example_admin"))}
        }

        @Test
        fun `login with wrong password throws BadLoginException`() {
            assertThrows<CustomException.Unauthorized.BadLoginException>
                {usersControllerService.checkUserCredentials(LoginForm("example_admin", "wrong_password"))}
        }

        @Test
        fun `successful login doesnt throw an Exception`() {
            assertDoesNotThrow{usersControllerService.checkUserCredentials(LoginForm("example_admin", "example_admin"))}
        }
    }

    @Nested
    inner class GetUsersTest {
        @Test
        fun `get users returns a list of 2 elements`() {
            assertThat(usersControllerService.getUsers(null)).hasSize(2)
        }

        @Test
        fun `get users returns a list that contains only normal users`() {
            assertThat(usersControllerService.getUsers(null).map{ it.username }).doesNotContain("example_admin")
        }

        @Test
        fun `get users returns a list that contains example_normal_user`() {
            assertThat(usersControllerService.getUsers(null).map{ it.username }).contains("example_normal_user")
        }

        @Test
        fun `get a particular user returns a list of 1 element`() {
            assertThat(usersControllerService.getUsers("example_normal_user2").map{ it.username }).hasSize(1)
        }

        @Test
        fun `get example_normal_user returns a list with this element`() {
            assertThat(usersControllerService.getUsers("example_normal_user").map{ it.username }).contains("example_normal_user")
        }

        @Test
        fun `get users by a username that doesnt exists returns an empty list`() {
            assertThat(usersControllerService.getUsers("example_no_user")).isEmpty()
        }

        @Test
        fun `get an admin user that exists returns an empty list`() {
            assertThat(usersControllerService.getUsers("example_admin")).isEmpty()
        }
    }
}