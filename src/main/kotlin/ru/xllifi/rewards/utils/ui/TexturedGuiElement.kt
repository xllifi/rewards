package ru.xllifi.rewards.utils.ui

import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.DyedItemColor
import net.minecraft.world.item.component.TooltipDisplay
import ru.xllifi.rewards.modId
import ru.xllifi.rewards.utils.resLoc

const val DEFAULT_COLOR: Int = 9145227
const val DISABLED_COLOR: Int = 11908533

fun texturedGuiElement(texture: String, color: Int = DEFAULT_COLOR): GuiElementBuilder =
  GuiElementBuilder(resLoc(modId, texture))
    .setComponent(DataComponents.DYED_COLOR, DyedItemColor(color))
    .setComponent(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay(false, linkedSetOf(DataComponents.DYED_COLOR)))