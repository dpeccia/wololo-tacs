package com.grupox.wololo.model_tests

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.AttackForm
import com.grupox.wololo.model.helpers.MovementForm
import com.grupox.wololo.model.repos.RepoUsers
import io.mockk.every
import io.mockk.mockkObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProvinceTests {
    val user1: User = User(1, "", "a_mail", "a_password", false)
    val user2: User = User(2, "", "other_mail", "other_password", false)

    val town1: Town = Town(id = 1, name = "town1", elevation = 20.0)
    val town2: Town = Town(id = 2, name = "town2", elevation = 10.0)
    private val towns: List<Town> = listOf(town1, town2)
    private val province = Province(0, "a_province", ArrayList(towns))

    @BeforeEach
    fun fixture() {
        town1.gauchos = 10
        town2.gauchos = 20
    }

    // TODO Tests de movimiento de gauchos
    @Nested
    inner class MoveGauchos {
        @Test
        fun `trying to move gauchos to a Town that doesnt exist throws TownNotFoundException`() {
            assertThrows<CustomException.NotFound.TownNotFoundException>
                { province.moveGauchosBetweenTowns(user1, MovementForm(1, 3,2)) }
        }

        @Test
        fun `trying to move gauchos from a Town that doesnt exist throws TownNotFoundException`() {
            assertThrows<CustomException.NotFound.TownNotFoundException>
                { province.moveGauchosBetweenTowns(user1, MovementForm(3, 1,2)) }
        }

        @Test
        fun `user1 cannot move gauchos from a town to itself`() {
            town1.owner = user1
            assertThrows<CustomException.Forbidden.IllegalGauchoMovement>
            { province.moveGauchosBetweenTowns(user1, MovementForm(1, 1,2)) }
        }

        @Test
        fun `user1 cannot move gauchos from a town that doesnt belong to him`() {
            town1.owner = user2
            town2.owner = user1
            assertThrows<CustomException.Forbidden.IllegalGauchoMovement>
            { province.moveGauchosBetweenTowns(user1, MovementForm(1, 2,2)) }
        }

        @Test
        fun `user1 cannot move gauchos to a town that doesnt belong to him`() {
            town1.owner = user1
            town2.owner = user2
            assertThrows<CustomException.Forbidden.IllegalGauchoMovement>
            { province.moveGauchosBetweenTowns(user1, MovementForm(1, 2,2)) }
        }

        @Test
        fun `user1 cannot move gauchos between towns that belong to an enemy`() {
            town1.owner = user2
            town2.owner = user2
            assertThrows<CustomException.Forbidden.IllegalGauchoMovement>
            { province.moveGauchosBetweenTowns(user1, MovementForm(1, 2,2)) }
        }

        @Test
        fun `user1 cannot move gauchos between towns that doesnt belong to someone`() {
            assertThrows<CustomException.Forbidden.IllegalGauchoMovement>
            { province.moveGauchosBetweenTowns(user1, MovementForm(1, 2,2)) }
        }

        @Test
        fun `user1 cannot move gauchos to a town that is locked`() {
            town1.owner = user1
            town2.owner = user1
            town2.isLocked = true
            assertThrows<CustomException.Forbidden.IllegalGauchoMovement>
            { province.moveGauchosBetweenTowns(user1, MovementForm(1, 2,2)) }
        }

        @Test
        fun `successfully move 3 gauchos from town1 to town2 leaves town1 with 7 gauchos and town2 with 23 gauchos`() {
            town1.owner = user1
            town2.owner = user1
            province.moveGauchosBetweenTowns(user1, MovementForm(1, 2,3))
            assertThat(town1.gauchos).isEqualTo(7)
            assertThat(town2.gauchos).isEqualTo(23)
        }

        @Test
        fun `successfully move gauchos from town1 to town2 leaves town1 unlocked and town2 locked`() {
            town1.owner = user1
            town2.owner = user1
            province.moveGauchosBetweenTowns(user1, MovementForm(1, 2,3))
            assertFalse(town1.isLocked)
            assertTrue(town2.isLocked)
        }
    }

    // TODO Tests de ataque de municipio
    @Nested
    inner class AttackTown {
        @Test
        fun `trying to attack from a Town that doesnt exist throws TownNotFoundException`() {
            assertThrows<CustomException.NotFound.TownNotFoundException> { province.attackTown(user1, AttackForm(3, 1)) }
        }

        @Test
        fun `trying to attack a Town that doesnt exist throws TownNotFoundException`() {
            assertThrows<CustomException.NotFound.TownNotFoundException>
            { province.attackTown(user1, AttackForm(1, 3)) }
        }

        @Test
        fun `user1 cannot attack from a town to itself`() {
            town1.owner = user1
            assertThrows<CustomException.Forbidden.IllegalAttack>
            { province.attackTown(user1, AttackForm(1, 1)) }
        }

        @Test
        fun `user1 cannot attack from a town that doesnt belong to him`() {
            town1.owner = user2
            town2.owner = user2
            assertThrows<CustomException.Forbidden.IllegalAttack>
            { province.attackTown(user1, AttackForm(1, 2)) }
        }

        @Test
        fun `user1 cannot attack to a town that belongs to him`() {
            town1.owner = user1
            town2.owner = user1
            assertThrows<CustomException.Forbidden.IllegalAttack>
            { province.attackTown(user1, AttackForm(1, 2)) }
        }

        @Test
        fun `user1 cannot attack from a town that doesnt belong to someone`() {
            assertThrows<CustomException.Forbidden.IllegalAttack>
            { province.attackTown(user1, AttackForm(1, 2)) }
        }
    }


}