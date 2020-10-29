package com.grupox.wololo.model.externalservices

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.grupox.wololo.configs.properties.PixabayProperties
import com.grupox.wololo.errors.CustomException
import org.geojson.FeatureCollection
import org.geojson.GeoJsonObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.io.File
import java.text.Normalizer

@Service
@PropertySource("classpath:provinces.properties")
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
class ProvincesService {
    @Autowired
    private lateinit var pixabayProperties: PixabayProperties

    @Autowired
    private lateinit var env: Environment

    private lateinit var _townsGeoJSONs: List<TownGeoJSON>

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

    //@Cacheable("withTimeToLive") No se por qué rompe cuando lo uso
    fun getUrl(provinceName: String): String =
        env.getProperty("${provinceName.toUpperCase().replace(' ', '_')}.url") ?: pixabayProperties.defaultImage

    @Cacheable("withTimeToLive")
    fun availableProvinces(): Either<CustomException, List<String>> {
        val eitherFile = runCatching { File("src/main/resources/provinces.properties") }
                    .fold({ it.right() }, { CustomException.Service.ProvincePropertiesNotAvailableException().left() })

        return eitherFile.map { file -> file.readLines().filter { it.isNotBlank() }.map { formatLine(it) } }
    }

    fun townsGeoJSONs(provinceName: String, townNames: List<String>): List<TownGeoJSON> {
        val formattedProvinceName = unaccent(provinceName).toUpperCase()
        val byProvince = _townsGeoJSONs.filter { json -> json.features.any { it.properties.province == formattedProvinceName } }
        val formattedTownNames = townNames.map { formatTownName(it) }
        return byProvince.filter { json -> json.features.any { formattedTownNames.contains(it.properties.town) } }
    }

    fun getRandomBorderingTowns(provinceName: String, townAmount: Int): List<TownGeoJSONWithBordering> {
        val formattedProvinceName = unaccent(provinceName).toUpperCase()
        val byProvince = _townsGeoJSONs
                .filter { json -> json.features.any { it.properties.province == formattedProvinceName } }
                .map { TownGeoJSONProperties(formattedProvinceName, formatTownName(it.features.first().properties.town),
                        it.features.first().properties.bordering.map { b -> formatTownName(b) }) }
        val randomTown = byProvince.shuffled().first()
        val towns = listOf(TownGeoJSONWithBordering(randomTown.town, randomTown.bordering))
        return getTownsRecursive(towns.first(), byProvince, towns, townAmount - 1)
    }

    private fun getTownsRecursive(seed: TownGeoJSONWithBordering, towns: List<TownGeoJSONProperties>, result: List<TownGeoJSONWithBordering>, townAmount: Int): List<TownGeoJSONWithBordering> {
        if(townAmount == 0) return result
        var borderingTowns = seed.borderingTowns.map { btown -> TownGeoJSONWithBordering(btown, towns.find { it.town == btown }!!.bordering) }
        if(borderingTowns.size > townAmount)
            borderingTowns = borderingTowns.take(townAmount)
        val res = result + (borderingTowns - result.intersect(borderingTowns))
        val townQty = townAmount - (borderingTowns - result.intersect(borderingTowns)).size
        val nextSeed = borderingTowns.shuffled().find { t -> t.borderingTowns.any { !res.map{ r -> r.town}.contains(it) } }!!
        return getTownsRecursive(nextSeed, towns, res, townQty)
    }

    private fun formatLine(line: String): String =
            line.substringBefore('.')
                    .removeSurrounding(" ")
                    .replace('_', ' ')
                    .toLowerCase()
                    .split(' ')
                    .joinToString(" ") { if (it.length > 3) it.capitalize() else it }
                    .capitalize()

    private fun unaccent(str: String): String {
        return str.map { unaccentChar(it) }.joinToString("")
    }

    private fun unaccentChar(char: Char): Char {
        val escapeRegex = "[ñÑ]".toRegex()
        val asString = char.toString()

        if(asString.matches(escapeRegex))
            return char

        val regexUnaccent = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        val temp = Normalizer.normalize(asString, Normalizer.Form.NFD)
        return regexUnaccent.replace(temp, "")[0]
    }

    private fun unpunctuate(str: String): String {
        val regexPunctuation = "[.,;]".toRegex()
        return regexPunctuation.replace(str, "")
    }

    private fun formatTownName(townName: String) = unpunctuate(unaccent(townName)).toUpperCase()
}

data class TownGeoJSONProperties(val province: String, val town: String, val bordering: List<String>)

data class TownGeoJSONInfo(val type: String, val properties: TownGeoJSONProperties, val geometry: GeoJsonObject)

data class TownGeoJSON(val type: String, val features: List<TownGeoJSONInfo>)

data class TownGeoJSONWithBordering(val town: String, val borderingTowns: List<String>)