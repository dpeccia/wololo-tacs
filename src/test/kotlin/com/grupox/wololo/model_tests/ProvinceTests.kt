package com.grupox.wololo.model_tests

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.MovementForm
import com.grupox.wololo.model.repos.RepoUsers
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProvinceTests {
    val user1: User = User(1,"a_mail", "a_password", false)
    val user2: User = User(2,"other_mail", "other_password", false)

    val town1: Town = Town(id = 1, name = "town1", elevation = 10.0)
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
        fun `a`() {

        }
    }

    // TODO Tests de ataque de municipio
    @Nested
    inner class AttackTown {
        @Test
        fun `b`() {

        }
    }


}