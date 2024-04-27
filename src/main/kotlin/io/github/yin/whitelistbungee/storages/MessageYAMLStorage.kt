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

object MessageYAMLStorage {

    private lateinit var folder: Path
    private lateinit var path: Path
    lateinit var language: String

    fun initialization(file: File) {
        val custom: Path = Paths.get(ConfigurationYAMLStorage.configuration.getString("message.translations.file.path"))
        // 如果是空则使用默认路径
        if (custom.toString().isEmpty()) {
            folder = Paths.get(file.path, "Translations")
        } else {
            if (custom.startsWith(Paths.get("plugins"))) {
                folder = file.toPath().parent.resolve(custom.subpath(1, custom.nameCount))
            }
        }
        Files.createDirectories(folder)
        language = ConfigurationYAMLStorage.configuration.getString("message.language")
        path = folder.resolve("$language.yml")
        // 示例文件是必须存在的
        if (!Files.exists(path)) {
            val stream = Main.instance.getResourceAsStream("Translations/chinese.yml")
            Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING)
        }

    }

    lateinit var configuration: Configuration
    fun load() {
        configuration = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(path.toFile())
    }
}