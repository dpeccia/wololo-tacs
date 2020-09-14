package com.grupox.wololo.model.repos

import arrow.core.Option

interface Repository<T> {
    fun getAll(): List<T>
    fun getById(id: Int): Option<T>
    fun filter(predicate: (obj: T) -> Boolean): List<T>
    fun insert(obj: T)
}