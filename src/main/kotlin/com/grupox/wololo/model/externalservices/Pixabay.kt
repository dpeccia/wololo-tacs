package com.grupox.wololo.model.externalservices

import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.grupox.wololo.configs.properties.PixabayProperties
import com.grupox.wololo.errors.CustomException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Service


@JsonIgnoreProperties(ignoreUnknown = true)
data class ImageQueryResponse (
    val totalHits: Int,
    val hits: List<HitInfo>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class HitInfo (
    val webformatURL: String
)

interface IPixabay{
    fun requestTownImage(townName: String): Either<CustomException, String>
}

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
class Pixabay : IPixabay{
    @Autowired
    lateinit var pixabayProperties: PixabayProperties

    val baseUrl: String = "https://pixabay.com/api/"
    val category: String = "buildings"

    @Cacheable("withTimeToLive")
    override fun requestTownImage(townName: String): Either<CustomException, String> {
        val query = formatString(townName)
        val response = HttpService("Pixabay").requestData<ImageQueryResponse>(baseUrl, mapOf("key" to pixabayProperties.apiKey, "category" to category, "q" to query))

        return response.map {
            if (it.totalHits <= 0)
                pixabayProperties.defaultImage
            else
                it.hits.first().webformatURL
        }
    }

    private fun formatString(str: String): String =
        str.replace(' ', '+')
}