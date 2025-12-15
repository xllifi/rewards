package ru.xllifi.rewards.config.textures

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils
import ru.xllifi.rewards.configDir
import ru.xllifi.rewards.modId
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.notExists
import kotlin.io.path.readBytes

object TextureManager {
  val texturesPath: Path = configDir.resolve("textures")
  fun registerResourceLoader() {
    PolymerResourcePackUtils.addModAssets(modId)
    if (texturesPath.notExists()) texturesPath.createDirectories()

    PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register { builder ->
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
    "0": "$modId:item/<NAME>"
  }
}"""
