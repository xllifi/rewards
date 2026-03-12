package ru.xllifi.rewards.utils

import net.minecraft.world.inventory.MenuType

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

