package ru.xllifi.rewards.mainmenu

import eu.pb4.sgui.api.elements.GuiElementBuilder
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import ru.xllifi.rewards.calendar.ui.CalendarScreen
import ru.xllifi.rewards.config.ServerAttachment
import ru.xllifi.rewards.config.getServerAttachment
import ru.xllifi.rewards.serializers.time.dayHumanReadable
import ru.xllifi.rewards.utils.ui.PagedScreen

class DiscoverCalendarsGui(
  player: ServerPlayer,
  callback: (() -> Unit)? = null,
) : PagedScreen(player, callback) {
  val attachment: ServerAttachment = player.level().server.getServerAttachment()

  init {
    this.title = Component.translatable("rewards.calendar.discovery.title")
    this.refreshOpen()
  }

  override val pageAmount: Int
    get() = attachment.calendars.size / PAGE_SIZE

  override fun getElement(id: Int): DisplayElement? {
    return if (id < attachment.calendars.size) {
      val calendar = attachment.calendars[id]
      DisplayElement.of(
        GuiElementBuilder(calendar.displayItem)
          .setItemName(attachment.audiences.asNative(calendar.title))
          .setLore(
            listOf(
              Component.empty(),
              Component.translatable(
                "rewards.calendar.discovery.entry.lore.duration",
                calendar.startDay.dayHumanReadable(),
                calendar.startDay.plus(calendar.cells.size * 24, DateTimeUnit.HOUR).dayHumanReadable(),
              ).withStyle(ChatFormatting.GRAY),
              Component.empty(),
              Component.translatable("rewards.calendar.discovery.entry.lore.action")
                .withStyle(ChatFormatting.YELLOW),
            )
          )
          .setCallback { _, _, _ ->
            CalendarScreen(calendar, player, this::open)
          }
      )
    } else {
      null
    }
  }
}