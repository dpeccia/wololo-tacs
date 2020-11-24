package com.grupox.wololo.model.helpers

import com.grupox.wololo.model.Difficulty
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.FileSystems
import java.nio.file.Paths
import java.util.*

object GamesConfigHelper {
    private fun getGamesConfigFilePath(): String {
        val absolutePath = FileSystems.getDefault().getPath("").toAbsolutePath().toString()
        return Paths.get(absolutePath, "src", "main", "resources", "game.properties").toString()
    }

    private fun getProperties(): Properties {
        val configFile = FileInputStream(getGamesConfigFilePath())
        val props = Properties()
        props.load(configFile)
        configFile.close()
        return props
    }

    fun getAllConfigurationValues(): Map<String, Double> {
        val properties = getProperties().map { Pair(it.key.toString(), it.value.toString().toDouble()) }
        val result = mutableMapOf<String, Double>()
        properties.forEach { result[it.first] = it.second }
        return result
    }

    fun getDifficultyMultipliers(difficulty: Difficulty): GameMode {
        val config = getAllConfigurationValues()
        return when(difficulty) {
            Difficulty.EASY -> GameMode("EASY", config["multGauchosForDefenseEasyMode"]!!, config["multGauchosForProductionEasyMode"]!!, config["multDefenseForDefenseEasyMode"]!!, config["multDefenseForProductionEasyMode"]!!)
            Difficulty.NORMAL -> GameMode("NORMAL", config["multGauchosForDefenseNormalMode"]!!, config["multGauchosForProductionNormalMode"]!!, config["multDefenseForDefenseNormalMode"]!!, config["multDefenseForProductionNormalMode"]!!)
            Difficulty.HARD -> GameMode("HARD", config["multGauchosForDefenseHardMode"]!!, config["multGauchosForProductionHardMode"]!!, config["multDefenseForDefenseHardMode"]!!, config["multDefenseForProductionHardMode"]!!)
        }
    }

    fun updateValues(changes: Map<String, Double>) {
        val props = getProperties()
        val configFile = FileOutputStream(getGamesConfigFilePath())
        changes.forEach { (key, value) -> props.setProperty(key, value.toString())}
        props.store(configFile, null)
        configFile.close()
    }
}