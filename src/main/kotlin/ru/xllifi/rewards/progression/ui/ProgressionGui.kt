package ru.xllifi.rewards.progression.ui

import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.gui.SimpleGui
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.progression.Progression
import ru.xllifi.rewards.utils.extensions.resizeEnd
import ru.xllifi.rewards.progression.sql.getCollectedTierIndexes
import ru.xllifi.rewards.utils.ui.GuiHelpersRewards
import ru.xllifi.rewards.utils.extensions.restorePlayerInventory
import ru.xllifi.rewards.utils.extensions.setSlot
import ru.xllifi.rewards.utils.extensions.setSlotInPlayerInventory
import ru.xllifi.rewards.utils.ui.texturedGuiElement

class ProgressionGui(
  val progression: Progression,
  player: ServerPlayer,
  val callback: (() -> Unit)? = null,
) : SimpleGui(
  /* type = */ GuiHelpersRewards.menuTypeForRowCount(progression.lines),
  /* player = */ player,
  /* manipulatePlayerSlots = */ true,
) {
  val lines: List<List<Progression.Tier?>> = progression.tiers
    .resizeEnd(progression.lines * 7, null) { a, b -> a ?: b }
    .chunked(7)
  val audiences: MinecraftServerAudiences = MinecraftServerAudiences.of(player.level().server)

  init {
    this.title = audiences.asNative(progression.title)
    this.updateDisplay()
    this.open()
  }

  override fun onClose() {
    if (callback != null) {
      callback()
    }
  }

  fun updateDisplay() {
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

    for (i in 0..<progression.lines) {
      // Blanks (most left column)
      this.setSlot(
        column = 0,
        row = i,
        element = blankGuiElement
      )
      // Blanks (most right column)
      this.setSlot(
        column = 8,
        row = i,
        element = blankGuiElement
      )
    }
    val collectedTierIndexes = progression.getCollectedTierIndexes(player)
    Main.logger.info("Updating screen for ${player.uuid}")
    lines.forEachIndexed { row, line ->
      line.forEachIndexed { col, tier ->
        this.setSlot(
          column = col + 1,
          row = row,
          element = getGuiElement(tier, row * 7 + col, collectedTierIndexes),
        )
      }
    }
  }

  fun getGuiElement(tier: Progression.Tier?, tierIdx: Int, collectedTierIndexes: List<Int>): GuiElement = run {
    when {
      tier == null -> noTierGuiElement
      collectedTierIndexes.contains(tierIdx) -> collectedTierGuiElement(tier)
      else -> when (tier.unlockCondition.status(player)) {
        null -> pendingTierGuiElement(tier)
        true -> completedTierGuiElement(tier)
        false -> failedTierGuiElement(tier)
      }
    }
  }
}
