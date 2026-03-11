package ru.xllifi.rewards.progression.ui

import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.gui.SimpleGui
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.progression.Progression
import ru.xllifi.rewards.utils.resizeEnd
import ru.xllifi.rewards.progression.sql.getCollectedTierIndexes
import ru.xllifi.rewards.utils.restorePlayerInventory
import ru.xllifi.rewards.utils.setSlot
import ru.xllifi.rewards.utils.setSlotInPlayerInventory
import ru.xllifi.rewards.utils.ui.texturedGuiElement

class ProgressionScreen : SimpleGui {
  val callback: (() -> Unit)?
  val progression: Progression
  val lines: List<List<Progression.Tier?>>
  val audiences: MinecraftServerAudiences

  // TODO: move to init{} blocks
  constructor(
    progression: Progression,
    player: ServerPlayer,
    callback: (() -> Unit)? = null,
  ) : super(
    /* type = */
    when (progression.lines) {
      1 -> MenuType.GENERIC_9x1
      2 -> MenuType.GENERIC_9x2
      3 -> MenuType.GENERIC_9x3
      4 -> MenuType.GENERIC_9x4
      5 -> MenuType.GENERIC_9x5
      6 -> MenuType.GENERIC_9x6
      else -> throw IllegalStateException("Invalid lines count: ${progression.lines}!")
    },
    /* player = */ player,
    /* manipulatePlayerSlots = */ true,
  ) {
    this.callback = callback
    this.progression = progression
    this.lines = progression.tiers
      .resizeEnd(progression.lines * 7, null) { a, b -> a ?: b }
      .chunked(7)

    this.audiences = MinecraftServerAudiences.of(player.level().server)
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
