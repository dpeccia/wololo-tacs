package com.grupox.wololo.model_tests

import com.grupox.wololo.model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GameTests {
    val user1: User = User(1,"a_mail", "a_password", false)
    val user2: User = User(2,"other_mail", "other_password", false)

    val town1: Town = Town(name = "town1", elevation = 10f)
    val town2: Town = Town(name = "town2", elevation = 10f)
    val town3: Town = Town(name = "town3", elevation = 10f)
    val town4: Town = Town(name = "town4", elevation = 10f)
    val town5: Town = Town(name = "town5", elevation = 10f)

    val towns: List<Town> = listOf(town1, town2, town3, town4, town5)
    val players: List<User> = listOf(user1, user2)

    @Test
    fun `creating a game distributes towns with the available players evenly`(){
        val game = Game(players, Province("a_province", ArrayList(towns)))

        val numberOfTownsAssignedToUser1: Int = game.province.towns.count { it.owner?.id == user1.id }
        val numberOfTownsAssignedToUser2: Int = game.province.towns.count { it.owner?.id == user2.id }

        assertEquals(numberOfTownsAssignedToUser1, numberOfTownsAssignedToUser2)
    }
}