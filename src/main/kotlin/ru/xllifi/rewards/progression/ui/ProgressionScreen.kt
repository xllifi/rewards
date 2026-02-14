package ru.xllifi.rewards.progression.ui

import eu.pb4.sgui.api.GuiHelpers
import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import ru.xllifi.rewards.logger
import ru.xllifi.rewards.progression.Progression
import ru.xllifi.rewards.utils.resizeEnd
import ru.xllifi.rewards.progression.sql.getCollectedTierIndexes
import ru.xllifi.rewards.utils.ui.texturedGuiElement

class ProgressionScreen : SimpleGui {
  val callback: (() -> Unit)?
  val progression: Progression
  val lines: List<List<Progression.Tier?>>
  val audiences: MinecraftServerAudiences

  constructor(
    progression: Progression,
    player: ServerPlayer,
    callback: (() -> Unit)? = null
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

  fun updateDisplay() {
    // Restore player inventory
    val height = GuiHelpers.getHeight(this.type)
    for (row in 0..3) {
      val isHotbar: Boolean = (row == 3)
      for (col in 0..8) {
        this.setSlot(
          row = row + height,
          column = col,
          itemStack = player.inventory.getItem(
            if (isHotbar) {
              col
            } else {
              (row + 1) * 9 + col
            }
          )
        )
      }
    }
    // Set ephemeral item
    if (callback != null) {
      this.setSlot(
        column = 0,
        row = height,
        element = texturedGuiElement("paged_screen/prev")
          .setItemName(Component.translatable("rewards.paged_screen.back"))
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
    logger.info("Updating screen for ${player.uuid}")
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
    logger.info("Collected indexes: {}. Current idx: {}", collectedTierIndexes, tierIdx)
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

fun SimpleGui.setSlot(
  column: Int,
  row: Int,
  element: GuiElement,
) {
  setSlot(row * 9 + column, element)
}

fun SimpleGui.setSlot(
  column: Int,
  row: Int,
  itemStack: ItemStack
) {
  setSlot(row * 9 + column, itemStack)
}