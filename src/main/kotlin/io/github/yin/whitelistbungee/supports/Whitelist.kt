package io.github.yin.whitelistbungee.supports

import io.github.yin.whitelistbungee.Main
import io.github.yin.whitelistbungee.storages.WhitelistMySQLStorage
import io.github.yin.whitelistbungee.storages.WhitelistYAMLStorage
import net.md_5.bungee.api.ProxyServer


object Whitelist {

    lateinit var playerNames: MutableSet<String>
    fun load() {
        ProxyServer.getInstance().scheduler.runAsync(Main.instance) {
            when (Main.storage) {
                "yaml" -> playerNames = WhitelistYAMLStorage.getPlayerNames().toMutableSet()
                "mysql" -> playerNames = WhitelistMySQLStorage.getPlayerNames()
            }
        }
    }


    fun add(playername: String): Boolean {
        if (playerNames.add(playername)) {
            ProxyServer.getInstance().scheduler.runAsync(Main.instance) {
                DelayUpdate.update(DelayUpdate.ActionType.ADD, playername)
            }
            return true
        } else {
            return false
        }
    }

    fun remove(playername: String): Boolean {
        if (playerNames.remove(playername)) {
            ProxyServer.getInstance().scheduler.runAsync(Main.instance) {
                DelayUpdate.update(DelayUpdate.ActionType.REMOVE, playername)
            }
            return true
        } else {
            return false
        }
    }


}