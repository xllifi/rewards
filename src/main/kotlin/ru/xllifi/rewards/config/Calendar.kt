package ru.xllifi.rewards.config

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import org.jetbrains.exposed.v1.dao.exceptions.EntityNotFoundException
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.logger
import ru.xllifi.rewards.serializers.text.Component
import ru.xllifi.rewards.serializers.text.ComponentSerializer
import ru.xllifi.rewards.serializers.time.InstantAsDay
import ru.xllifi.rewards.serializers.time.InstantAsDaySerializer
import ru.xllifi.rewards.sql.PlayerData
import kotlin.time.Clock

class CalendarSerializer : KSerializer<Calendar> {
  @OptIn(InternalSerializationApi::class)
  override val descriptor: SerialDescriptor
    get() = buildSerialDescriptor("calendar", SerialKind.CONTEXTUAL)

  override fun serialize(encoder: Encoder, value: Calendar) {
    encoder.encodeString(value.id)
    ComponentSerializer.serialize(encoder, value.title)
    InstantAsDaySerializer.serialize(encoder, value.startDay)
    InstantAsDaySerializer.serialize(encoder, value.endDay)

  }

  override fun deserialize(decoder: Decoder): Calendar {
    TODO("Not yet implemented")
  }

}

@Serializable
data class Calendar(
  val id: String,
  val title: Component,
  val startDay: InstantAsDay,
  val endDay: InstantAsDay,
  val cells: List<Cell>
) {
  fun isCellCollected(player: ServerPlayer, cell: Cell): Boolean {
    val playerData = transaction(Main.database) {
      try {
        PlayerData[player.uuid]
      } catch (_: EntityNotFoundException) {
        null
      }
    }
    return playerData?.collectedCalendarCells[this.id]?.contains(cell.id) ?: false
  }

  fun collectCell(player: ServerPlayer, cell: Cell) {
    if (isCellCollected(player, cell)) {
      throw IllegalStateException("Cell already collected!")
    }
    if (getCellStatus(cell) != CellStatus.Available) {
      throw IllegalStateException("Cell is not available!")
    }
    player.collectCell(this, cell)
    cell.rewards.grant(player)
  }

  fun getCellStartLocalDate(cell: Cell): LocalDate {
    val index = cells.indexOf(cell)
    if (index == -1) throw IllegalStateException("Cell ${cell.id} not in calendar ${this.id}!")
    val startLocalDateTime = startDay.toLocalDateTime(Main.globalConfig.timeZoneForSure).date
    return startLocalDateTime.plus(index, DateTimeUnit.DAY)
  }

  val firstDayOrdinal: Int
    get() = getCellStartLocalDate(cells.first()).dayOfWeek.ordinal

  fun getCellStatus(cell: Cell): CellStatus {
    val cellStartLocalDate = getCellStartLocalDate(cell)
    val now = Clock.System.now().toLocalDateTime(Main.globalConfig.timeZoneForSure).date
    return when {
      now > cellStartLocalDate -> CellStatus.Ended
      now >= cellStartLocalDate -> CellStatus.Available
      else -> CellStatus.Upcoming
    }
  }

  enum class CellStatus {
    Upcoming,
    Available,
    Ended,
  }

  @Serializable
  class Cell(
    val id: String,
    val title: Component,
    val description: List<Component>,
    @Contextual val displayItem: ItemStack,
    val rewards: List<Reward>,
  )
}

fun ServerPlayer.collectCell(calendar: Calendar, cell: Calendar.Cell): PlayerData? {
  val player = this
  return transaction(Main.database) {
    return@transaction PlayerData.findByIdAndUpdate(player.uuid) { playerData ->
      val oldCells = playerData.collectedCalendarCells[calendar.id] ?: emptySet()
      if (oldCells.contains(cell.id)) return@findByIdAndUpdate

      val newCell = setOf(cell.id)
      playerData.collectedCalendarCells = playerData.collectedCalendarCells.plus(
        calendar.id to newCell + oldCells
      )
    }
  }
}

fun ServerPlayer.uncollectCell(calendar: Calendar, cell: Calendar.Cell): PlayerData? {
  val player = this
  return transaction(Main.database) {
    return@transaction PlayerData.findByIdAndUpdate(player.uuid) { playerData ->
      val oldCells = playerData.collectedCalendarCells[calendar.id] ?: emptySet()
      if (oldCells.isEmpty() || !oldCells.contains(cell.id)) return@findByIdAndUpdate

      playerData.collectedCalendarCells = playerData.collectedCalendarCells.plus(
        calendar.id to oldCells.minus(cell.id)
      )
    }
  }
}