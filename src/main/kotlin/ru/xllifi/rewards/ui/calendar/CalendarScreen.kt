package ru.xllifi.rewards.ui.calendar

import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences
import net.minecraft.core.component.DataComponents
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.xllifi.rewards.commands.Debug
import ru.xllifi.rewards.config.Calendar
import ru.xllifi.rewards.logger
import ru.xllifi.rewards.modId
import ru.xllifi.rewards.sql.CollectedCell
import ru.xllifi.rewards.sql.CollectedCellTable
import ru.xllifi.rewards.utils.resizeEnd

class CalendarScreen : SimpleGui {
  val calendar: Calendar
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
    if (calendar.cells.size > 34) {
      logger.warn("Calendar ${calendar.id} has too many cells (${calendar.cells.size}/35)! Every cell after 35th will not be accessible.")
    }
    val paddedCells = List(calendar.firstDayOrdinal) { null } + calendar.cells
    this.weeks = paddedCells
      .resizeEnd(35, null) { a, b -> a ?: b }
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
    val collectedCells = transaction {
      addLogger(StdOutSqlLogger)
      CollectedCell.find {
        CollectedCellTable.playerUuid eq player.uuid
        CollectedCellTable.calendarId eq calendar.id
      }.map { it.cellId }.toSet()
    }
    logger.info("Updaing screen for ${player.uuid}")
    weeks.forEachIndexed { weekIndex, week ->
      week.forEachIndexed { cellIndex, cell ->
        this.setSlot(
          column = cellIndex + 1,
          row = weekIndex,
          element = getGuiElement(cell, collectedCells),
        )
      }
    }
  }

  fun getGuiElement(cell: Calendar.Cell?, collectedCells: Set<String>): GuiElement =
    if (cell == null) {
      noCellGuiElement
    } else {
      if (collectedCells.contains(cell.id)) {
        collectedCellGuiElement
      } else {
        if (Debug.calendarsIgnoreCellStatus) activeGuiElement(cell)
        else when (calendar.getCellStatus(cell)) {
          Calendar.CellStatus.Upcoming -> upcomingCellElement(cell)
          Calendar.CellStatus.Available -> activeGuiElement(cell)
          Calendar.CellStatus.Ended -> missedCellElement
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