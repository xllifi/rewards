package ru.xllifi.rewards.utils.ui

import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.DyedItemColor
import net.minecraft.world.item.component.TooltipDisplay
import ru.xllifi.rewards.modId
import ru.xllifi.rewards.utils.resLoc

fun texturedGuiElement(texture: String): GuiElementBuilder =
  GuiElementBuilder(resLoc(modId, texture))
    .setComponent(DataComponents.DYED_COLOR, DyedItemColor(9145227))
    .setComponent(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay(false, linkedSetOf(DataComponents.DYED_COLOR)))