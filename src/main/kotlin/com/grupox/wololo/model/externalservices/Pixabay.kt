package com.grupox.wololo.model.externalservices

import arrow.core.Either
import arrow.core.right
import com.grupox.wololo.errors.CustomException
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Service

interface IPixabay{
    fun requestTownImage(townName: String): Either<CustomException, String>
}

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
class Pixabay : HttpService(apiName = "Pixabay"), IPixabay{
    @Cacheable("withTimeToLive")
    override fun requestTownImage(townName: String): Either<CustomException, String> =
        "https://as00.epimg.net/img/comunes/fotos/fichas/equipos/large/107.png".right()  // Lo mockeo por ahora
}