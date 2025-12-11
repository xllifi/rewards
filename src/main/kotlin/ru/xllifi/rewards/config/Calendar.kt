package ru.xllifi.rewards.config

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import org.jetbrains.exposed.v1.dao.exceptions.EntityNotFoundException
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.conditions.Condition
import ru.xllifi.rewards.serializers.text.Component
import ru.xllifi.rewards.serializers.time.InstantAsDay
import ru.xllifi.rewards.sql.PlayerData

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
    if (!cell.unlockCondition.isMet(player)) {
      throw IllegalStateException("Cell unlock condition is not met!")
    }
    player.collectCell(this, cell)
    cell.rewards.grant(player)
  }

  @Serializable
  class Cell(
    val id: String,
    val title: Component,
    val description: List<Component>,
    @Contextual val displayItem: ItemStack,
    val unlockCondition: Condition,
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