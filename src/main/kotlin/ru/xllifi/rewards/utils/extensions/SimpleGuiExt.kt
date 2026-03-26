package ru.xllifi.rewards.utils.extensions

import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.world.item.ItemStack

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
  itemStack: ItemStack,
) {
  setSlot(row * 9 + column, itemStack)
}

fun SimpleGui.restorePlayerInventory() {
  // Restore player inventory
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
}

fun SimpleGui.setSlotInPlayerInventory(
  column: Int,
  row: Int,
  element: GuiElement,
) {
  this.setSlot(
    column = column,
    row = height + row,
    element = element,
  )
}