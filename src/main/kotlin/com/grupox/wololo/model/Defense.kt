package com.grupox.wololo.model

import com.google.common.math.DoubleMath.roundToInt
import com.grupox.wololo.model.helpers.GameMode
import java.math.RoundingMode


class Defense : Specialization {
    override fun gauchos(gameMode: GameMode, townAltitude: Double, maxAltitude: Double, minAltitude: Double): Int =
            roundToInt(gameMode.multGauchosForDefense * this.gauchosBaseFormula(townAltitude, maxAltitude, minAltitude), RoundingMode.HALF_EVEN)

    override fun multDefense(gameMode: GameMode): Double = gameMode.multDefenseForDefense

    override fun updateStats(gameMode: GameMode, townAltitude: Double, maxAltitude: Double, minAltitude: Double, town: Town) {
        town.stats.increaseGauchosGeneratedByDefense(gauchos(gameMode, townAltitude, maxAltitude, minAltitude))
    }

    override fun toString(): String = "DEFENSE"
}