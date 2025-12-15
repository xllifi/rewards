package ru.xllifi.rewards.ui.calendar

import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.TooltipDisplay
import ru.xllifi.rewards.config.Calendar
import ru.xllifi.rewards.config.collectCell
import ru.xllifi.rewards.config.grant
import ru.xllifi.rewards.modId
import kotlin.time.ExperimentalTime

val CalendarScreen.noCellGuiElement: GuiElement
  get() = GuiElementBuilder(Items.PAPER)
    .setComponent(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(modId, "nocell"))
    .hideTooltip()
    .build()

val CalendarScreen.upcomingCellElement: GuiElement
  get() = GuiElementBuilder(Items.PAPER)
    .setComponent(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(modId, "upcoming"))
    .setItemName(Component.literal("Upcoming cell")) // TODO: translate
    .build()

val CalendarScreen.expiredCellElement: GuiElement
  get() = GuiElementBuilder(Items.PAPER)
    .setComponent(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(modId, "expired"))
    .setItemName(Component.literal("Expired cell")) // TODO: translate
    .build()

val CalendarScreen.collectedCellGuiElement: GuiElement
  get() = GuiElementBuilder(Items.PAPER)
    .setComponent(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(modId, "collected"))
    .setItemName(Component.literal("Collected cell")) // TODO: translate
    .build()

fun CalendarScreen.activeGuiElement(cell: Calendar.Cell): GuiElement {
  val builder = GuiElementBuilder(cell.displayItem)
    .setName(audiences.asNative(cell.title))
    .setLore(cell.description.map { audiences.asNative(it) })
    .setCallback { itemIndex, simpleClickType, minecraftClickType ->
      if (simpleClickType == ClickType.MOUSE_LEFT && !calendar.isCellCollected(player, cell)) {
        cell.rewards.grant(player)
        player.collectCell(calendar, cell)
      }
      updateDisplay()
    }

  return builder.build()
}