package com.grupox.wololo.model

import com.google.common.math.DoubleMath.roundToInt
import com.grupox.wololo.model.helpers.GameMode
import java.math.RoundingMode

class Production : Specialization {
    override fun gauchos(gameMode: GameMode, townAltitude: Double, maxAltitude: Double, minAltitude: Double): Int =
            roundToInt(gameMode.multGauchosForProduction * this.gauchosBaseFormula(townAltitude, maxAltitude, minAltitude), RoundingMode.HALF_EVEN)

    override fun multDefense(gameMode: GameMode): Double = gameMode.multDefenseForProduction

    override fun updateStats(gameMode: GameMode, townAltitude: Double, maxAltitude: Double, minAltitude: Double, town: Town) {
        town.stats.increaseGauchosGeneratedByProduction(gauchos(gameMode, townAltitude, maxAltitude, minAltitude))
    }

    override fun toString(): String = "PRODUCTION"
}