package com.grupox.wololo.model

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.either.monad.flatMap
import arrow.core.extensions.either.monad.monad
import arrow.core.extensions.fx
import arrow.core.extensions.list.traverse.sequence
import arrow.core.extensions.list.traverse.traverse
import arrow.core.fix
import arrow.core.flatMap
import arrow.fx.IO
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.services.GeoRef
import com.grupox.wololo.model.services.LocationData
import com.grupox.wololo.model.services.TopoData

object ProvinceFactory {
    fun generateProvince(provinceName: String, townAmount: Int): Either<CustomException, Province> {
        return Either.fx<CustomException, Province> {
            val provinceData = GeoRef.requestProvinceData(provinceName).bind()
            val townsData = GeoRef.requestTownsData(provinceData.id, townAmount).bind()
            val towns = townsData.map { generateTown((it)) }.sequence(Either.applicative()).fix().bind()
            Province (
                    id = provinceData.id,
                    name = provinceData.name,
                    coordinates = provinceData.coordinates,
                    towns = ArrayList(towns.fix())
            )
        }.fix()
    }

    private fun generateTown(townData: LocationData): Either<CustomException, Town> =
        TopoData.requestElevation(townData.coordinates).map { elevation ->
            Town (id = townData.id, name = townData.name, coordinates = townData.coordinates, elevation = elevation)
        }
}