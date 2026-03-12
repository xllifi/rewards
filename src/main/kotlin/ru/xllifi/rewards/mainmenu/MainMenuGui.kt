package ru.xllifi.rewards.mainmenu

import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import ru.xllifi.rewards.cosmetic.ui.LockerGui
//import ru.xllifi.rewards.cosmetic.ui.LockerScreen
import ru.xllifi.rewards.utils.setSlot
import ru.xllifi.rewards.utils.ui.texturedGuiElement

class MainMenuGui(player: ServerPlayer) : SimpleGui(MenuType.GENERIC_9x1, player, false) {
  init {
    this.title = Component.translatable("rewards.mainmenu.title")
    this.updateDisplay()
    this.open()
  }

  fun updateDisplay() {
    for (i in 0..<3) {
      this.setSlot(
        column = i,
        row = 0,
        element = texturedGuiElement("blank")
          .hideTooltip()
          .build(),
      )
      this.setSlot(
        column = i + 6,
        row = 0,
        element = texturedGuiElement("blank")
          .hideTooltip()
          .build(),
      )
    }
    this.setSlot(
      column = 3,
      row = 0,
      element = texturedGuiElement("mainmenu/calendar")
        .setItemName(Component.translatable("rewards.mainmenu.calendars"))
        .setCallback { _ ->
          val screen = DiscoverCalendarsGui(player, this::open)
          screen.refreshOpen()
        }
        .build(),
    )
    this.setSlot(
      column = 4,
      row = 0,
      element = texturedGuiElement("mainmenu/progression")
        .setItemName(Component.translatable("rewards.mainmenu.progressions"))
        .setCallback { _ ->
          val screen = DiscoverProgressionsGui(player, this::open)
          screen.refreshOpen()
        }
        .build(),
    )
    this.setSlot(
      column = 5,
      row = 0,
      element = texturedGuiElement("mainmenu/locker")
        .setItemName(Component.translatable("rewards.mainmenu.locker"))
        .setCallback { _ ->
          LockerGui(player, this::open).open()
        }
        .build(),
    )
  }
}