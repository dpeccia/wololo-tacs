package com.grupox.wololo.model

import kotlin.math.round

class Defense : Specialization {
    override fun gauchos(townAltitude: Double, maxAltitude: Double, minAltitude: Double): Int =
            round(10 * this.gauchosBaseFormula(townAltitude, maxAltitude, minAltitude)).toInt()

    override fun multDefense(): Double = 1.25
}