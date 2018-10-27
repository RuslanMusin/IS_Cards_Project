package com.summer.itis.cardsproject.utils

import java.util.*

fun <T> List<T>.getRandom(): T? {
    if (size == 0) {
        return null
    } else {
        return this[Random().nextInt(size)]
    }
}

