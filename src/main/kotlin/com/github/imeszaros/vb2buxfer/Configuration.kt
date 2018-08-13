package com.github.imeszaros.vb2buxfer

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.Item
import com.uchuhimo.konf.source.yaml.toYaml
import java.io.File

class Configuration {

    private val filename = "config.yml"

    private var config = load()

    fun save() {
        config.toYaml.toFile(filename)
        config = load()
    }

    operator fun <T : Any> get(key: Item<T>): T = config[key]

    operator fun <T : Any> set(key: Item<T>, value: T): Configuration {
        config[key] = value
        return this
    }

    private fun load(): Config = Config {
        addSpec(Buxfer)
        addSpec(Settings)
    }.let { conf ->
        File(filename).takeIf { it.exists() }
                ?.let { conf.withSourceFrom.yaml.file(it) } ?: conf
    }
            .withSourceFrom.env()
            .withSourceFrom.systemProperties()
            .withLayer("application")

    object Buxfer : ConfigSpec("buxfer") {

        val username by optional("", description = "Buxfer username.")
        val password by optional("", description = "Buxfer password.")
    }

    object Settings : ConfigSpec("settings") {

        val account by optional("", description = "Last selected account.")
        val tagMappings by optional(mapOf<String, String>(), description = "Transaction description -> Tags")
    }
}