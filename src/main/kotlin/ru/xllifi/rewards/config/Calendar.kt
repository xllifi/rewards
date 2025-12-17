package ru.xllifi.rewards.config

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.minecraft.world.item.ItemStack
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.serializers.ResourceLocation
import ru.xllifi.rewards.serializers.text.Component
import ru.xllifi.rewards.serializers.time.InstantAsDay
import kotlin.time.Clock

@Serializable
data class Calendar(
  val id: String,
  val title: Component,
  val startDay: InstantAsDay,
  val endDay: InstantAsDay,
  val cells: List<Cell>
) {
  val isActive: Boolean get() {
    val now = Clock.System.now()
    return now in startDay..endDay
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
    val collectionSound: ResourceLocation? = null,
    @Contextual val displayItem: ItemStack,
    val rewards: List<Reward>,
  )
}