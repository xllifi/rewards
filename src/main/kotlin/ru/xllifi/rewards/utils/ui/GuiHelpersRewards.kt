package ru.xllifi.rewards.utils.ui

import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.core.component.DataComponents
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.component.TooltipDisplay

object GuiHelpersRewards {
  fun menuTypeForRowCount(rowCount: Int) =
    when (rowCount) {
      1 -> MenuType.GENERIC_9x1
      2 -> MenuType.GENERIC_9x2
      3 -> MenuType.GENERIC_9x3
      4 -> MenuType.GENERIC_9x4
      5 -> MenuType.GENERIC_9x5
      6 -> MenuType.GENERIC_9x6
      else -> throw IllegalStateException("Invalid row count: $rowCount!")
    }
}

val canBeDefaultComponents = linkedSetOf(
  DataComponents.ATTRIBUTE_MODIFIERS,
  DataComponents.ENCHANTMENTS,
  DataComponents.STORED_ENCHANTMENTS,
  DataComponents.UNBREAKABLE,
  DataComponents.CAN_BREAK,
  DataComponents.CAN_PLACE_ON,
  DataComponents.TRIM,
  DataComponents.DYED_COLOR,
  DataComponents.POTION_CONTENTS,
  DataComponents.CONTAINER,
  DataComponents.BUNDLE_CONTENTS,
  DataComponents.JUKEBOX_PLAYABLE,
  DataComponents.INSTRUMENT,
  DataComponents.BANNER_PATTERNS,
  DataComponents.FIREWORK_EXPLOSION,
  DataComponents.FIREWORKS,
  DataComponents.WRITTEN_BOOK_CONTENT,
  DataComponents.BEES,
  DataComponents.OMINOUS_BOTTLE_AMPLIFIER,
  DataComponents.CHARGED_PROJECTILES,
  DataComponents.PROFILE,
  DataComponents.POT_DECORATIONS,
  DataComponents.BUCKET_ENTITY_DATA,
  DataComponents.SUSPICIOUS_STEW_EFFECTS
)

fun GuiElementBuilder.hideDefaultTooltipComponents(): GuiElementBuilder =
  setComponent(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay(false, canBeDefaultComponents))
