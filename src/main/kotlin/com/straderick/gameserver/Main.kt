package com.straderick.gameserver

import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val application = Application()
    application.start()
}