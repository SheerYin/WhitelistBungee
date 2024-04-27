package io.github.yin.whitelistbungee

import io.github.yin.whitelistbungee.commands.WhitelistBungeeTabExecutor
import io.github.yin.whitelistbungee.listeners.ServerConnect
import io.github.yin.whitelistbungee.storages.ConfigurationYAMLStorage
import io.github.yin.whitelistbungee.storages.MessageYAMLStorage
import io.github.yin.whitelistbungee.storages.WhitelistMySQLStorage
import io.github.yin.whitelistbungee.storages.WhitelistYAMLStorage
import io.github.yin.whitelistbungee.supports.DelayUpdate
import io.github.yin.whitelistbungee.supports.Whitelist
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.plugin.Plugin
import java.util.*


class Main : Plugin() {
    companion object {
        lateinit var instance: Main
        const val prefix = "§f[§c白名单§f] "
        lateinit var storage: String
    }

    override fun onEnable() {
        instance = this
        proxy.console.sendMessage(TextComponent(prefix + "插件开始加载 " + description.version))

        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }

        ConfigurationYAMLStorage.initialization(dataFolder)
        ConfigurationYAMLStorage.load()
        MessageYAMLStorage.initialization(dataFolder)
        MessageYAMLStorage.load()
        val configuration = ConfigurationYAMLStorage.configuration
        storage = configuration.getString("players.storage").lowercase(Locale.getDefault())

        when (storage) {
            "yaml" -> {
                WhitelistYAMLStorage.initialization(dataFolder)
                WhitelistYAMLStorage.load()
            }

            "mysql" -> {
                val parameter = WhitelistMySQLStorage.Parameter(
                    configuration.getString("players.mysql.url"),
                    configuration.getString("players.mysql.username"),
                    configuration.getString("players.mysql.password"),
                    configuration.getInt("players.mysql.maximum-pool-size"),
                    configuration.getInt("players.mysql.minimum-idle"),
                    configuration.getLong("players.mysql.connection-timeout"),
                    configuration.getLong("players.mysql.idle-timeout"),
                    configuration.getLong("players.mysql.maximum-lifetime")
                )
                WhitelistMySQLStorage.initialization(parameter)
                WhitelistMySQLStorage.createTable()
            }
        }

        Whitelist.load()

        DelayUpdate.run()

        proxy.pluginManager.registerListener(this, ServerConnect)
        proxy.pluginManager.registerCommand(this, WhitelistBungeeTabExecutor())
        proxy.pluginManager.registerListener(this, ServerConnect)
    }

    override fun onDisable() {
        proxy.console.sendMessage(TextComponent(prefix + "插件开始卸载 " + description.version))

        DelayUpdate.stop()
        DelayUpdate.clear()

        if ("mysql" == storage) {
            WhitelistMySQLStorage.close()
        }
    }

}