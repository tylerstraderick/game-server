package com.straderick.gameserver.core.utils

object Logger {
    fun info(message: String) {
        println("INFO: $message")
    }

    fun error(message: String, throwable: Throwable? = null) {
        println("ERROR: $message")
        throwable?.printStackTrace()
    }
}