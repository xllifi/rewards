package ru.xllifi.rewards.progression.ui

import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import ru.xllifi.rewards.config.ServerAttachment
import ru.xllifi.rewards.config.getServerAttachment
import ru.xllifi.rewards.utils.ui.PagedScreen

class DiscoverProgressionsScreen(
  player: ServerPlayer
) : PagedScreen(player, null) {
  val attachment: ServerAttachment = player.level().server.getServerAttachment()

  init {
    this.title = Component.translatable("rewards.progression.discovery.title")
  }

  override val pageAmount: Int
    get() = attachment.progressions.size / PAGE_SIZE

  override fun getElement(id: Int): DisplayElement? {
    return if (id < attachment.progressions.size) {
      val progression = attachment.progressions[id]
      DisplayElement.of(
        GuiElementBuilder(progression.displayItem)
          .setItemName(attachment.audiences.asNative(progression.title))
          .setLore(
            listOf(
              Component.empty(),
              Component.translatable("rewards.progression.discovery.entry.lore.x_tiers", progression.tiers.size)
                .withStyle(ChatFormatting.GRAY),
              Component.empty(),
              Component.translatable("rewards.progression.discovery.entry.lore.action")
                .withStyle(ChatFormatting.YELLOW),
            )
          )
          .setCallback { _, _, _ ->
            ProgressionScreen(progression, player, this::open)
          }
      )
    } else {
      null
    }
  }
}