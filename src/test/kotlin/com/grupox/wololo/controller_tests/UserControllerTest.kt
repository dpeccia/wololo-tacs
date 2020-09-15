package com.grupox.wololo.controller_tests

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.repos.RepoUsers
import com.grupox.wololo.model.Stats
import com.grupox.wololo.model.User
import com.grupox.wololo.model.helpers.UserForm
import com.grupox.wololo.services.UsersService
import io.mockk.every
import io.mockk.mockkObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.util.ArrayList

class UserControllerTest {
    val usersService: UsersService = UsersService()

    private val users: ArrayList<User> = arrayListOf(
            User(1,"example_admin", "example_admin", true, Stats(0, 0)),
            User(2,"example_normal_user", "example_normal_user", false, Stats(1, 1)),
            User(3,"example_normal_user2", "example_normal_user2", false, Stats(1, 1))
    )

    @BeforeEach
    fun fixture() {
        mockkObject(RepoUsers)
        every { RepoUsers.getAll() } returns users
    }

    @Nested
    @DisplayName("POST /users/tokens")
    inner class LoginTest {
        @Test
        fun `login with wrong username throws BadLoginException`() {
            assertThrows<CustomException.Unauthorized.BadLoginException>
                {usersService.checkUserCredentials(UserForm("wrong_name", "example_admin"))}
        }

        @Test
        fun `login with wrong password throws BadLoginException`() {
            assertThrows<CustomException.Unauthorized.BadLoginException>
                {usersService.checkUserCredentials(UserForm("example_admin", "wrong_password"))}
        }

        @Test
        fun `successful login doesnt throw an Exception`() {
            assertDoesNotThrow{usersService.checkUserCredentials(UserForm("example_admin", "example_admin"))}
        }
    }

    @Nested
    @DisplayName("GET /users")
    inner class GetUsersTest {
        @Test
        fun `get users returns a list of 2 elements`() {
            assertThat(usersService.getUsers(null)).hasSize(2)
        }

        @Test
        fun `get users returns a list that contains only normal users`() {
            assertThat(usersService.getUsers(null).map{ it.mail }).doesNotContain("example_admin")
        }

        @Test
        fun `get users returns a list that contains example_normal_user`() {
            assertThat(usersService.getUsers(null).map{ it.mail }).contains("example_normal_user")
        }

        @Test
        fun `get a particular user returns a list of 1 element`() {
            assertThat(usersService.getUsers("example_normal_user").map{ it.mail }).hasSize(1)
        }

        @Test
        fun `get example_normal_user returns a list with this element`() {
            assertThat(usersService.getUsers("example_normal_user").map{ it.mail }).contains("example_normal_user")
        }

        @Test
        fun `get a user that doesnt exists throws NotFoundException`() {
            assertThrows<CustomException.NotFound>{usersService.getUsers("example_no_user")}
        }

        @Test
        fun `get an admin user that exists throws NotFoundException`() {
            assertThrows<CustomException.NotFound>{usersService.getUsers("example_admin")}
        }
    }
}