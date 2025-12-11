package ru.xllifi.rewards.ui.calendar

import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.TooltipDisplay
import kotlin.time.ExperimentalTime

//val CalendarScreen.noCellGuiElement: GuiElement
//  get() = GuiElementBuilder(Items.PAPER)
//    .setComponent(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(modId, "nocell"))
//    .hideTooltip()
//    .build()
//
//@OptIn(ExperimentalTime::class)
//fun CalendarScreen.lockedUpcomingCellGuiElement(cell: Calendar.Cell): GuiElement {
//  val daysToUnlocked = daysFromNowTo(cell.getUnlockedAtMillis(calendar))
//  return GuiElementBuilder(Items.PAPER)
//    .setComponent(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(modId, "upcoming"))
//    .setItemName(
//      Component.literal(texts.cellTexts.lockedUpcoming.title)
//    )
//    .setLore(
//      format(
//        texts.cellTexts.lockedUpcoming.description,
//        daysToUnlocked,
//        texts.units.day.stringFor(daysToUnlocked),
//      )
//        .split('\n')
//        .map { Component.literal(it) }
//    )
//    .build()
//}
//
//val CalendarScreen.lockedExpiredCellGuiElement: GuiElement
//  get() = GuiElementBuilder(Items.PAPER)
//    .setComponent(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(modId, "expired"))
//    .setItemName(Component.literal(texts.cellTexts.lockedExpired.title))
//    .setLore(
//      texts.cellTexts.lockedExpired.description
//        .split('\n')
//        .map { Component.literal(it) }
//    )
//    .build()
//
//val CalendarScreen.collectedCellGuiElement: GuiElement
//  get() = GuiElementBuilder(Items.PAPER)
//    .setComponent(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(modId, "collected"))
//    .setItemName(Component.literal(texts.cellTexts.collected.title))
//    .setLore(
//      texts.cellTexts.collected.description
//        .split('\n')
//        .map { Component.literal(it) }
//    )
//    .build()
//
//fun CalendarScreen.activeGuiElement(cell: Calendar.Cell): GuiElement {
//  val builder = GuiElementBuilder(
//    when (cell) {
//      is Calendar.Cell.Item -> cell.getItemStack(registryAccess)
//      is Calendar.Cell.Xp -> ItemStack(Items.EXPERIENCE_BOTTLE)
//    }
//  )
//    .setName(cell.getName())
//    .setLore(cell.getLore(texts))
//    .setComponent(
//      DataComponents.TOOLTIP_DISPLAY,
//      TooltipDisplay(
//        false,
//        LinkedHashSet(
//          listOf(
//            DataComponents.POTION_CONTENTS,
//            DataComponents.ENCHANTMENTS,
//            DataComponents.STORED_ENCHANTMENTS,
//            DataComponents.ATTRIBUTE_MODIFIERS,
//          )
//        )
//      )
//    )
//    .apply(cell.guiElementBuilderAdditions)
//    .setCallback { itemIndex, simpleClickType, minecraftClickType ->
//      if (simpleClickType == ClickType.MOUSE_LEFT && !player.hasCollectedCell(calendar, cell)) {
//        cell.grant(player)
//        player.playSound(
//          when (cell) {
//            is Calendar.Cell.Item -> SoundEvents.ITEM_PICKUP
//            is Calendar.Cell.Xp -> SoundEvents.EXPERIENCE_ORB_PICKUP
//          }
//        )
//        player.collectCell(calendar.id, cell.id)
//      }
//      updateDisplay()
//    }
//
//  return builder.build()
//}