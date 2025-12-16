package ru.xllifi.rewards

import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.xllifi.rewards.commands.registerCommands
import ru.xllifi.rewards.config.*
import ru.xllifi.rewards.config.TextureManager
import ru.xllifi.rewards.sql.CollectedCellTable
import ru.xllifi.rewards.sql.isCellCollectedBy
import ru.xllifi.rewards.utils.plus
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.notExists
import kotlin.time.Clock

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

    // Loading server attachment
    ServerWorldEvents.LOAD.register { server, _ -> server.setServerAttachment() }
    ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { server, _, _ -> server.setServerAttachment() }

    // Available cell notification
    ServerPlayerEvents.JOIN.register { player ->
      if (globalConfig.calendarReminders) {
        val serverAttachment = player.level().server.getServerAttachment()
        for (calendar in serverAttachment.calendars) {
          val todayDate = Clock.System.now().toLocalDateTime(globalConfig.timeZoneForSure).date
          val calendarStartDate = calendar.startDay.toLocalDateTime(globalConfig.timeZoneForSure).date

          val currentCalendarDay = (todayDate - calendarStartDate).days
          val todayCell = calendar.cells[currentCalendarDay]

          if (!calendar.isCellCollectedBy(player, todayCell)) {
            player.sendSystemMessage(
              Component.translatable(
                "rewards.calendar.notification",
                serverAttachment.audiences.asNative(calendar.title)
              ) +
                Component.literal("\n") +
                Component.translatable("rewards.calendar.notification.action").withStyle(
                  Style.EMPTY
                    .withUnderlined(true)
                    .withColor(ChatFormatting.YELLOW)
                    .withClickEvent(ClickEvent.RunCommand("/calendar open ${calendar.id}"))
                )
            )
          }
        }
      }
    }
  }

  fun setup() {
    if (configDir.notExists()) configDir.createDirectories()
    if (localDbPath.notExists()) localDbPath.createFile()
    transaction(database) { SchemaUtils.create(CollectedCellTable) }
  }
}