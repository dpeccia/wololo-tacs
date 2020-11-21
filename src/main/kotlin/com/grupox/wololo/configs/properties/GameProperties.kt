package com.grupox.wololo.configs.properties

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@ConfigurationProperties(prefix = "game")
@PropertySource("classpath:game.properties")
class GameProperties {

    @Value("\${game.multGauchosForDefenseEasyMode}")
    lateinit var multGauchosForDefenseEasyMode: String
    @Value("\${game.multGauchosForProductionEasyMode}")
    lateinit var multGauchosForProductionEasyMode: String
    @Value("\${game.multDefenseForDefenseEasyMode}")
    lateinit var multDefenseForDefenseEasyMode: String
    @Value("\${game.multDefenseForProductionEasyMode}")
    lateinit var multDefenseForProductionEasyMode: String

    @Value("\${game.multGauchosForDefenseNormalMode}")
    lateinit var multGauchosForDefenseNormalMode: String
    @Value("\${game.multGauchosForProductionNormalMode}")
    lateinit var multGauchosForProductionNormalMode: String
    @Value("\${game.multDefenseForDefenseNormalMode}")
    lateinit var multDefenseForDefenseNormalMode: String
    @Value("\${game.multDefenseForProductionNormalMode}")
    lateinit var multDefenseForProductionNormalMode: String

    @Value("\${game.multGauchosForDefenseHardMode}")
    lateinit var multGauchosForDefenseHardMode: String
    @Value("\${game.multGauchosForProductionHardMode}")
    lateinit var multGauchosForProductionHardMode: String
    @Value("\${game.multDefenseForDefenseHardMode}")
    lateinit var multDefenseForDefenseHardMode: String
    @Value("\${game.multDefenseForProductionHardMode}")
    lateinit var multDefenseForProductionHardMode: String




}