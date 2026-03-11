package ru.xllifi.rewards.calendar.ui

import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.gui.SimpleGui
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.calendar.Calendar
import ru.xllifi.rewards.calendar.sql.CollectedCell
import ru.xllifi.rewards.calendar.sql.CollectedCellTable
import ru.xllifi.rewards.commands.DebugCommands
import ru.xllifi.rewards.utils.plus
import ru.xllifi.rewards.utils.resizeEnd
import ru.xllifi.rewards.utils.restorePlayerInventory
import ru.xllifi.rewards.utils.setSlot
import ru.xllifi.rewards.utils.setSlotInPlayerInventory
import ru.xllifi.rewards.utils.ui.texturedGuiElement

class CalendarScreen : SimpleGui {
  val callback: (() -> Unit)?
  val calendar: Calendar
  val weeks: List<List<Calendar.Cell?>>
  val audiences: MinecraftServerAudiences

  // TODO: move to init{} blocks
  constructor(
    calendar: Calendar,
    player: ServerPlayer,
    callback: (() -> Unit)? = null,
  ) : super(
    /* type = */ when (calendar.weeksCount) {
      1 -> MenuType.GENERIC_9x1
      2 -> MenuType.GENERIC_9x2
      3 -> MenuType.GENERIC_9x3
      4 -> MenuType.GENERIC_9x4
      5 -> MenuType.GENERIC_9x5
      6 -> MenuType.GENERIC_9x6
      else -> throw IllegalStateException("Invalid weeksCount: ${calendar.weeksCount}!")
    },
    /* player = */ player,
    /* manipulatePlayerSlots = */ true,
  ) {
    this.callback = callback
    this.calendar = calendar
    val paddedCells = List(calendar.startDayPadding) { null } + calendar.cells
    this.weeks = paddedCells
      .resizeEnd(calendar.weeksCount * 7, null) { a, b -> a ?: b }
      .chunked(7)

    this.audiences = MinecraftServerAudiences.of(player.level().server)
    this.title = audiences.asNative(calendar.title)
    this.updateDisplay()
    this.open()
  }

  override fun onClose() {
    if (callback != null) {
      callback()
    }
  }

  fun updateDisplay() {
    restorePlayerInventory()
    if (callback != null) {
      setSlotInPlayerInventory(
        column = 0,
        row = 0,
        element = texturedGuiElement("paged_screen/close")
          .setItemName(Component.translatable("rewards.paged_screen.back").withStyle(ChatFormatting.RED))
          .setCallback(callback)
          .build()
      )
    }

    for (i in 0..<calendar.weeksCount) {
      // Weeks (most left column)
      this.setSlot(
        column = 0,
        row = i,
        element = weekGuiElement(i + 1),
      )
      // Blanks (most right column)
      this.setSlot(
        column = 8,
        row = i,
        element = blankGuiElement
      )
    }
    val collectedCells = transaction(Main.database) {
      addLogger(StdOutSqlLogger)
      CollectedCell.find {
        CollectedCellTable.playerUuid.eq(player.uuid) +
        CollectedCellTable.calendarId.eq(calendar.id)
      }.map { it.cellId }.toSet()
    }
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
    when {
      cell == null -> noCellGuiElement
      collectedCells.contains(cell.id) -> collectedCellGuiElement
      DebugCommands.calendarsIgnoreCellStatus -> activeGuiElement(cell)
      else -> when (calendar.getCellStatus(cell)) {
        Calendar.CellStatus.Upcoming -> upcomingCellElement(cell)
        Calendar.CellStatus.Available -> activeGuiElement(cell)
        Calendar.CellStatus.Ended -> missedCellElement
      }
    }
}
