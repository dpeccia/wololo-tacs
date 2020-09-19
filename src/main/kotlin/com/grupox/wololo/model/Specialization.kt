package com.grupox.wololo.model

interface Specialization {
    fun multDefense(): Double

    fun gauchos(townAltitude: Double, maxAltitude: Double, minAltitude: Double): Int

    fun updateStats(townAltitude: Double, maxAltitude: Double, minAltitude: Double, town: Town)

    fun gauchosBaseFormula(townAltitude: Double, maxAltitude: Double, minAltitude: Double): Double =
            (1 - (townAltitude - minAltitude) / 2 * (maxAltitude - minAltitude))
}