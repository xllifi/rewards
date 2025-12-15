package ru.xllifi.rewards.ui.calendar

import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences
import net.minecraft.core.RegistryAccess
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items
import ru.xllifi.rewards.config.Calendar
import ru.xllifi.rewards.logger
import ru.xllifi.rewards.modId

class CalendarScreen : SimpleGui {
  val calendar: Calendar
  val firstOffset: Int
  val weeks: List<List<Calendar.Cell?>>
  val audiences: MinecraftServerAudiences

  constructor(
    calendar: Calendar,
    player: ServerPlayer,
  ) : super(
    /* type = */ MenuType.GENERIC_9x5,
    /* player = */ player,
    /* manipulatePlayerSlots = */ false,
  ) {
    this.calendar = calendar
    this.firstOffset = 1//getDayOfWeekUTC((calendar.startAtSecs + calendar.cells.first().unlockedDeltaSecs) * 1000).value
    if (calendar.cells.size > 34) {
      logger.warn("Calendar ${calendar.id} has too many cells (${calendar.cells.size}/35)! Every cell after 35th will not be accessible.")
    }
    this.weeks = (List(firstOffset - 1) { null } + calendar.cells)
      // TODO: find the source for this
//      .resizeEnd(35, null) { a, b -> a ?: b }
      .chunked(7)

    this.audiences = MinecraftServerAudiences.of(player.level().server)
    this.title = audiences.asNative(calendar.title)
    this.updateDisplay()
    this.open()
  }

  fun updateDisplay() {
    for (i in 0..4) {
      // Weeks (most left column)
      this.setSlot(
        column = 0,
        row = i,
        element = GuiElementBuilder(Items.PAPER)
          .setComponent(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(modId, "w${i + 1}"))
          .hideTooltip()
          .build()
      )
      // Blanks (most right column)
      this.setSlot(
        column = 8,
        row = i,
        element = GuiElementBuilder(Items.PAPER)
          .setComponent(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(modId, "blank"))
          .hideTooltip()
          .build()
      )
    }
    weeks.forEachIndexed { weekIndex, week ->
      week.forEachIndexed { cellIndex, cell ->
        this.setSlot(
          column = cellIndex + 1,
          row = weekIndex,
          element = cell.getGuiElement(),
        )
      }
    }
  }

  fun Calendar.Cell?.getGuiElement(): GuiElement {
    val cell = this

    return if (cell == null) {
      noCellGuiElement
    } else {
      if (calendar.isCellCollected(player, cell)) {
        collectedCellGuiElement
      } else {
        when (calendar.getCellStatus(cell)) {
          Calendar.CellStatus.Upcoming -> upcomingCellElement
          Calendar.CellStatus.Started -> activeGuiElement(cell)
          Calendar.CellStatus.Ended -> expiredCellElement
        }
      }
    }
  }
}

fun SimpleGui.setSlot(
  column: Int,
  row: Int,
  element: GuiElement,
) {
  setSlot(row * 9 + column, element)
}