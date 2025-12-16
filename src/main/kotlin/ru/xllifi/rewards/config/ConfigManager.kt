package ru.xllifi.rewards.config

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import ru.xllifi.rewards.logger
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.notExists
import kotlin.io.path.outputStream
import kotlin.reflect.jvm.jvmName

@OptIn(ExperimentalSerializationApi::class)
val defaultJson = Json {
  ignoreUnknownKeys = true
  isLenient = true
  encodeDefaults = true
  explicitNulls = false
  prettyPrint = true
  namingStrategy = JsonNamingStrategy.SnakeCase
}

class ConfigManager(
  val json: Json = defaultJson
) {
  val explicitJson = Json(json) { explicitNulls = true }
  inline fun <reified T : Any> saveFile(path: Path, content: T) {
    try {
      val jsonString = explicitJson.encodeToString(content)
      path.outputStream().write(jsonString.encodeToByteArray())
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
        val ret: T = json.decodeFromString(path.inputStream().bufferedReader().readText())
        saveFile(path, ret)
        return ret
      }
    } catch (e: Exception) {
      logger.error("Failed to load $path. See trace below.")
      throw e
    }
  }

  inline fun <reified T : Any> loadDir(path: Path, extension: String = "json"): List<T> =
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
