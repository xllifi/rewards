package ru.xllifi.rewards.config

import kotlinx.serialization.json.Json
import ru.xllifi.rewards.logger
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.notExists
import kotlin.io.path.outputStream
import kotlin.reflect.jvm.jvmName

class ConfigManager(
  val json: Json
) {
  inline fun <reified T : Any> saveFile(path: Path, config: T) {
    try {
      val yamlString = json.encodeToString(config)
      path.outputStream().write(yamlString.encodeToByteArray())
      logger.info("File saved successfully to $path")
    } catch (e: Exception) {
      logger.error("Failed to save file: ${e.localizedMessage}")
    }
  }

  inline fun <reified T : Any> loadFile(path: Path, default: T?): T {
    try {
      if (path.notExists()) {
        if (default != null) {
          logger.info("File (${path}) not found, generating from default.")
          saveFile(path, default)
          return default
        } else {
          throw IOException("File at path $path doesn't exist and default is null!")
        }
      } else {
        return json.decodeFromString(path.inputStream().bufferedReader().readText())
      }
    } catch (e: Exception) {
      logger.error("Failed to load $path. See trace below.")
      throw e
    }
  }

  inline fun <reified T : Any> loadDirAsList(path: Path, extension: String = "json"): List<T> =
    Files.walk(path).use { paths ->
      paths.filter { Files.isRegularFile(it) && it.extension == extension }
        .toList()
        .mapNotNull {
          logger.info("Loading a ${T::class.simpleName ?: T::class.jvmName} from path $it")
          try {
            loadFile<T>(it, null)
          } catch (e: Exception) {
            logger.error(e.stackTraceToString())
            null
          }
        }
    }
}
