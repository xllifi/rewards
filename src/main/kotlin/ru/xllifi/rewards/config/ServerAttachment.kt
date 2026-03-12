@file:Suppress("UnstableApiUsage")

package ru.xllifi.rewards.config

import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences
import net.minecraft.commands.CommandSourceStack
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.calendar.Calendar
import ru.xllifi.rewards.cosmetic.CosmeticDef
import ru.xllifi.rewards.cosmetic.CosmeticKind
import ru.xllifi.rewards.progression.Progression
import ru.xllifi.rewards.serializers.JsonSerializers
import java.nio.file.Path
import kotlin.io.path.createDirectories

val serverAttachmentType: AttachmentType<ServerAttachment> = AttachmentRegistry.create(
  Identifier.fromNamespaceAndPath(Main.MOD_ID, "server_attachment")
)

data class ServerAttachment(
  val audiences: MinecraftServerAudiences,
  val jsonSerializers: JsonSerializers,
  val configManager: ConfigManager,
  val calendars: List<Calendar>,
  val progressions: List<Progression>,
  val cosmetics: Map<CosmeticKind, Map<String, CosmeticDef>>
)

val calendarsDir: Path = Main.configDir.resolve("calendars")
val progressionsDir: Path = Main.configDir.resolve("progressions")
val cosmeticsDir: Path = Main.configDir.resolve("cosmetics")

private val MinecraftServer.defaultServerAttachment: ServerAttachment
  get() {
    // Ensure config dirs exists
    calendarsDir.createDirectories()
    progressionsDir.createDirectories()
    cosmeticsDir.createDirectories()

    val jsonSerializers = JsonSerializers(this)
    val configManager = ConfigManager(jsonSerializers.json)

    return ServerAttachment(
      audiences = MinecraftServerAudiences.of(this),
      jsonSerializers = jsonSerializers,
      configManager = configManager,
      calendars = configManager.loadDir(calendarsDir),
      progressions = configManager.loadDir(progressionsDir),
      cosmetics = configManager.loadDir<CosmeticDef>(cosmeticsDir)
        .groupBy { it.kind }
        .mapValues { (_, cosmeticDefsOfKind) ->
          cosmeticDefsOfKind.associateBy { it.id }
        }
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