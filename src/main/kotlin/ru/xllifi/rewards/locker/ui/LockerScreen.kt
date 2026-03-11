package ru.xllifi.rewards.locker.ui

import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.component.DyedItemColor
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.config.getServerAttachment
import ru.xllifi.rewards.locker.items.LockerItem
import ru.xllifi.rewards.locker.items.LockerItemKind
import ru.xllifi.rewards.locker.items.LockerRegistry
import ru.xllifi.rewards.locker.sql.CollectedLockerItem
import ru.xllifi.rewards.locker.sql.CollectedLockerItemTable
import ru.xllifi.rewards.utils.plus
import ru.xllifi.rewards.utils.restorePlayerInventory
import ru.xllifi.rewards.utils.setSlotInPlayerInventory
import ru.xllifi.rewards.utils.ui.DISABLED_COLOR
import ru.xllifi.rewards.utils.ui.PagedScreen
import ru.xllifi.rewards.utils.ui.texturedGuiElement
import kotlin.reflect.KClass

val EMPTY_ELEMENT: GuiElement = texturedGuiElement("blank")
  .hideTooltip()
  .build()

class LockerScreen : SimpleGui {
  val callback: (() -> Unit)?
  val kClasses = LockerItem::class.sealedSubclasses

  constructor(
    player: ServerPlayer,
    callback: (() -> Unit)? = null,
  ) : super(
    /* type = */ MenuType.GENERIC_9x1,
    /* player = */ player,
    /* manipulatePlayerSlots = */ true,
  ) {
    this.title = Component.translatable("rewards.locker.title")
    this.callback = callback
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
    for (index in 0..<this.height*9) {
      if (index < kClasses.size) {
        val kClass = kClasses[index]
        val kind = LockerRegistry.getKind(kClass)
        val count = transaction(Main.database) {
          CollectedLockerItem.find {
            CollectedLockerItemTable.playerUuid.eq(player.uuid) +
              CollectedLockerItemTable.kind.eq(kind)
          }.count()
        }
        this.setSlot(
          /* index = */ index,
          /* element = */ run {
            var lore = listOf(
              Component.literal("$count collected").withStyle(ChatFormatting.GRAY),
            )
            val builder = kind.getGuiElementBuilder()
              .setCallback { _ ->
                if (count > 0) {
                  val screen = LockerItemListScreen.create(kClass, player, this::open)
                  screen.refreshOpen()
                }
              }

            if (count <= 0) {
              builder
                .setComponent(
                  DataComponents.DYED_COLOR, DyedItemColor(DISABLED_COLOR)
                )
            } else {
              lore = lore + listOf(
                Component.empty(),
                Component.translatable("rewards.generic.click_to_open").withStyle(ChatFormatting.YELLOW),
              )
            }

            builder.setLore(lore)
            builder.build()
          }
        )
      } else {
        this.setSlot(index, EMPTY_ELEMENT)
      }
    }
  }
}

class LockerItemListScreen(
  val kind: LockerItemKind,
  player: ServerPlayer,
  callback: (() -> Unit)? = null,
) : PagedScreen(
  player,
  callback,
) {
  init {
    this.title = Component.translatable("rewards.locker.item_list.${kind.name}.title")
  }

  var items: List<CollectedLockerItem> = transaction(Main.database) {
    CollectedLockerItem.find {
      CollectedLockerItemTable.playerUuid.eq(player.uuid) +
        CollectedLockerItemTable.kind.eq(kind)
    }.toList()
  }
  override val pageAmount: Int
    get() = 1

  fun updateItems() {
    this.items = transaction(Main.database) {
      CollectedLockerItem.find {
        CollectedLockerItemTable.playerUuid.eq(player.uuid) +
          CollectedLockerItemTable.kind.eq(kind)
      }.toList()
    }
  }

  override fun getElement(id: Int): DisplayElement? {
    return if (id < items.size) {
      val collectedLockerItem = items[id]
      DisplayElement.of(
        collectedLockerItem.item.getGuiElementBuilder(player.level().server.getServerAttachment().audiences)
          .setLore(
            listOf(
              Component.translatable("rewards.locker.item_list.equipped.${collectedLockerItem.equipped}")
                .withStyle(ChatFormatting.GRAY),
              Component.translatable("rewards.locker.item_list.equipped.${collectedLockerItem.equipped}.action")
                .withStyle(ChatFormatting.YELLOW),
            )
          )
          .setCallback { _ ->
            collectedLockerItem.item.setEquippedFor(player, !collectedLockerItem.equipped)
            this.updateItems()
            this.updateDisplay()
          }
      )
    } else {
      null
    }
  }

  companion object {
    fun create(
      kClass: KClass<out LockerItem>,
      player: ServerPlayer,
      callback: (() -> Unit)? = null,
    ): LockerItemListScreen {
      val kind = LockerRegistry.getKind(kClass)
      return LockerItemListScreen(kind, player, callback)
    }
  }
}