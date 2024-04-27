package io.github.yin.whitelistbungee.commands

import io.github.yin.whitelistbungee.Main
import io.github.yin.whitelistbungee.storages.ConfigurationYAMLStorage
import io.github.yin.whitelistbungee.storages.MessageYAMLStorage
import io.github.yin.whitelistbungee.storages.WhitelistMySQLStorage
import io.github.yin.whitelistbungee.storages.WhitelistYAMLStorage
import io.github.yin.whitelistbungee.supports.DelayUpdate
import io.github.yin.whitelistbungee.supports.TextProcess
import io.github.yin.whitelistbungee.supports.Whitelist
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor
import net.md_5.bungee.config.Configuration
import java.util.*


class WhitelistBungeeTabExecutor : Command("whitelistbungee", "whitelistbungee.command", "wb", "wl", "wlb"),
    TabExecutor {
    private fun help(sender: CommandSender) {
        MessageYAMLStorage.configuration.getStringList("command.help").forEach {
            sender.sendMessage(TextComponent(it))
        }
    }

    private fun add(sender: CommandSender, playerName: String) {
        if (Whitelist.add(playerName)) {

            sender.sendMessage(
                TextComponent(
                    TextProcess.replace(
                        MessageYAMLStorage.configuration.getString("command.add-player-success"),
                        playerName
                    )
                )
            )

        } else {

            sender.sendMessage(
                TextComponent(
                    TextProcess.replace(
                        MessageYAMLStorage.configuration.getString("command.add-player-failed"),
                        playerName
                    )
                )
            )

        }
    }

    private fun remove(sender: CommandSender, playerName: String) {
        if (Whitelist.remove(playerName)) {

            sender.sendMessage(
                TextComponent(
                    TextProcess.replace(
                        MessageYAMLStorage.configuration.getString("command.remove-player-success"),
                        playerName
                    )
                )
            )

            ProxyServer.getInstance().getPlayer(playerName)?.disconnect(
                TextComponent(MessageYAMLStorage.configuration.getString("command.remove-player-kick"))
            )
        } else {

            sender.sendMessage(
                TextComponent(
                    TextProcess.replace(
                        MessageYAMLStorage.configuration.getString("command.remove-player-failed"),
                        playerName
                    )
                )
            )

        }
    }

    private fun transform(sender: CommandSender, argument: String) {
        when (argument) {
            "yaml" -> {
                if ("yaml".equals(Main.storage, ignoreCase = true)) {

                    sender.sendMessage(
                        TextComponent(
                            TextProcess.replace(
                                MessageYAMLStorage.configuration.getString("command.transform-already"),
                                argument
                            )
                        )
                    )

                } else {
                    ProxyServer.getInstance().scheduler.runAsync(Main.instance) {
                        WhitelistMySQLStorage.close()

                        WhitelistYAMLStorage.initialization(Main.instance.dataFolder)
                        WhitelistYAMLStorage.load()
                        WhitelistYAMLStorage.save(Whitelist.playerNames)

                        sender.sendMessage(
                            TextComponent(
                                TextProcess.replace(
                                    MessageYAMLStorage.configuration.getString("command.transform-complete"),
                                    argument
                                )
                            )
                        )

                        Main.storage = "yaml"
                        Whitelist.load()

                    }
                }
            }

            "mysql" -> {
                if ("mysql".equals(Main.storage, ignoreCase = true)) {
                    sender.sendMessage(
                        TextComponent(
                            TextProcess.replace(
                                MessageYAMLStorage.configuration.getString("command.transform-already"),
                                argument
                            )
                        )
                    )
                } else {
                    ProxyServer.getInstance().scheduler.runAsync(Main.instance) {
                        val configuration: Configuration = ConfigurationYAMLStorage.configuration
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
                        Thread.sleep(1000)
                        WhitelistMySQLStorage.insertPlayerNames(Whitelist.playerNames)

                        sender.sendMessage(
                            TextComponent(
                                TextProcess.replace(
                                    MessageYAMLStorage.configuration.getString("command.transform-complete"),
                                    argument
                                )
                            )
                        )

                        Main.storage = "mysql"
                        Whitelist.load()
                    }
                }
            }
        }
    }

    override fun execute(sender: CommandSender, arguments: Array<out String>) {
        when (arguments.size) {
            0 -> help(sender)
            1 -> {
                when (arguments[0].lowercase(Locale.getDefault())) {
                    "help" -> help(sender)
                    "list" -> sender.sendMessage(
                        TextComponent(
                            TextProcess.replace(
                                MessageYAMLStorage.configuration.getString("command.list-size"),
                                Whitelist.playerNames.size.toString()
                            )
                        )
                    )

                    "reload" -> {

                        ProxyServer.getInstance().scheduler.runAsync(Main.instance) {
                            DelayUpdate.clear()

                            ConfigurationYAMLStorage.load()
                            MessageYAMLStorage.initialization(Main.instance.dataFolder)
                            MessageYAMLStorage.load()
                            WhitelistYAMLStorage.initialization(Main.instance.dataFolder)
                            WhitelistYAMLStorage.load()
                            Whitelist.load()

                            TextProcess
                                .replaceList(
                                    MessageYAMLStorage.configuration.getStringList("command.reload"),
                                    MessageYAMLStorage.language,
                                    Whitelist.playerNames.size.toString()
                                )
                                .forEach { sender.sendMessage(TextComponent(it)) }
                        }

                    }
                }
            }

            2 -> {
                when (arguments[0].lowercase(Locale.getDefault())) {
                    "add" -> add(sender, arguments[1])
                    "remove" -> remove(sender, arguments[1])
                    "transform" -> transform(sender, arguments[1].lowercase())
                }
            }
        }
    }

    override fun onTabComplete(sender: CommandSender, arguments: Array<out String>): List<String> {
        when (arguments.size) {
            1 -> {
                return listMatches(arguments[0], mutableListOf("help", "list", "add", "remove", "transform", "reload"))
            }
            2 -> {
                if ("remove".equals(arguments[0], ignoreCase = true)) {
                    return listMatches(arguments[1], Whitelist.playerNames)
                } else if ("transform".equals(arguments[0], ignoreCase = true)) {
                    return listMatches(arguments[1], mutableListOf("yaml", "mysql"))
                }
            }
        }
        return emptyList()
    }

    private fun listMatches(argument: String, suggest: Iterable<String>): MutableList<String> {
        return suggest.filter { it.contains(argument) }.toMutableList()
    }

}
