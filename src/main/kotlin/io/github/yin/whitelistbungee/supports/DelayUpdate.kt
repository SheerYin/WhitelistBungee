package io.github.yin.whitelistbungee.supports


import io.github.yin.whitelistbungee.Main
import io.github.yin.whitelistbungee.storages.ConfigurationYAMLStorage
import io.github.yin.whitelistbungee.storages.WhitelistMySQLStorage
import io.github.yin.whitelistbungee.storages.WhitelistYAMLStorage
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit


object DelayUpdate {

    private lateinit var task: ScheduledTask

    private val instantLock = Any()
    private var instant: Instant? = null

    fun run() {
        task = ProxyServer.getInstance().scheduler.schedule(Main.instance, {
            ProxyServer.getInstance().scheduler.runAsync(Main.instance) {
                synchronized(instantLock) {
                    if (instant != null && Instant.now().isAfter(instant)) {
                        clear()
                    }
                }
            }
        }, 0L, ConfigurationYAMLStorage.configuration.getLong("players.queue-clear-interval"), TimeUnit.SECONDS)
    }

    fun stop() {
        task.cancel()
    }

    private val map: MutableMap<String, ActionType> = mutableMapOf()

    fun update(action: ActionType, name: String) {
        synchronized(instantLock) {
            instant = Instant.now()
                .plus(Duration.ofSeconds(ConfigurationYAMLStorage.configuration.getLong("players.queue-add-time")))
            map[name] = action
        }
    }

    fun clear() {
        when (Main.storage) {
            "yaml" -> {
                WhitelistYAMLStorage.save(Whitelist.playerNames)
            }

            "mysql" -> {
                WhitelistMySQLStorage.updatePlayers(map)
            }
        }
        instant = null
        map.clear()
    }

    enum class ActionType {
        ADD,
        REMOVE
    }

}