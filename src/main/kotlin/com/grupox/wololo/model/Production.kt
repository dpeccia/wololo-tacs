package com.grupox.wololo.model

import com.google.common.math.DoubleMath.roundToInt
import java.math.RoundingMode

class Production : Specialization {
    override fun gauchos(townAltitude: Double, maxAltitude: Double, minAltitude: Double): Int =
            roundToInt(15 * this.gauchosBaseFormula(townAltitude, maxAltitude, minAltitude), RoundingMode.HALF_EVEN)

    override fun multDefense(): Double = 1.0

    override fun updateStats(townAltitude: Double, maxAltitude: Double, minAltitude: Double, town: Town) {
        town.updateGauchosByProduction(gauchos(townAltitude, maxAltitude, minAltitude))
    }

    override fun toString(): String = "PRODUCTION"
}