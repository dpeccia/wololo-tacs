package com.grupox.wololo.model

import com.google.common.math.DoubleMath
import com.google.common.math.DoubleMath.roundToInt
import java.math.RoundingMode


class Defense : Specialization {
    override fun gauchos(townAltitude: Double, maxAltitude: Double, minAltitude: Double): Int =
            roundToInt(10 * this.gauchosBaseFormula(townAltitude, maxAltitude, minAltitude), RoundingMode.HALF_EVEN)

    override fun multDefense(): Double = 1.25

    override fun updateStats(townAltitude: Double, maxAltitude: Double, minAltitude: Double, town: Town) {
        town.updateGauchosByDefense(gauchos(townAltitude, maxAltitude, minAltitude))
    }
}