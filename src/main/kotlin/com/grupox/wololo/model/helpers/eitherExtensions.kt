package com.grupox.wololo.model.helpers

import arrow.core.Either
import arrow.core.getOrHandle

fun <T>Either<Throwable, T>.getOrThrow(): T = this.getOrHandle { throw it }