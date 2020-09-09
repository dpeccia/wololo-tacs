package com.grupox.wololo.model.services

import arrow.core.Either
import arrow.core.right
import com.grupox.wololo.errors.CustomException


object Pixabay : HttpService(apiName = "Pixabay"){
    private fun requestImage(locationName: String): Either<CustomException, String> {
        return "https://as00.epimg.net/img/comunes/fotos/fichas/equipos/large/107.png".right()  // Lo mockeo por ahora
    }
}