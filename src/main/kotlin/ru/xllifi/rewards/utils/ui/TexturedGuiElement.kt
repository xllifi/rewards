package ru.xllifi.rewards.utils.ui

import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.DyedItemColor
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.utils.idOf

const val DEFAULT_COLOR: Int = 9145227
const val DISABLED_COLOR: Int = 11908533

fun texturedGuiElement(texture: String, color: Int = DEFAULT_COLOR): GuiElementBuilder =
  GuiElementBuilder(idOf(Main.MOD_ID, texture))
    .setComponent(DataComponents.DYED_COLOR, DyedItemColor(color))
    .hideDefaultTooltipComponents()