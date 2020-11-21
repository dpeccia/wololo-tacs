package com.grupox.wololo.model.helpers

import com.grupox.wololo.configs.properties.GameProperties
import com.grupox.wololo.model.Difficulty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class GameModeService {
    @Autowired
    private lateinit var gameProperties: GameProperties

    fun getDifficultyMultipliers(difficulty: Difficulty) : GameMode = when(difficulty) {
        Difficulty.EASY -> GameMode("EASY", gameProperties.multGauchosForDefenseEasyMode.toDouble(), gameProperties.multGauchosForProductionEasyMode.toDouble(), gameProperties.multDefenseForDefenseEasyMode.toDouble(), gameProperties.multDefenseForProductionEasyMode.toDouble())
        Difficulty.NORMAL -> GameMode("NORMAL", gameProperties.multGauchosForDefenseNormalMode.toDouble(), gameProperties.multGauchosForProductionNormalMode.toDouble(), gameProperties.multDefenseForDefenseNormalMode.toDouble(), gameProperties.multDefenseForProductionNormalMode.toDouble())
        Difficulty.HARD -> GameMode("HARD", gameProperties.multGauchosForDefenseHardMode.toDouble(), gameProperties.multGauchosForProductionHardMode.toDouble(), gameProperties.multDefenseForDefenseHardMode.toDouble(), gameProperties.multDefenseForProductionHardMode.toDouble())
    }
}