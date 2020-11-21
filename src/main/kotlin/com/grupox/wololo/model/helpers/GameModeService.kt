package com.grupox.wololo.model.helpers

import com.grupox.wololo.configs.properties.GameProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class GameModeService {

    @Autowired
    private lateinit var gameProperties: GameProperties

    fun getDifficultyMultipliers(difficulty: String) : GameMode {
        //val gameMode: DTO.GameModeDTO
        lateinit var gameMode: GameMode
        when(difficulty){
            "EASY" -> gameMode = GameMode("EASY",gameProperties.multGauchosForDefenseEasyMode.toDouble(), gameProperties.multGauchosForProductionEasyMode.toDouble(), gameProperties.multDefenseForDefenseEasyMode.toDouble(), gameProperties.multDefenseForProductionEasyMode.toDouble())
            "NORMAL" -> gameMode = GameMode("NORMAL",gameProperties.multGauchosForDefenseNormalMode.toDouble(), gameProperties.multGauchosForProductionNormalMode.toDouble(), gameProperties.multDefenseForDefenseNormalMode.toDouble(), gameProperties.multDefenseForProductionNormalMode.toDouble())
            "HARD" -> gameMode = GameMode("HARD",gameProperties.multGauchosForDefenseHardMode.toDouble(), gameProperties.multGauchosForProductionHardMode.toDouble(), gameProperties.multDefenseForDefenseHardMode.toDouble(), gameProperties.multDefenseForProductionHardMode.toDouble())
        }
        return gameMode
    }




}