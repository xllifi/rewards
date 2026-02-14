package ru.xllifi.rewards.utils.ui

import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.Items
import ru.xllifi.rewards.modId
import ru.xllifi.rewards.serializers.ResourceLocation

fun texturedGuiElement(texture: String): GuiElementBuilder =
  GuiElementBuilder(Items.PAPER)
    .setComponent(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(modId, texture))