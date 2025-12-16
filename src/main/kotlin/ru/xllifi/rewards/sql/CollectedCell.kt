package ru.xllifi.rewards.sql

import net.minecraft.server.level.ServerPlayer
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.xllifi.rewards.config.Calendar
import java.util.UUID

class CollectedCell(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<CollectedCell>(CollectedCellTable)

  var playerUuid by CollectedCellTable.playerUuid
  var calendarId by CollectedCellTable.calendarId
  var cellId by CollectedCellTable.cellId

  override fun toString(): String =
    "CollectedCell(playerUuid=$playerUuid, calendarId=$calendarId, cellId=$cellId)"
}

object CollectedCellTable : IntIdTable("t_collected_cells") {
  val playerUuid: Column<UUID> = text("player_uuid")
    .transform<String, UUID>(
      wrap = { UUID.fromString(it) },
      unwrap = { it.toString() },
    )
  val calendarId: Column<String> = text("calendar_id")
  val cellId: Column<String> = text("cell_id")
}

private fun Calendar.getCollectedCell(cell: Calendar.Cell, player: ServerPlayer): CollectedCell? {
  val calendar = this
  return transaction {
    addLogger(StdOutSqlLogger)
    CollectedCell.find {
      CollectedCellTable.playerUuid eq player.uuid
      CollectedCellTable.calendarId eq calendar.id
      CollectedCellTable.cellId eq cell.id
    }.firstOrNull()
  }
}

fun Calendar.isCellCollectedBy(player: ServerPlayer, cell: Calendar.Cell): Boolean = getCollectedCell(cell, player) != null

fun Calendar.setCellCollectedFor(player: ServerPlayer, cell: Calendar.Cell, to: Boolean) {
  val calendar = this
  transaction {
    addLogger(StdOutSqlLogger)
    if (to == true) {
      CollectedCell.new {
        playerUuid = player.uuid
        calendarId = calendar.id
        cellId = cell.id
      }
    } else {
      getCollectedCell(cell, player)?.delete()
    }
  }
}