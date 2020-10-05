package com.grupox.wololo.model_tests

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Town
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TownTests {
    lateinit var town: Town

    @BeforeEach
    fun fixture() {
        town = Town.new("town", 10.0)
        town.gauchos = 10
    }

    @Nested
    inner class GiveGauchos {
        @Test
        fun `trying to give 0 gauchos throws IllegalGauchosQtyException`() {
            assertThrows<CustomException.BadRequest.IllegalGauchosQtyException> { town.giveGauchos(0) }
        }

        @Test
        fun `trying to give a negative number of gauchos throws IllegalGauchosQtyException`() {
            assertThrows<CustomException.BadRequest.IllegalGauchosQtyException> { town.giveGauchos(-1) }
        }

        @Test
        fun `trying to give more gauchos than the town has throws NotEnoughGauchosException`() {
            assertThrows<CustomException.BadRequest.NotEnoughGauchosException> { town.giveGauchos(11) }
        }

        @Test
        fun `successfully give 2 gauchos leaves 8 gauchos in the town`() {
            town.giveGauchos(2)
            assertThat(town.gauchos).isEqualTo(8)
        }
    }

    @Nested
    inner class ReceiveGauchos {
        @Test
        fun `trying to receive 0 gauchos throws IllegalGauchosQtyException`() {
            assertThrows<CustomException.BadRequest.IllegalGauchosQtyException> { town.receiveGauchos(0) }
        }

        @Test
        fun `trying to receive a negative number of gauchos throws IllegalGauchosQtyException`() {
            assertThrows<CustomException.BadRequest.IllegalGauchosQtyException> { town.receiveGauchos(-1) }
        }

        @Test
        fun `successfully receive 2 gauchos leaves 12 gauchos in the town`() {
            town.receiveGauchos(2)
            assertThat(town.gauchos).isEqualTo(12)
        }

        @Test
        fun `successfully receive gauchos leaves the town locked`() {
            town.isLocked = false
            town.receiveGauchos(2)
            assertTrue(town.isLocked)
        }
    }
}