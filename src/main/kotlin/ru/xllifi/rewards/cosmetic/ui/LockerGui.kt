package ru.xllifi.rewards.cosmetic.ui

import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.component.DyedItemColor
import ru.xllifi.rewards.config.getServerAttachment
import ru.xllifi.rewards.cosmetic.CosmeticKind
import ru.xllifi.rewards.cosmetic.getCollectedBy
import ru.xllifi.rewards.utils.GuiHelpersRewards
import ru.xllifi.rewards.utils.extendAndAlign
import ru.xllifi.rewards.utils.restorePlayerInventory
import ru.xllifi.rewards.utils.setSlot
import ru.xllifi.rewards.utils.setSlotInPlayerInventory
import ru.xllifi.rewards.utils.ui.DISABLED_COLOR
import ru.xllifi.rewards.utils.ui.texturedGuiElement

class LockerGui(
  player: ServerPlayer,
  val callback: (() -> Unit)? = null,
  /** **Don't change!** */
  rows: List<List<CosmeticKind>> = CosmeticKind.entries.chunked(9)
) : SimpleGui(
  /* type = */ GuiHelpersRewards.menuTypeForRowCount(rows.size),
  /* player = */ player,
  /* manipulatePlayerSlots = */ true,
) {
  init {
    this.title = Component.translatable("gui.rewards.locker.title")

    restorePlayerInventory()
    if (callback != null) {
      setSlotInPlayerInventory(
        column = 0,
        row = 0,
        element = texturedGuiElement("paged_screen/close")
          .setItemName(Component.translatable("rewards.paged_screen.back").withStyle(ChatFormatting.RED))
          .setCallback(callback)
          .build()
      )
    }

    val attachment = player.level().server.getServerAttachment()

    rows.forEachIndexed { rowIndex, row ->
      val row = if (rowIndex == rows.lastIndex) {
        row.extendAndAlign(9, null)
      } else row

      row.forEachIndexed { colIndex, cosmeticKind ->
        this.setSlot(
          row = rowIndex,
          column = colIndex,
          element = if (cosmeticKind != null) {
            val registeredCosmetics = attachment.cosmetics[cosmeticKind]?.values?.toList() ?: emptyList()
            val countableIds = registeredCosmetics.filter { it.shouldCountInTotal }.map { it.id }
            val collectedIds = registeredCosmetics.getCollectedBy(player).map { it.cosmeticId.value }

            var builder = cosmeticKind.getGuiElementBuilder()
              .setItemName(Component.translatable("gui.rewards.locker.kinds.${cosmeticKind.snakeCaseName()}"))
              .setLore(
                listOf(
                  Component.translatable(
                    "gui.rewards.locker.collected",
                    collectedIds.size, countableIds.size
                  ),
                )
              )
            if (collectedIds.isEmpty()) {
              builder = builder.setComponent(DataComponents.DYED_COLOR, DyedItemColor(DISABLED_COLOR))
            }
            builder
              .setCallback { _ ->
                if (collectedIds.isEmpty()) {
                  player.connection.send(
                    ClientboundSoundEntityPacket(
                      SoundEvents.SHIELD_BLOCK,
                      SoundSource.UI,
                      player,
                      1f,
                      1f,
                      player.level().random.nextLong()
                    )
                  )
                } else {
                  CollectedCosmeticsOfKindGui(
                    cosmeticKind = cosmeticKind,
                    player = player,
                    callback = this::open,
                  )
                }
              }
              .build()
          } else {
            texturedGuiElement("blank").hideTooltip().build()
          }
        )

      }
    }
  }

  override fun onClose() {
    if (callback != null) {
      callback()
    }
  }
}