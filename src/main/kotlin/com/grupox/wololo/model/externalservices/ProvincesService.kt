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
                            town = it.properties["departamento"] as String
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
        val formatedProvinceName = unaccentString(provinceName).toUpperCase()
        val byProvince = _townsGeoJSONs.filter { json -> json.features.any { it.properties.province == formatedProvinceName } }
        val formatedTownNames = townNames.map { unaccentString(it).toUpperCase() }
        return byProvince.filter { json -> json.features.any { formatedTownNames.contains(it.properties.town) } }
    }

    private fun formatLine(line: String): String =
            line.substringBefore('.')
                    .removeSurrounding(" ")
                    .replace('_', ' ')
                    .toLowerCase()
                    .split(' ')
                    .joinToString(" ") { if (it.length > 3) it.capitalize() else it }
                    .capitalize()

    private fun unaccentString(str: String): String {
        val regexUnaccent = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        val temp = Normalizer.normalize(str, Normalizer.Form.NFD)
        return regexUnaccent.replace(temp, "")
    }
}

data class TownGeoJSONProperties(val province: String, val town: String)

data class TownGeoJSONInfo(val type: String, val properties: TownGeoJSONProperties, val geometry: GeoJsonObject)

data class TownGeoJSON(val type: String, val features: List<TownGeoJSONInfo>)