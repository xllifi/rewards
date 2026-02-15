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
import ru.xllifi.rewards.playerlocker.items.LockerItem
import ru.xllifi.rewards.playerlocker.items.setupPrefixPlaceholder
import ru.xllifi.rewards.playerlocker.items.setupSuffixPlaceholder
import ru.xllifi.rewards.playerlocker.sql.CollectedLockerItemTable
import ru.xllifi.rewards.progression.sql.CollectedProgressionTiersTable
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.notExists

const val modId = "rewards"
val configDir: Path = FabricLoader.getInstance().configDir.resolve(modId)
val globalConfigFile: Path = configDir.resolve("config.json")
fun loadGlobalConfig() = Main.globalConfigManager.loadFile(globalConfigFile, defaultGlobalConfig)
val logger: Logger = LoggerFactory.getLogger(modId)

object Main : ModInitializer {
  val globalConfigManager = ConfigManager(Json(defaultJson) { explicitNulls = true })
  var globalConfig = loadGlobalConfig()
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
    TextureManager.registerResourceLoader()
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
  }

  fun setup() {
    if (configDir.notExists()) configDir.createDirectories()
    if (localDbPath.notExists()) localDbPath.createFile()
    transaction(database) {
      SchemaUtils.create(CollectedCellTable)
      SchemaUtils.create(CollectedProgressionTiersTable)
      SchemaUtils.create(CollectedLockerItemTable)
    }
  }
}