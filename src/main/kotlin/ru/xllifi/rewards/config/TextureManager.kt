package ru.xllifi.rewards.config

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils
import net.fabricmc.loader.api.FabricLoader
import ru.xllifi.rewards.configDir
import ru.xllifi.rewards.logger
import ru.xllifi.rewards.modId
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.notExists
import kotlin.io.path.readBytes
import kotlin.jvm.optionals.getOrNull

object TextureManager {
  val localTexturesPath: Path = configDir.resolve("textures")
  val modContainer = FabricLoader.getInstance().getModContainer(modId).get()
  fun copyDefaultTextures() {
    // Ensure config dir exists
    localTexturesPath.createDirectories()

    val zipTexturesPath = modContainer.findPath("assets/$modId/default_textures").getOrNull()
    if (zipTexturesPath == null) {
      logger.error("No default textures! Please tell the developer!")
      return
    }

    val dirs = Files.walk(zipTexturesPath).filter { it.isDirectory() }
    for (path in dirs) {
      val localTargetPath = localTexturesPath.resolve(zipTexturesPath.relativize(path).toString())
      localTargetPath.createDirectories()
    }

    val pngs = Files.walk(zipTexturesPath).filter { it.isRegularFile() && it.extension == "png" }
    for (path in pngs) {
      val localTargetPath = localTexturesPath.resolve(zipTexturesPath.relativize(path).toString())
      try {
        path.copyTo(localTargetPath)
      } catch (_: FileAlreadyExistsException) {
        logger.debug("{} already exists, not copying asset {}!", localTargetPath, path)
      }
    }
  }
  fun registerResourceLoader() {
    if (localTexturesPath.notExists()) localTexturesPath.createDirectories()
    copyDefaultTextures()
    // TODO: Properly load textures for client when running this mod on client

    PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register { builder ->
      // From assets
      builder.addData("assets/$modId/models/item/gui18.json", modContainer.findPath("assets/$modId/models/item/gui18.json").get().readBytes())

      // From configs
      val pngs = Files.walk(localTexturesPath).filter { it.isRegularFile() && it.extension == "png" }
      pngs.forEach { path ->
        val assetName = localTexturesPath.relativize(path).toString().split(".").first()
        val bytes = path.readBytes()
        builder.addData("assets/$modId/textures/item/${assetName}.png", bytes)
        builder.addStringData(
          "assets/$modId/items/${assetName}.json",
          itemDefinitionTemplate.replace("<NAME>", assetName)
        )
        builder.addStringData(
          "assets/$modId/models/item/${assetName}.json",
          itemModelDefinitionTemplate.replace("<NAME>", assetName)
        )
      }
    }
  }
}

const val itemDefinitionTemplate: String = """{
  "oversized_in_gui": true,
  "model": {
    "type": "minecraft:model",
    "model": "$modId:item/<NAME>",
    "tints": [
      {
        "type": "minecraft:constant",
        "value": [1, 1, 1]
      },
      {
        "type": "minecraft:dye",
        "default": 9145227
      }
    ]
  }
}"""
const val itemModelDefinitionTemplate: String = """{
  "parent": "$modId:item/gui18",
  "textures": {
    "layer0": "$modId:item/blank",
    "layer1": "$modId:item/<NAME>"
  }
}"""
