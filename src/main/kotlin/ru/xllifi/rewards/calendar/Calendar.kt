package ru.xllifi.rewards.calendar

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Style
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component as McComponent
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.calendar.sql.isCellCollectedBy
import ru.xllifi.rewards.config.GlobalConfig
import ru.xllifi.rewards.config.getServerAttachment
import ru.xllifi.rewards.rewards.Reward
import ru.xllifi.rewards.serializers.ResourceLocation
import ru.xllifi.rewards.serializers.text.Component
import ru.xllifi.rewards.serializers.time.InstantAsDay
import ru.xllifi.rewards.utils.plus
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class Calendar(
  val id: String,
  val title: Component,
  val startDay: InstantAsDay,
  val cells: List<Cell>,

  @Transient
  val endDay: Instant = startDay.plus(cells.size * 24, DateTimeUnit.HOUR),
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
    val cellStart = getCellStartLocalDate(cell).atTime(0, 0)
    val cellEnd = cellStart.date.plus(1, DateTimeUnit.DAY).atTime(0, 0)
    val now = Clock.System.now().toLocalDateTime(Main.globalConfig.timeZoneForSure)
    return when {
      now > cellEnd -> CellStatus.Ended
      now > cellStart -> CellStatus.Available
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

  companion object {
    fun notifyPlayerOfAvailableUncollectedCells(
      globalConfig: GlobalConfig,
      player: ServerPlayer,
    ) {
      if (globalConfig.calendarReminders) {
        val serverAttachment = player.level().server.getServerAttachment()
        for (calendar in serverAttachment.calendars.filter { it.isActive }) {
          val todayDate = Clock.System.now().toLocalDateTime(globalConfig.timeZoneForSure).date
          val calendarStartDate = calendar.startDay.toLocalDateTime(globalConfig.timeZoneForSure).date

          val currentCalendarDay = (todayDate - calendarStartDate).days
          val todayCell = calendar.cells[currentCalendarDay]

          if (!calendar.isCellCollectedBy(player, todayCell)) {
            player.sendSystemMessage(
              McComponent.translatable(
                "rewards.calendar.notification",
                serverAttachment.audiences.asNative(calendar.title)
              ) +
                McComponent.literal("\n") +
                McComponent.translatable("rewards.calendar.notification.action").withStyle(
                  Style.EMPTY
                    .withUnderlined(true)
                    .withColor(ChatFormatting.YELLOW)
                    .withClickEvent(ClickEvent.RunCommand("/calendar open ${calendar.id}"))
                )
            )
          }
        }
      }
    }
  }
}