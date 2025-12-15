package ru.xllifi.rewards.utils

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity

val ItemStack.displayNameNoBrackets: Component
  get() {
    val comp = this.hoverName.copy()
    if (comp.style.color == null && this.rarity != Rarity.COMMON) {
      comp.withStyle(this.rarity.color())
    }
    if (!this.isEmpty) {
      comp.withStyle { style -> style.withHoverEvent(HoverEvent.ShowItem(this)) }
    }
    return comp
  }