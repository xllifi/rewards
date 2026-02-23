package ru.xllifi.rewards.utils

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