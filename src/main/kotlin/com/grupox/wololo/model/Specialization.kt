package com.grupox.wololo.model

import com.grupox.wololo.model.helpers.GameMode

interface Specialization {
    fun multDefense(gameMode: GameMode): Double

    fun gauchos(gameMode: GameMode, townAltitude: Double, maxAltitude: Double, minAltitude: Double): Int

    fun updateStats(gameMode: GameMode, townAltitude: Double, maxAltitude: Double, minAltitude: Double, town: Town)

    fun gauchosBaseFormula(townAltitude: Double, maxAltitude: Double, minAltitude: Double): Double =
            (1 - ((townAltitude - minAltitude) / (2 * (maxAltitude - minAltitude))))

    override fun toString(): String
}