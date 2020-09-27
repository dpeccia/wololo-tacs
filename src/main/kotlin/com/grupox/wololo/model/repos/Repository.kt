package com.grupox.wololo.model.repos

import arrow.core.Either
import com.grupox.wololo.errors.CustomException

interface Repository<T> {
    fun getAll(): List<T>
    fun getById(id: Int): Either<CustomException.NotFound, T>
    fun filter(predicate: (obj: T) -> Boolean): List<T>
    fun insert(obj: T)
}