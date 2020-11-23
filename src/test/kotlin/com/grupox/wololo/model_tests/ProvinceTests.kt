package com.grupox.wololo.model_tests

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.AttackForm
import com.grupox.wololo.model.helpers.GameMode
import com.grupox.wololo.model.helpers.MovementForm
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.math.round

class ProvinceTests {

    private lateinit var user1: User
    private lateinit var user2: User
    private lateinit var town1: Town
    private lateinit var town2: Town
    private lateinit var townThatDoesntExists: Town
    private lateinit var towns: List<Town>
    private lateinit var province: Province
    private lateinit var normalMode: GameMode

    @BeforeEach
    fun fixture() {
        user1 = User("a_user", "a_mail", "a_password")
        user2 = User("other_user", "other_mail", "other_password")
        town1 = Town.new("town1", 20.0, listOf("town2"))
        town2 = Town.new("town2", 10.0, listOf("town1"))
        townThatDoesntExists = Town.new("town that doesn't exist", 11.0, listOf())
        town1.gauchos = 10
        town2.gauchos = 20
        towns = listOf(town1, town2)
        province = Province("a_province", ArrayList(towns))
        normalMode = GameMode("NORMAL", 10.0, 15.0, 1.25, 1.0)

    }

    @Nested
    inner class MoveGauchos {
        @Test
        fun `trying to move gauchos to a Town that doesnt exist throws TownNotFoundException`() {
            assertThrows<CustomException.NotFound.TownNotFoundException>
                { province.moveGauchosBetweenTowns(user1, MovementForm(town1.id, townThatDoesntExists.id,2)) }
        }

        @Test
        fun `trying to move gauchos from a Town that doesnt exist throws TownNotFoundException`() {
            assertThrows<CustomException.NotFound.TownNotFoundException>
                { province.moveGauchosBetweenTowns(user1, MovementForm(townThatDoesntExists.id, town1.id,2)) }
        }

        @Test
        fun `user1 cannot move gauchos from a town to itself`() {
            town1.owner = user1
            assertThrows<CustomException.Forbidden.IllegalGauchoMovement>
            { province.moveGauchosBetweenTowns(user1, MovementForm(town1.id, town1.id,2)) }
        }

        @Test
        fun `user1 cannot move gauchos from a town that doesnt belong to him`() {
            town1.owner = user2
            town2.owner = user1
            assertThrows<CustomException.Forbidden.IllegalGauchoMovement>
            { province.moveGauchosBetweenTowns(user1, MovementForm(town1.id, town2.id,2)) }
        }

        @Test
        fun `user1 cannot move gauchos to a town that doesnt belong to him`() {
            town1.owner = user1
            town2.owner = user2
            assertThrows<CustomException.Forbidden.IllegalGauchoMovement>
            { province.moveGauchosBetweenTowns(user1, MovementForm(town1.id, town2.id,2)) }
        }

        @Test
        fun `user1 cannot move gauchos between towns that belong to an enemy`() {
            town1.owner = user2
            town2.owner = user2
            assertThrows<CustomException.Forbidden.IllegalGauchoMovement>
            { province.moveGauchosBetweenTowns(user1, MovementForm(town1.id, town2.id,2)) }
        }

        @Test
        fun `user1 cannot move gauchos between towns that doesnt belong to someone`() {
            assertThrows<CustomException.Forbidden.IllegalGauchoMovement>
            { province.moveGauchosBetweenTowns(user1, MovementForm(town1.id, town2.id,2)) }
        }

        @Test
        fun `user1 cannot move gauchos to a town that is locked`() {
            town1.owner = user1
            town2.owner = user1
            town2.isLocked = true
            assertThrows<CustomException.Forbidden.IllegalGauchoMovement>
            { province.moveGauchosBetweenTowns(user1, MovementForm(town1.id, town2.id,2)) }
        }

        @Test
        fun `user1 cannot move gauchos to a town that isnt bordering`() {
            town1.owner = user1
            town2.owner = user1
            town2.borderingTowns = listOf()
            town1.borderingTowns = listOf()
            assertThrows<CustomException.Forbidden.IllegalGauchoMovement>
            { province.moveGauchosBetweenTowns(user1, MovementForm(town1.id, town2.id,2)) }
        }

        @Test
        fun `successfully move 3 gauchos from town1 to town2 leaves town1 with 7 gauchos and town2 with 23 gauchos`() {
            town1.owner = user1
            town2.owner = user1
            province.moveGauchosBetweenTowns(user1, MovementForm(town1.id, town2.id,3))
            assertThat(town1.gauchos).isEqualTo(7)
            assertThat(town2.gauchos).isEqualTo(23)
        }

        @Test
        fun `successfully move gauchos from town1 to town2 leaves town1 unlocked and town2 locked`() {
            town1.owner = user1
            town2.owner = user1
            province.moveGauchosBetweenTowns(user1, MovementForm(town1.id, town2.id,3))
            assertFalse(town1.isLocked)
            assertTrue(town2.isLocked)
        }
    }

    @Nested
    inner class AttackTown {
        private lateinit var jujuy: Province
        private lateinit var yavi: Town
        private lateinit var elCondor: Town
        private lateinit var cangrejillos: Town
        private lateinit var abraPampa: Town

        @BeforeEach
        fun fixture() {
            yavi = Town.new("Yavi", 3485.0263671875, listOf("El Cóndor", "Cangrejillos", "Abra Pampa"), Coordinates((-65.3412864273208).toFloat(), (-22.1949119799291).toFloat()))
            elCondor = Town.new("El Cóndor", 3609.618408203125, listOf("Yavi", "Cangrejillos", "Abra Pampa"), Coordinates((-65.3795310298623).toFloat(), (-22.414030404424).toFloat()))
            cangrejillos = Town.new("Cangrejillos", 3617.323974609375, listOf("El Cóndor", "Yavi", "Abra Pampa"), Coordinates((-65.5405751330491).toFloat(), (-22.4438999595476).toFloat()))
            abraPampa = Town.new("Abra Pampa", 3519.69287109375, listOf("El Cóndor", "Cangrejillos", "Yavi"), Coordinates((-66.0322682108588).toFloat(), (-22.8431449411788).toFloat()))
            yavi.owner = user1
            elCondor.owner = user1
            cangrejillos.owner = user2
            jujuy = Province("Jujuy", arrayListOf(yavi, elCondor, cangrejillos, abraPampa))
        }

        @Test
        fun `trying to attack from a Town that doesnt exist throws TownNotFoundException`() {
            assertThrows<CustomException.NotFound.TownNotFoundException> { province.attackTown(user1, AttackForm(townThatDoesntExists.id, town1.id), normalMode) }
        }

        @Test
        fun `trying to attack a Town that doesnt exist throws TownNotFoundException`() {
            assertThrows<CustomException.NotFound.TownNotFoundException>
            { province.attackTown(user1, AttackForm(town1.id, townThatDoesntExists.id), normalMode) }
        }

        @Test
        fun `user1 cannot attack from a town to itself`() {
            town1.owner = user1
            assertThrows<CustomException.Forbidden.IllegalAttack>
            { province.attackTown(user1, AttackForm(town1.id, town1.id), normalMode) }
        }

        @Test
        fun `user1 cannot attack from a town that doesnt belong to him`() {
            town1.owner = user2
            town2.owner = user2
            assertThrows<CustomException.Forbidden.IllegalAttack>
            { province.attackTown(user1, AttackForm(town1.id, town2.id), normalMode) }
        }

        @Test
        fun `user1 cannot attack to a town that belongs to him`() {
            town1.owner = user1
            town2.owner = user1
            assertThrows<CustomException.Forbidden.IllegalAttack>
            { province.attackTown(user1, AttackForm(town1.id, town2.id), normalMode) }
        }

        @Test
        fun `user1 cannot attack from a town that doesnt belong to someone`() {
            assertThrows<CustomException.Forbidden.IllegalAttack>
            { province.attackTown(user1, AttackForm(town1.id, town2.id), normalMode) }
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
        fun `distanceBetween|2 is commutative`() {
            assertThat(jujuy.distanceBetween(yavi, cangrejillos)).isEqualTo(jujuy.distanceBetween(cangrejillos, yavi))
        }

        @Test
        fun `the distance between to towns changes if one elevation changes`() {
            val elCondorWithFakeElevation = Town.new("El Cóndor", 100.0, listOf(), Coordinates((-65.3795310298623).toFloat(), (-22.414030404424).toFloat()))
            assertThat(jujuy.distanceBetween(yavi, elCondor)).isNotEqualTo(jujuy.distanceBetween(yavi, elCondorWithFakeElevation))
        }

        @Test
        fun `maxDistance in Jujuy is the distance between Yavi and Abra Pampa`() { // checked with Google Maps manually
            assertThat(jujuy.maxDistance).isEqualTo(jujuy.distanceBetween(yavi, abraPampa))
            assertThat(round(jujuy.maxDistance!!)).isEqualTo(101158.0)
        }

        @Test
        fun `minDistance in Jujuy is the distance between El Condor and Cangrejillos`() { // checked with Google Maps manually
            assertThat(jujuy.minDistance).isEqualTo(jujuy.distanceBetween(elCondor, cangrejillos))
            assertThat(round(jujuy.minDistance!!)).isEqualTo(16882.0)
        }

        @Test
        fun `user1 cannot atack a town that is not bordering`() {
            yavi.gauchos = 5
            abraPampa.gauchos = 4
            yavi.borderingTowns = listOf()
            abraPampa.borderingTowns = listOf()
            assertThrows<CustomException.Forbidden.IllegalAttack>{ jujuy.attackTown(user1, AttackForm(yavi.id, abraPampa.id), normalMode) }
        }

        @Test
        fun `Attack from Yavi (user1 town, 5 gauchos, Production) to AbraPampa (rebel town, 4 gauchos, Production) = Yavi (0 gauchos, user1), AbraPampa (2 gauchos, rebel)`() {
            yavi.gauchos = 5
            abraPampa.gauchos = 4
            jujuy.attackTown(user1, AttackForm(yavi.id, abraPampa.id), normalMode)
            assertAll(
                    Executable { assertThat(yavi.owner).isEqualTo(user1) },
                    Executable { assertThat(abraPampa.owner).isNull() },
                    Executable { assertThat(yavi.gauchos).isEqualTo(0) },
                    Executable { assertThat(abraPampa.gauchos).isEqualTo(2) }
            )
        }

        @Test
        fun `Attack from Yavi (user1 town, 10 gauchos, Production) to AbraPampa (rebel town, 4 gauchos, Production) = Yavi (0 gauchos, user1), AbraPampa (0 gauchos, user1)`() {
            yavi.gauchos = 10
            abraPampa.gauchos = 4
            jujuy.attackTown(user1, AttackForm(yavi.id, abraPampa.id), normalMode)
            assertAll(
                    Executable { assertThat(yavi.owner).isEqualTo(user1) },
                    Executable { assertThat(abraPampa.owner).isEqualTo(user1) },
                    Executable { assertThat(yavi.gauchos).isEqualTo(0) },
                    Executable { assertThat(abraPampa.gauchos).isEqualTo(0) }
            )
        }

        @Test
        fun `Attack from Yavi (user1 town, 10 gauchos, Production) to AbraPampa (rebel town, 4 gauchos, Defense) = Yavi (0 gauchos, user1), AbraPampa (1 gaucho, rebel)`() {
            yavi.gauchos = 10
            abraPampa.gauchos = 4
            abraPampa.specialization = Defense()
            jujuy.attackTown(user1, AttackForm(yavi.id, abraPampa.id), normalMode)
            assertAll(
                    Executable { assertThat(yavi.owner).isEqualTo(user1) },
                    Executable { assertThat(abraPampa.owner).isNull() },
                    Executable { assertThat(yavi.gauchos).isEqualTo(0) },
                    Executable { assertThat(abraPampa.gauchos).isEqualTo(1) }
            )
        }

        @Test
        fun `Attack from Yavi (user1 town, 153 gauchos, Defense) to Cangrejillos (user2 town, 201 gauchos, Production) = Yavi (0 gauchos, user1), Cangrejillos (110 gauchos, user2)`() {
            yavi.gauchos = 153
            cangrejillos.gauchos = 201
            yavi.specialization = Defense()
            jujuy.attackTown(user1, AttackForm(yavi.id, cangrejillos.id), normalMode)
            assertAll(
                    Executable { assertThat(yavi.owner).isEqualTo(user1) },
                    Executable { assertThat(cangrejillos.owner).isEqualTo(user2) },
                    Executable { assertThat(yavi.gauchos).isEqualTo(0) },
                    Executable { assertThat(cangrejillos.gauchos).isEqualTo(110) }
            )
        }
    }


}