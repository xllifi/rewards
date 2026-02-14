package ru.xllifi.rewards.calendar.ui

import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.elements.GuiElementBuilder
import kotlinx.datetime.atStartOfDayIn
import net.minecraft.ChatFormatting
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.sounds.SoundSource
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.calendar.Calendar
import ru.xllifi.rewards.calendar.sql.setCellCollectedFor
import ru.xllifi.rewards.logger
import ru.xllifi.rewards.rewards.grant
import ru.xllifi.rewards.serializers.time.dayHumanReadable
import ru.xllifi.rewards.utils.texturedGuiElement

val CalendarScreen.blankGuiElement: GuiElement
  get() = texturedGuiElement("calendar/blank")
    .hideTooltip()
    .build()

fun CalendarScreen.weekGuiElement(w: Int): GuiElement =
  texturedGuiElement("calendar/w${w}")
    .hideTooltip()
    .build()

val CalendarScreen.noCellGuiElement: GuiElement
  get() = texturedGuiElement("calendar/nocell")
    .hideTooltip()
    .build()

fun CalendarScreen.upcomingCellElement(cell: Calendar.Cell): GuiElement =
  texturedGuiElement("calendar/upcoming")
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
  get() = texturedGuiElement("calendar/missed")
    .setItemName(Component.translatable("rewards.calendar.cell.missed").withStyle(ChatFormatting.GRAY))
    .setLore(
      listOf(
        Component.translatable("rewards.calendar.cell.missed.lore").withStyle(ChatFormatting.GRAY)
      )
    )
    .build()

val CalendarScreen.collectedCellGuiElement: GuiElement
  get() = texturedGuiElement("calendar/collected")
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
    .setCallback { _, _, _ ->
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