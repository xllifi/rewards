package ru.xllifi.rewards.config

import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import net.fabricmc.loader.api.FabricLoader
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.configDir
import ru.xllifi.rewards.logger
import ru.xllifi.rewards.modId
import ru.xllifi.rewards.serializers.text.Component
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.notExists
import kotlin.io.path.outputStream

val defaultMainConfig = MainConfig(
  prefix = Component.text("[Rewards]")
)

@Serializable
data class MainConfig(
  val prefix: Component
)

data class Configs(
  val mainConfig: MainConfig,
  val calendars: List<Calendar>,
  val progressions: List<Progression>,
)

object ConfigsManager {
  val json = Main.jsonSerializers.json
  val mainConfigFile: Path = configDir.resolve("config.json")
  val calendarsDir: Path = configDir.resolve("calendars")
  val progressionsDir: Path = configDir.resolve("progressions")

  fun loadConfigs(): Configs {
    val mainConfig = loadFile(mainConfigFile, defaultMainConfig)

    if (calendarsDir.notExists()) calendarsDir.createDirectory()
    val calendars: List<Calendar> =
      Files.walk(calendarsDir).use { paths ->
        paths.filter { Files.isRegularFile(it) && it.extension == "json" }
          .map { loadFile<Calendar>(it, null) }
          .toList()
      }

    if (progressionsDir.notExists()) progressionsDir.createDirectory()
    val progressions: List<Progression> =
      Files.walk(progressionsDir).use { paths ->
        paths.filter { Files.isRegularFile(it) }
          .map { loadFile<Progression>(it, null) }
          .toList()
      }

    return Configs(
      mainConfig = mainConfig,
      calendars = calendars,
      progressions = progressions,
    )
  }

  private inline fun <reified T : Any> saveFile(path: Path, config: T) {
    try {
      val yamlString = json.encodeToString(config)
      path.outputStream().write(yamlString.encodeToByteArray())
      logger.info("File saved successfully to $path")
    } catch (e: Exception) {
      logger.error("Failed to save file: ${e.localizedMessage}")
    }
  }

  private inline fun <reified T : Any> loadFile(path: Path, default: T?): T {
    try {
      if (path.notExists()) {
        if (default != null) {
          logger.info("File (${path}) not found, generating from default.")
          saveFile(path, default)
          return default
        } else {
          throw IOException("File at path \"$path\" doesn't exist and default is null!")
        }
      } else {
        return json.decodeFromString(path.inputStream().bufferedReader().readText())
      }
    } catch (e: Exception) {
      logger.error("Failed to load file. See trace below.")
      throw e
    }
  }
}