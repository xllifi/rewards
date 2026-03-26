package ru.xllifi.rewards.mainmenu

import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import ru.xllifi.rewards.config.ServerAttachment
import ru.xllifi.rewards.config.getServerAttachment
import ru.xllifi.rewards.progression.ui.ProgressionGui
import ru.xllifi.rewards.utils.ui.PagedGui

class DiscoverProgressionsGui(
  player: ServerPlayer,
  callback: (() -> Unit)? = null,
) : PagedGui(player, callback) {
  val attachment: ServerAttachment = player.level().server.getServerAttachment()

  init {
    this.title = Component.translatable("rewards.progression.discovery.title")
    this.refreshOpen()
  }

  override val pageAmount: Int
    get() = attachment.progressions.size / PAGE_SIZE

  override fun getElement(index: Int): DisplayElement? {
    return if (index < attachment.progressions.size) {
      val progression = attachment.progressions[index]
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
            ProgressionGui(progression, player, this::open)
          }
      )
    } else {
      null
    }
  }
}