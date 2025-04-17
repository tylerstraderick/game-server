package com.straderick.gameserver.config

import java.util.Properties

object Configuration {
    private val properties = Properties().apply {
        ClassLoader.getSystemClassLoader().getResourceAsStream("application.properties")?.use {
            load(it)
        }
    }

    fun getProperty(key: String): String? = properties.getProperty(key)
}