@file:Suppress("UnstableApiUsage")

package ru.xllifi.rewards.config

import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences
import net.minecraft.commands.CommandSourceStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import ru.xllifi.rewards.configDir
import ru.xllifi.rewards.modId
import ru.xllifi.rewards.serializers.JsonSerializers
import java.nio.file.Path

val serverAttachmentType: AttachmentType<ServerAttachment> = AttachmentRegistry.create(
  ResourceLocation.fromNamespaceAndPath(modId, "server_attachment")
)

data class ServerAttachment(
  val audiences: MinecraftServerAudiences,
  val jsonSerializers: JsonSerializers,
  val configManager: ConfigManager,
  val calendars: List<Calendar>,
  val progressions: List<Progression>,
) {
  override fun toString(): String {
    return "ServerAttachment(audiences=$audiences, " +
      "jsonSerializers=$jsonSerializers, " +
      "configManager=$configManager, " +
      "calendars=${jsonSerializers.json.encodeToString(calendars)}, " +
      "progressions=${jsonSerializers.json.encodeToString(progressions)})"
  }
}

val calendarsDir: Path = configDir.resolve("calendars")
val progressionsDir: Path = configDir.resolve("progressions")

private val MinecraftServer.defaultServerAttachment: ServerAttachment
  get() {
    val jsonSerializers = JsonSerializers(this)
    val configManager = ConfigManager(jsonSerializers.json)

    return ServerAttachment(
      audiences = MinecraftServerAudiences.of(this),
      jsonSerializers = jsonSerializers,
      configManager = configManager,
      calendars = configManager.loadDirAsList(calendarsDir),
      progressions = configManager.loadDirAsList(progressionsDir),
    )
  }

fun MinecraftServer.setServerAttachment(): ServerAttachment {
  val serverAttachment = this.defaultServerAttachment
  this.overworld().setAttached(serverAttachmentType, serverAttachment)
  return serverAttachment
}

fun MinecraftServer.getServerAttachment(): ServerAttachment {
  return this.overworld().getAttachedOrCreate(serverAttachmentType) { this.defaultServerAttachment }
}

fun CommandContext<CommandSourceStack>.getServerAttachment(): ServerAttachment =
  this.source.server.getServerAttachment()