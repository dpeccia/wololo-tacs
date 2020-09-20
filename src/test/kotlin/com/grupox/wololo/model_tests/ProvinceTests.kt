package com.grupox.wololo.model_tests

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.AttackForm
import com.grupox.wololo.model.helpers.MovementForm
import com.grupox.wololo.model.repos.RepoUsers
import io.mockk.every
import io.mockk.mockkObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.function.Executable
import kotlin.math.round

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
        private lateinit var jujuy: Province
        private lateinit var yavi: Town
        private lateinit var elCondor: Town
        private lateinit var cangrejillos: Town
        private lateinit var abraPampa: Town

        @BeforeEach
        fun fixture() {
            yavi = Town(10, "Yavi", Coordinates((-65.3412864273208).toFloat(), (-22.1949119799291).toFloat()), 3485.0263671875)
            elCondor = Town(11, "El Cóndor", Coordinates((-65.3795310298623).toFloat(), (-22.414030404424).toFloat()), 3609.618408203125)
            cangrejillos = Town(12, "Cangrejillos", Coordinates((-65.5405751330491).toFloat(), (-22.4438999595476).toFloat()), 3617.323974609375)
            abraPampa = Town(13, "Abra Pampa", Coordinates((-66.0322682108588).toFloat(), (-22.8431449411788).toFloat()), 3519.69287109375)
            yavi.owner = user1
            elCondor.owner = user1
            cangrejillos.owner = user2
            jujuy = Province(10, "Jujuy", arrayListOf(yavi, elCondor, cangrejillos, abraPampa))
        }

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

        @Test
        fun `maxAltitude of Jujuy is the elevation of Cangrejillos`() {
            assertThat(jujuy.maxAltitude).isEqualTo(cangrejillos.elevation)
        }

        @Test
        fun `minAltitude of Jujuy is the elevation of Yavi`() {
            assertThat(jujuy.minAltitude).isEqualTo(yavi.elevation)
        }

        @Test
        fun `maxDistance in Jujuy is the distance between Yavi and Abra Pampa`() { // checked with Google Maps manually
            assertThat(jujuy.maxDistance).isEqualTo(jujuy.distanceBetween(yavi, abraPampa))
            assertThat(round(jujuy.maxDistance)).isEqualTo(101158)
        }

        @Test
        fun `minDistance in Jujuy is the distance between El Condor and Cangrejillos`() { // checked with Google Maps manually
            assertThat(jujuy.minDistance).isEqualTo(jujuy.distanceBetween(elCondor, cangrejillos))
            assertThat(round(jujuy.minDistance)).isEqualTo(16882)
        }

        @Test
        fun `attack from Yavi to Abra Pampa (rebel town) with 5 vs 4 gauchos leaves Yavi with 0 gauchos and Abra Pampa with 2`() {
            yavi.gauchos = 5
            abraPampa.gauchos = 4
            jujuy.attackTown(user1, AttackForm(yavi.id, abraPampa.id))
            assertAll(
                    Executable { assertThat(yavi.owner).isEqualTo(user1) },
                    Executable { assertThat(abraPampa.owner).isNull() },
                    Executable { assertThat(yavi.gauchos).isEqualTo(0) },
                    Executable { assertThat(abraPampa.gauchos).isEqualTo(2) }
            )
        }
    }


}