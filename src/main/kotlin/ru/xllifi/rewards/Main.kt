package ru.xllifi.rewards

import de.phyrone.brig.wrapper.literal
import kotlinx.serialization.json.Json
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.xllifi.rewards.commands.DebugCommands
import ru.xllifi.rewards.commands.RewardsCommands
import ru.xllifi.rewards.config.ConfigManager
import ru.xllifi.rewards.config.defaultMainConfig
import ru.xllifi.rewards.config.localDbPath
import ru.xllifi.rewards.config.setServerAttachment
import ru.xllifi.rewards.serializers.text.Component
import ru.xllifi.rewards.sql.PlayerData
import ru.xllifi.rewards.sql.PlayerDatas
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.notExists

const val modId = "rewards"
val configDir: Path = FabricLoader.getInstance().configDir.resolve(modId)
val mainConfigFile: Path = configDir.resolve("config.json")
val logger: Logger = LoggerFactory.getLogger(modId)

object Main : ModInitializer {
  val globalConfigManager = ConfigManager(Json)
  val globalConfig = globalConfigManager.loadFile(mainConfigFile, defaultMainConfig)
  val database =
    with(globalConfig.database) {
      Database.connect(
        url = jdbcUrl,
        driver = driver,
        user = username,
        password = password,
      )
    }

  override fun onInitialize() {
    setup()

    ServerLifecycleEvents.SERVER_STARTED.register { server -> server.setServerAttachment() }
    ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { server, _, _ -> server.setServerAttachment() }

    registerCommands()

    if (FabricLoader.getInstance().isDevelopmentEnvironment) {
      ServerPlayerEvents.JOIN.register { player ->
        transaction {
          if (PlayerData.count(PlayerDatas.id eq player.uuid) == 0L) {
            val playerData = PlayerData.new(id = player.uuid) {
              collectedCalendarCells = emptyMap()
            }
            player.sendMessage { Component.text("[Dev] Created you: ${playerData.id}") }
          }

          val getPlayerData = PlayerData[player.uuid]
          player.sendMessage { Component.text("[Dev] Found you: ${getPlayerData.id}, collected: ${getPlayerData.collectedCalendarCells}") }
        }
      }
    }
  }

  fun setup() {
    if (configDir.notExists()) configDir.createDirectories()
    if (localDbPath.notExists()) localDbPath.createFile()
    transaction(database) { SchemaUtils.create(PlayerDatas) }
  }

  fun registerCommands() {
    CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, environment ->
      dispatcher.literal("rewards") {
        with(RewardsCommands) { register() }
        if (FabricLoader.getInstance().isDevelopmentEnvironment) {
          with(DebugCommands) { register() }
        }
      }
    }
  }
}