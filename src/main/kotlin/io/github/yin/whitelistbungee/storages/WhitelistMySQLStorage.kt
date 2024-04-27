package io.github.yin.whitelistbungee.storages

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.yin.whitelistbungee.Main
import io.github.yin.whitelistbungee.supports.DelayUpdate
import net.md_5.bungee.api.ProxyServer
import java.sql.Connection


object WhitelistMySQLStorage {

    private lateinit var dataSource: HikariDataSource

    class Parameter(
        val jdbcUrl: String,
        val username: String,
        val password: String,
        val maximumPoolSize: Int,
        val minimumIdle: Int,
        val connectionTimeout: Long,
        val idleTimeout: Long,
        val maxLifetime: Long
    )

    fun initialization(parameter: Parameter) {
        val config = HikariConfig()
        config.jdbcUrl = parameter.jdbcUrl
        config.username = parameter.username
        config.password = parameter.password
        config.maximumPoolSize = parameter.maximumPoolSize
        config.minimumIdle = parameter.minimumIdle
        config.connectionTimeout = parameter.connectionTimeout
        config.idleTimeout = parameter.idleTimeout
        config.maxLifetime = parameter.maxLifetime
        dataSource = HikariDataSource(config)
    }

    private fun getConnection(): Connection {
        return dataSource.connection
    }

    fun close() {
        dataSource.close()
    }

    private val tablePrefix = ConfigurationYAMLStorage.configuration.getString("players.mysql.table-prefix")
    fun createTable() {
        ProxyServer.getInstance().scheduler.runAsync(Main.instance) {
            val table = tablePrefix + "players"
            val sql = """
                CREATE TABLE IF NOT EXISTS $table (
                id INT AUTO_INCREMENT PRIMARY KEY,
                names VARCHAR(64)
                );
                """.trimIndent()
            getConnection().createStatement().use { statement ->
                statement.executeUpdate(sql)
            }
        }
    }

    fun getPlayerNames(): MutableSet<String> {
        val playerNames: MutableSet<String> = HashSet()
        val table = tablePrefix + "players"
        val sql = "SELECT names FROM $table"
        getConnection().prepareStatement(sql).use { preparedStatement ->
            val resultSet = preparedStatement.executeQuery()
            while (resultSet.next()) {
                playerNames.add(resultSet.getString("names"))
            }
        }
        return playerNames
    }

    fun insertPlayerName(playerName: String) {
        val table = tablePrefix + "players"
        val sql = "INSERT INTO $table (names) VALUES (?)"
        getConnection().prepareStatement(sql).use { preparedStatement ->
            preparedStatement.setString(1, playerName)
            preparedStatement.executeUpdate()
        }
    }

    fun insertPlayerNames(list: Set<String>) {
        val table = tablePrefix + "players"
        val sql = "INSERT INTO $table (names) VALUES (?)"
        getConnection().prepareStatement(sql).use { preparedStatement ->
            for (value in list) {
                preparedStatement.setString(1, value)
                preparedStatement.addBatch()
            }
            preparedStatement.executeBatch()
        }
    }

    fun insertPlayerNamesIgnore(list: Set<String>) {
        val table = tablePrefix + "players"
        // 使用 INSERT IGNORE 替代 INSERT，当遇到重复键时跳过而不是报错
        val sql = "INSERT IGNORE INTO $table (names) VALUES (?)"
        getConnection().use { connection ->
            connection.prepareStatement(sql).use { preparedStatement ->
                for (value in list) {
                    preparedStatement.setString(1, value)
                    preparedStatement.addBatch()
                }
                preparedStatement.executeBatch()
            }
        }
    }

    fun removePlayerName(playerName: String) {
        val table = tablePrefix + "players"
        val sql = "DELETE FROM $table WHERE names = ?"
        getConnection().prepareStatement(sql).use { preparedStatement ->
            preparedStatement.setString(1, playerName)
            preparedStatement.executeUpdate()
        }
    }

    fun removePlayerNames(list: Set<String>) {
        val table = tablePrefix + "players"
        val sql = "DELETE FROM $table WHERE names = ?"
        getConnection().prepareStatement(sql).use { preparedStatement ->
            for (value in list) {
                preparedStatement.setString(1, value)
                preparedStatement.addBatch()
            }
            preparedStatement.executeBatch()
        }
    }

    fun updatePlayers(map: Map<String, DelayUpdate.ActionType>) {
        val table = tablePrefix + "players"
        val connection = getConnection()

        connection.autoCommit = false

        val add = connection.prepareStatement("INSERT IGNORE INTO $table (names) VALUES (?)")
        val remove = connection.prepareStatement("DELETE FROM $table WHERE names = ?")

        for ((key, value) in map) {
            when (value) {
                DelayUpdate.ActionType.ADD -> {
                    add.setString(1, key)
                    add.executeUpdate()
                }

                DelayUpdate.ActionType.REMOVE -> {
                    remove.setString(1, key)
                    remove.executeUpdate()
                }
            }
        }

        connection.commit()
        connection.close()
    }


}