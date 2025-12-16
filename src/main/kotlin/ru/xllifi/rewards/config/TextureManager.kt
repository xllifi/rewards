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
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.notExists
import kotlin.io.path.readBytes
import kotlin.jvm.optionals.getOrNull

object TextureManager {
  val texturesPath: Path = configDir.resolve("textures")
  val modContainer = FabricLoader.getInstance().getModContainer(modId).get()
  fun copyDefaultTextures() {
    // Ensure config dir exists
    texturesPath.createDirectories()

    val defaultTexturesPath = modContainer.findPath("assets/$modId/default_textures").getOrNull()
    if (defaultTexturesPath == null) {
      logger.error("No default textures! Please tell the developer!")
      return
    }

    val pngs = Files.walk(defaultTexturesPath).filter { it.isRegularFile() && it.extension == "png" }
    for (path in pngs) {
      val targetPath = texturesPath.resolve(path.name)
      try {
        path.copyTo(targetPath)
      } catch (_: FileAlreadyExistsException) {
        logger.debug("{} already exists, not copying asset {}!", targetPath, path)
      }
    }
  }
  fun registerResourceLoader() {
    if (texturesPath.notExists()) texturesPath.createDirectories()
    copyDefaultTextures()

    PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register { builder ->
      // From assets
      builder.addData("assets/$modId/models/item/gui18.json", modContainer.findPath("assets/$modId/models/item/gui18.json").get().readBytes())

      // From configs
      val pngs = Files.walk(texturesPath).filter { it.isRegularFile() && it.extension == "png" }
      pngs.forEach {
        val bytes = it.readBytes()
        builder.addData("assets/$modId/textures/item/${it.name}", bytes)
        builder.addStringData(
          "assets/$modId/items/${it.nameWithoutExtension}.json",
          itemDefinitionTemplate.replace("<NAME>", it.nameWithoutExtension)
        )
        builder.addStringData(
          "assets/$modId/models/item/${it.nameWithoutExtension}.json",
          itemModelDefinitionTemplate.replace("<NAME>", it.nameWithoutExtension)
        )
      }
    }
  }
}

const val itemDefinitionTemplate: String = """{
  "oversized_in_gui": true,
  "model": {
    "type": "minecraft:model",
    "model": "$modId:item/<NAME>"
  }
}"""
const val itemModelDefinitionTemplate: String = """{
  "parent": "$modId:item/gui18",
  "textures": {
    "layer0": "$modId:item/<NAME>"
  }
}"""
