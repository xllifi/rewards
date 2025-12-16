package ru.xllifi.rewards.ui.calendar

import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.elements.GuiElementBuilder
import kotlinx.datetime.atStartOfDayIn
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.Items
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.config.Calendar
import ru.xllifi.rewards.config.grant
import ru.xllifi.rewards.logger
import ru.xllifi.rewards.modId
import ru.xllifi.rewards.serializers.time.dayHumanReadable
import ru.xllifi.rewards.sql.setCellCollectedFor
import kotlin.reflect.jvm.jvmName

val CalendarScreen.noCellGuiElement: GuiElement
  get() = GuiElementBuilder(Items.PAPER)
    .setComponent(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(modId, "nocell"))
    .hideTooltip()
    .build()

fun CalendarScreen.upcomingCellElement(cell: Calendar.Cell): GuiElement =
  GuiElementBuilder(Items.PAPER)
    .setComponent(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(modId, "upcoming"))
    .setItemName(Component.translatable("rewards.calendar.cell.upcoming").withStyle(ChatFormatting.GRAY))
    .setLore(
      listOf(
        Component.translatable(
          "rewards.calendar.cell.upcoming.lore",
          calendar.getCellStartLocalDate(cell)
            .atStartOfDayIn(Main.globalConfig.timeZoneForSure)
            .dayHumanReadable()
        ).withStyle(ChatFormatting.GRAY)
      )
    )
    .build()

val CalendarScreen.missedCellElement: GuiElement
  get() = GuiElementBuilder(Items.PAPER)
    .setComponent(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(modId, "missed"))
    .setItemName(Component.translatable("rewards.calendar.cell.missed").withStyle(ChatFormatting.GRAY))
    .setLore(
      listOf(
        Component.translatable("rewards.calendar.cell.missed.lore").withStyle(ChatFormatting.GRAY)
      )
    )
    .build()

val CalendarScreen.collectedCellGuiElement: GuiElement
  get() = GuiElementBuilder(Items.PAPER)
    .setComponent(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(modId, "collected"))
    .setItemName(Component.translatable("rewards.calendar.cell.collected").withStyle(ChatFormatting.GRAY))
    .setLore(
      listOf(
        Component.translatable("rewards.calendar.cell.collected.lore").withStyle(ChatFormatting.GRAY)
      )
    )
    .build()

fun CalendarScreen.activeGuiElement(cell: Calendar.Cell): GuiElement {
  val builder = GuiElementBuilder(cell.displayItem)
    .setName(audiences.asNative(cell.title))
    .setLore(
      listOf(
        *cell.description.map { audiences.asNative(it) }.toTypedArray(),
        Component.empty(),
        Component.translatable("rewards.generic.rewards").withStyle(ChatFormatting.GRAY),
        *cell.rewards.map { it.lore() }.toTypedArray(),
        Component.empty(),
        Component.translatable("rewards.generic.click_to_collect").withStyle(ChatFormatting.YELLOW),
      )
    )
    .setCallback { itemIndex, simpleClickType, minecraftClickType ->
      calendar.setCellCollectedFor(player, cell, true)
      if (cell.collectionSound != null) {
        try {
          val sound = player.registryAccess()
            .get(
              ResourceKey.create(
                Registries.SOUND_EVENT,
                cell.collectionSound
              )
            ).get().value()
          player.playNotifySound(sound, SoundSource.UI, 1f, 1f)
        } catch (e: Exception) {
          logger.error("Failed to play sound ${cell.collectionSound} to player ${player.plainTextName}: ${e.stackTraceToString()}")
        }
      }
      cell.rewards.grant(player)
      updateDisplay()
    }

  return builder.build()
}