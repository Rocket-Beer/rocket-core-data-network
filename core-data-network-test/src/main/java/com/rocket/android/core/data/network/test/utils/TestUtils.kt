package com.rocket.android.core.data.network.test.utils

import com.rocket.core.domain.functional.Either

fun <L> Either<L, *>.l(): L {
    return this.fold(
        ifLeft = { it },
        ifRight = { throw TestException() }
    )
}

fun <R> Either<*, R>.r(): R {
    return this.fold(
        ifLeft = { throw TestException() },
        ifRight = { it }
    )
}
