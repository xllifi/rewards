package ru.xllifi.rewards

import kotlinx.serialization.json.Json
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.xllifi.rewards.commands.registerCommands
import ru.xllifi.rewards.config.*
import ru.xllifi.rewards.config.TextureManager
import ru.xllifi.rewards.sql.CollectedCellTable
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.notExists

const val modId = "rewards"
val configDir: Path = FabricLoader.getInstance().configDir.resolve(modId)
val mainConfigFile: Path = configDir.resolve("config.json")
fun loadMainConfig() = Main.globalConfigManager.loadFile(mainConfigFile, defaultMainConfig)
val logger: Logger = LoggerFactory.getLogger(modId)

object Main : ModInitializer {
  val globalConfigManager = ConfigManager(Json(defaultJson) { explicitNulls = true })
  var globalConfig = loadMainConfig()
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

    ServerLifecycleEvents.SERVER_STARTED.register { server -> server.setServerAttachment() }
    ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { server, _, _ -> server.setServerAttachment() }
  }

  fun setup() {
    if (configDir.notExists()) configDir.createDirectories()
    if (localDbPath.notExists()) localDbPath.createFile()
    transaction(database) { SchemaUtils.create(CollectedCellTable) }
  }
}