package com.grupox.wololo.model

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.helpers.DTO
import com.grupox.wololo.model.helpers.GameMode
import com.grupox.wololo.model.helpers.Requestable
import org.springframework.data.mongodb.core.mapping.DBRef
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

data class Town(val id: Int, val name: String, val coordinates: Coordinates, val elevation: Double, val townImage: String, val stats: TownStats, var borderingTowns: List<String>) : Requestable {
    @DBRef
    var owner: User? = null

    var isLocked: Boolean = false
    var specialization : Specialization = Production()
    var gauchos = 0

    companion object {
        private val idGenerator: AtomicInteger = AtomicInteger(0)
        fun new(_name: String, _elevation: Double, _borderingTowns: List<String>, _coordinates: Coordinates = Coordinates(0f,0f),
                _townImage: String = "", _stats: TownStats = TownStats(0,0)): Town =
                Town(idGenerator.incrementAndGet(), _name, _coordinates, _elevation, _townImage, _stats, _borderingTowns)
    }

    fun isFrom(user: User) = owner?.id.toString() == user.id.toString()

    fun unlock() {
        isLocked = false
    }

    private fun multDefense(gameMode: GameMode): Double = specialization.multDefense(gameMode)

    fun addGauchos(maxAltitude: Double, minAltitude: Double, gameMode: GameMode) {
        val gauchosAmount: Int = specialization.gauchos(gameMode, elevation, maxAltitude, minAltitude)
        gauchos += gauchosAmount
        specialization.updateStats(gameMode, elevation, maxAltitude, minAltitude, this)
    }

    fun giveGauchos(qty: Int) {
        if (qty <= 0) throw CustomException.BadRequest.IllegalGauchosQtyException()
        if (qty > gauchos) throw CustomException.BadRequest.NotEnoughGauchosException(qty, gauchos)
        gauchos -= qty
    }

    fun receiveGauchos(qty: Int) {
        if (qty <= 0) throw CustomException.BadRequest.IllegalGauchosQtyException()
        gauchos += qty
        isLocked = true
    }

    fun attack(defenderQty: Int, multDistance: Double, multAltitude: Double, gameMode: GameMode) {
        val gauchosAttackFinal = floor(gauchos * multDistance - defenderQty * multAltitude * this.multDefense(gameMode)).toInt()
        this.gauchos = max(gauchosAttackFinal, 0)
    }

    fun defend(attackerOwner: User, attackerQty: Int, multDistance: Double, multAltitude: Double, gameMode: GameMode) {
        val gauchosDefenseFinal =
                ceil((gauchos * multAltitude * this.multDefense(gameMode) - attackerQty * multDistance) / (multAltitude * this.multDefense(gameMode))).toInt()
        if(gauchosDefenseFinal <= 0)
            this.owner = attackerOwner
        this.gauchos = max(gauchosDefenseFinal, 0)
    }

    fun neutralize() {
        this.owner = null
        // Por ahi queremos setear un numero de gauchos particular
    }

    override fun dto(): DTO.TownDTO =
        DTO.TownDTO(
            id = id,
            name = name,
            coordinates = coordinates,
            elevation = elevation,
            imageUrl = townImage,
            ownerId = owner?.id?.toString(),
            specialization = specialization.toString(),
            gauchos = gauchos,
            isLocked = isLocked,
            gauchosGeneratedByDefense = stats.gauchosGeneratedByDefense,
            gauchosGeneratedByProduction = stats.gauchosGeneratedByProduction
        )
}

