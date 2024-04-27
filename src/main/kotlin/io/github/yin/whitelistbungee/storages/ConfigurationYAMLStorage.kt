package io.github.yin.whitelistbungee.storages

import io.github.yin.whitelistbungee.Main
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption


object ConfigurationYAMLStorage {

    private lateinit var path: Path
    fun initialization(file: File) {
        path = file.toPath().resolve("config.yml")
        if (!Files.exists(path)) {
            Main.instance.getResourceAsStream("config.yml").use { stream ->
                Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    lateinit var configuration: Configuration
    fun load() {
        configuration = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(path.toFile())
    }


}