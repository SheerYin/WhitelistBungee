package io.github.yin.whitelistbungee.storages

import io.github.yin.whitelistbungee.Main
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption


object WhitelistYAMLStorage {

    private lateinit var path: Path

    fun initialization(file: File) {
        val customPath: Path =
            Path.of(ConfigurationYAMLStorage.configuration.getString("players.yaml.path"))
        if (customPath.toString().isEmpty()) {
            path = Paths.get(file.path, "players.yml")
        } else {
            if (customPath.startsWith(Paths.get("plugins"))) {
                path = file.toPath().parent.resolve(customPath.subpath(1, customPath.nameCount))
                val directory = path.parent
                if (!Files.exists(directory)) {
                    Files.createDirectories(directory)
                }
            } else {
                path = customPath
            }
        }

        if (!Files.exists(path)) {
            Main.instance.getResourceAsStream("players.yml").use { stream ->
                Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    private lateinit var configuration: Configuration

    fun load() {
        configuration = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(path.toFile())
    }

    fun getPlayerNames(): MutableSet<String> {
        return configuration.getStringList("names").toMutableSet()
    }

    fun save(list: Set<String>) {
        configuration.set("names", list.stream().toList())
        ConfigurationProvider.getProvider(YamlConfiguration::class.java).save(configuration, path.toFile())
    }

}