package io.github.yin.whitelistbungee.listeners

import io.github.yin.whitelistbungee.storages.MessageYAMLStorage
import io.github.yin.whitelistbungee.supports.Whitelist
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

object ServerConnect : Listener {
    init {
        for (listenerInfo in ProxyServer.getInstance().config.listeners) {
            serverPriority = listenerInfo.serverPriority
        }
    }

    private lateinit var serverPriority: List<String>

    @EventHandler
    fun onServerConnect(event: ServerConnectEvent) {
        if (serverPriority.contains(event.target.name)) {
            return
        }

        val player = event.player
        if (!Whitelist.playerNames.contains(player.name)) {
            player.sendMessage(TextComponent(MessageYAMLStorage.configuration.getString("whitelist.apply")))
            event.target = ProxyServer.getInstance().getServerInfo(serverPriority[0])
        }
    }
}