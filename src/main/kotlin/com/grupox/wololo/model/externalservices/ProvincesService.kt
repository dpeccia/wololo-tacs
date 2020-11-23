package com.grupox.wololo.model.externalservices

import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.helpers.formatTownName
import com.grupox.wololo.model.helpers.unaccent
import org.geojson.FeatureCollection
import org.geojson.GeoJsonObject
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Service
import java.io.File

@Service
@PropertySource("classpath:provinces.properties")
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
class ProvincesService {
    lateinit var _townsGeoJSONs: List<TownGeoJSON>

    init {
        val jsonString: String = File("src/main/resources/departamentos-argentina.json").readText(Charsets.UTF_8)
        val mapper = jacksonObjectMapper()
        val allTownsGeoInfo = mapper.readValue<FeatureCollection>(jsonString)
        this._townsGeoJSONs = allTownsGeoInfo.features.map {
            TownGeoJSON(
                type = "FeatureCollection",
                features = listOf(
                    TownGeoJSONInfo(
                        type = "Feature",
                        properties = TownGeoJSONProperties(
                                province = it.properties["provincia"] as String,
                                town = it.properties["departamento"] as String,
                                bordering = it.properties["limitrofes"] as List<String>
                        ),
                        geometry = it.geometry
                    )
                )
            )
        }
    }

    fun getProvinces(): List<ProvinceGeoJSON> {
        val provincesAndTowns = _townsGeoJSONs.map { ProvinceAndTown(it.features[0].properties.province, it.features[0].properties.town) }
        val provinces = provincesAndTowns.map { it.province }.toSet()
        return provinces.map { province -> ProvinceGeoJSON(province, provincesAndTowns.filter { it.province == province }.size.coerceAtMost(100)) }
    }

    fun townsGeoJSONs(provinceName: String, townNames: List<String>): List<TownGeoJSON> {
        val formattedProvinceName = unaccent(provinceName).toUpperCase()
        val byProvince = _townsGeoJSONs.filter { json -> json.features.any { it.properties.province == formattedProvinceName } }
        val formattedTownNames = townNames.map { formatTownName(it) }
        return byProvince.filter { json -> json.features.any { formattedTownNames.contains(it.properties.town) } }
    }

    fun getRandomBorderingTowns(provinceName: String, townAmount: Int): Either<CustomException, List<TownGeoJSONWithBordering>> {
        val formattedProvinceName = unaccent(provinceName).toUpperCase()
        val byProvince = _townsGeoJSONs
                .filter { json -> json.features.any { it.properties.province == formattedProvinceName } }
                .map { TownGeoJSONProperties(formattedProvinceName, formatTownName(it.features.first().properties.town),
                        it.features.first().properties.bordering.map { b -> formatTownName(b) }) }
        if(byProvince.size < townAmount) return Left(CustomException.BadRequest.NotEnoughTownsException(provinceName, townAmount, byProvince.size))
        val randomTown = byProvince.shuffled().first()
        val towns = listOf(TownGeoJSONWithBordering(randomTown.town, randomTown.bordering, true))
        val result = getTownsRecursive(towns.first(), byProvince, towns, townAmount - 1)
        val resultAsStrings = result.map { it.town }
        result.forEach { t -> t.borderingTowns = t.borderingTowns.filter { resultAsStrings.contains(it) } }
        return Right(result)
    }

    private fun getTownsRecursive(seed: TownGeoJSONWithBordering, towns: List<TownGeoJSONProperties>, result: List<TownGeoJSONWithBordering>, townAmount: Int): List<TownGeoJSONWithBordering> {
        val borderingTownsStrings = seed.borderingTowns
        val resultStrings = result.map { it.town }
        var townsToAdd = borderingTownsStrings - resultStrings.intersect(borderingTownsStrings)
        if(townsToAdd.size > townAmount)
            townsToAdd = townsToAdd.take(townAmount)
        val res = result + townsToAdd.map { btown -> TownGeoJSONWithBordering(btown, towns.find { it.town == btown }!!.bordering) }
        val townQty = townAmount - townsToAdd.size
        if(townQty == 0) return res
        val nextSeed = res.shuffled().find { t -> !t.wasSeed && t.borderingTowns.any { !res.map{ r -> r.town}.contains(it) } }!!
        nextSeed.wasSeed = true
        return getTownsRecursive(nextSeed, towns, res, townQty)
    }
}

data class TownGeoJSONProperties(val province: String, val town: String, val bordering: List<String>)

data class TownGeoJSONInfo(val type: String, val properties: TownGeoJSONProperties, val geometry: GeoJsonObject)

data class TownGeoJSON(val type: String, val features: List<TownGeoJSONInfo>)

data class TownGeoJSONWithBordering(val town: String, var borderingTowns: List<String>, var wasSeed: Boolean = false)

data class ProvinceGeoJSON(val name: String, val qty: Int)

data class ProvinceAndTown(val province: String, val town: String)