package ru.xllifi.rewards

import kotlinx.serialization.json.Json
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.xllifi.rewards.calendar.Calendar
import ru.xllifi.rewards.commands.registerCommands
import ru.xllifi.rewards.config.*
import ru.xllifi.rewards.config.TextureManager
import ru.xllifi.rewards.calendar.sql.CollectedCellTable
import ru.xllifi.rewards.locker.items.setupPrefixPlaceholder
import ru.xllifi.rewards.locker.items.setupSuffixPlaceholder
import ru.xllifi.rewards.locker.sql.CollectedLockerItemTable
import ru.xllifi.rewards.progression.sql.CollectedProgressionTiersTable
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

class Main : ModInitializer {
  companion object {
    const val MOD_ID = "rewards"
    val logger: Logger = LoggerFactory.getLogger(MOD_ID)

    val configDir: Path = FabricLoader.getInstance().configDir.resolve(MOD_ID)
    val globalConfigFile: Path = configDir.resolve("config.json")

    // Use standard delegation or lateinits
    val globalConfigManager = ConfigManager(Json(defaultJson) { explicitNulls = true })
    lateinit var globalConfig: GlobalConfig
    lateinit var database: Database

    fun refreshGlobalConfig() {
      globalConfig = globalConfigManager.loadFile(globalConfigFile, defaultGlobalConfig)
    }
  }

  override fun onInitialize() {
    logger.info("Rewards mod initializing")
    // Create a config directory
    if (configDir.notExists()) configDir.createDirectories()
    // Load config
    globalConfig = globalConfigManager.loadFile(globalConfigFile, defaultGlobalConfig)

    // Initialize Database
    try {
      database = Database.connect(
        url = globalConfig.database.jdbcUrl,
        driver = globalConfig.database.driver,
        user = globalConfig.database.username,
        password = globalConfig.database.password,
      )

      transaction(database) {
        SchemaUtils.create(CollectedCellTable)
        SchemaUtils.create(CollectedProgressionTiersTable)
        SchemaUtils.create(CollectedLockerItemTable)
      }

      logger.info("Rewards connecting to DB: ${database.url}")
    } catch (e: Exception) {
      logger.error("Failed to initialize database!", e)
    }

    // Add textures from config to polymer resource pack
    TextureManager.copyDefaultTextures()
    TextureManager.registerPolymerResourceLoader()

    // Commands
    registerCommands()

    // Loading server attachments
    ServerLifecycleEvents.SERVER_STARTED.register { server -> server.setServerAttachment() }
    ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { server, _, _ -> server.setServerAttachment() }

    // Setup notifications
    ServerPlayerEvents.JOIN.register { player ->
      Calendar.notifyPlayerOfAvailableUncollectedCells(globalConfig, player)
    }

    // Setup placeholders
    setupPrefixPlaceholder()
    setupSuffixPlaceholder()

    logger.info("Rewards mod initialized!")
  }
}