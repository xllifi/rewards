package ru.xllifi.rewards

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences
import org.jetbrains.exposed.v1.core.InternalApi
import org.jetbrains.exposed.v1.core.SqlLogger
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.StatementContext
import org.jetbrains.exposed.v1.core.statements.expandArgs
import org.jetbrains.exposed.v1.core.transactions.currentTransaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.xllifi.rewards.commands.RewardsCommands
import ru.xllifi.rewards.config.Configs
import ru.xllifi.rewards.config.ConfigsManager
import ru.xllifi.rewards.serializers.JsonSerializers
import ru.xllifi.rewards.serializers.text.Component
import ru.xllifi.rewards.sql.PlayerData
import ru.xllifi.rewards.sql.PlayerDatas
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.notExists

const val modId = "rewards"
val configDir: Path = FabricLoader.getInstance().configDir.resolve(modId)
val dbPath: Path = configDir.resolve("database.db")
val logger: Logger = LoggerFactory.getLogger(modId)

object Main : ModInitializer {
  private var _audiencesFuture = CompletableFuture<MinecraftServerAudiences>()
  internal val audiences: MinecraftServerAudiences get() = _audiencesFuture.get()
  private var _jsonSerializersFuture = CompletableFuture<JsonSerializers>()
  internal val jsonSerializers: JsonSerializers get() = _jsonSerializersFuture.get()
  private var _configs = CompletableFuture<Configs>()
  internal val configs: Configs get() = _configs.get()
  private var _database = Database.connect("jdbc:sqlite:${dbPath}", "org.sqlite.JDBC")
  internal val database: Database get() = _database

  override fun onInitialize() {
    if (configDir.notExists()) configDir.createDirectories()
    if (dbPath.notExists()) dbPath.createFile()
    transaction {
      SchemaUtils.create(PlayerDatas)

      val rand = UUID.randomUUID()
      if (PlayerData.count(PlayerDatas.id eq rand) == 0L) {
        val playerData = PlayerData.new(id = rand) {
          collectedCalendarCells = emptyMap()
        }
      }
    }
    ServerLifecycleEvents.SERVER_STARTING.register { server ->
      _audiencesFuture.complete(MinecraftServerAudiences.of(server))
      _jsonSerializersFuture.complete(JsonSerializers(server))
      _configs.complete(ConfigsManager.loadConfigs())
    }
    ServerPlayerEvents.JOIN.register { player ->
      transaction {
        if (PlayerData.count(PlayerDatas.id eq player.uuid) == 0L) {
          val playerData = PlayerData.new(id = player.uuid) {
            collectedCalendarCells = emptyMap()
          }
          player.sendMessage { Component.text("Created you: ${playerData.id}") }
        }

        val getPlayerData = PlayerData[player.uuid]
        player.sendMessage { Component.text("Found you: ${getPlayerData.id}, collected: ${getPlayerData.collectedCalendarCells}") }
      }
    }
    registerCommands()
  }

  fun registerCommands() {
    CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, environment ->
      dispatcher.register(
        RewardsCommands().register()
      )
    }
  }
}