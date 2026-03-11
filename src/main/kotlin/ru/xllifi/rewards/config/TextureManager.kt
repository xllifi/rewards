package ru.xllifi.rewards.config

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils
import net.fabricmc.loader.api.FabricLoader
import ru.xllifi.rewards.Main
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.readBytes
import kotlin.jvm.optionals.getOrNull

object TextureManager {
  val localTexturesPath: Path = Main.configDir.resolve("textures")
  val modContainer = FabricLoader.getInstance().getModContainer(Main.MOD_ID).get()
  fun copyDefaultTextures() {
    // Ensure config dir exists
    localTexturesPath.createDirectories()

    val zipTexturesPath = modContainer.findPath("assets/${Main.MOD_ID}/textures").getOrNull()
    if (zipTexturesPath == null) {
      Main.logger.error("No default textures! Please tell the developer!")
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
        Main.logger.debug("{} already exists, not copying asset {}!", localTargetPath, path)
      }
    }
  }
  fun registerPolymerResourceLoader() {
    PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register { builder ->
      // From assets
      builder.addData("assets/${Main.MOD_ID}/models/item/gui18.json", modContainer.findPath("assets/${Main.MOD_ID}/models/item/gui18.json").get().readBytes())

      // From configs
      val pngs = Files.walk(localTexturesPath).filter { it.isRegularFile() && it.extension == "png" }
      pngs.forEach { path ->
        val assetName = localTexturesPath.relativize(path).toString().split(".").first()
        val bytes = path.readBytes()
        builder.addData("assets/${Main.MOD_ID}/textures/item/${assetName}.png", bytes)
        builder.addStringData(
          "assets/${Main.MOD_ID}/items/${assetName}.json",
          itemDefinitionTemplate.replace("<NAME>", assetName)
        )
        builder.addStringData(
          "assets/${Main.MOD_ID}/models/item/${assetName}.json",
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
    "model": "${Main.MOD_ID}:item/<NAME>",
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
  "parent": "${Main.MOD_ID}:item/gui18",
  "textures": {
    "layer0": "${Main.MOD_ID}:item/base",
    "layer1": "${Main.MOD_ID}:item/<NAME>"
  }
}"""
