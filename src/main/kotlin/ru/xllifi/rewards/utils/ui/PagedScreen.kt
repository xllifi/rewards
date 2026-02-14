package ru.xllifi.rewards.utils.ui

import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface
import eu.pb4.sgui.api.elements.GuiElementInterface
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import kotlin.math.max
import kotlin.math.min

// Based on https://github.com/Patbox/get-off-my-lawn-reserved/blob/main/src/main/java/draylar/goml/ui/PagedGui.java

abstract class PagedScreen(player: ServerPlayer, protected val closeCallback: Runnable?) :
  SimpleGui(MenuType.GENERIC_9x5, player, false) {
  protected var page: Int = 0
  var ignoreCloseCallback: Boolean = false

  fun refreshOpen() {
    this.updateDisplay()
    this.open()
  }

  override fun onClose() {
    if (this.closeCallback != null && !ignoreCloseCallback) {
      this.closeCallback.run()
    }
  }

  protected fun openNextPage() {
    this.page = min(this.pageAmount - 1, this.page + 1)
    this.updateDisplay()
  }

  protected val canNextPage: Boolean
    get() = this.pageAmount > this.page + 1

  protected fun openPrevPage() {
    this.page = max(0, this.page - 1)
    this.updateDisplay()
  }

  protected val canPrevPage: Boolean
    get() = this.page - 1 >= 0

  protected fun updateDisplay() {
    val offset = this.page * PAGE_SIZE

    for (i in 0..<PAGE_SIZE) {
      var element = this.getElement(offset + i)

      if (element == null) {
        element = DisplayElement.empty()
      }

      if (element.element != null) {
        this.setSlot(i, element.element)
      } else if (element.slot != null) {
        this.setSlotRedirect(i, element.slot)
      }
    }

    for (i in 0..8) {
      var navElement: DisplayElement? = this.getNavElement(i)

      if (navElement == null) {
        navElement = DisplayElement.empty()
      }

      if (navElement.element != null) {
        this.setSlot(i + PAGE_SIZE, navElement.element)
      } else if (navElement.slot != null) {
        this.setSlotRedirect(i + PAGE_SIZE, navElement.slot)
      }
    }
  }

  protected abstract val pageAmount: Int

  protected abstract fun getElement(id: Int): DisplayElement?

  protected fun getNavElement(id: Int): DisplayElement? {
    return when (id) {
      1 -> DisplayElement.prevPage(this)
      3 -> DisplayElement.nextPage(this)
      7 -> DisplayElement.of(
        texturedGuiElement("paged_screen/close")
          .setName(
            Component.translatable(if (this.closeCallback != null) "rewards.paged_screen.prev" else "rewards.paged_screen.close")
              .withStyle(ChatFormatting.RED)
          )
          .hideDefaultTooltip()
          .setCallback { _, _, _ ->
            playClickSound(this.player)
            this.close(this.closeCallback != null)
          }
      )

      else -> DisplayElement.filler()
    }
  }

  data class DisplayElement(val element: GuiElementInterface?, val slot: Slot?) {
    companion object {
      private val EMPTY = of(GuiElement(ItemStack.EMPTY, GuiElementInterface.EMPTY_CALLBACK))
      private val FILLER = of(
        texturedGuiElement("paged_screen/blank")
          .setName(Component.empty())
          .hideTooltip()
      )

      fun of(element: GuiElementInterface?): DisplayElement {
        return DisplayElement(element, null)
      }

      fun of(element: GuiElementBuilderInterface<*>): DisplayElement {
        return DisplayElement(element.build(), null)
      }

      fun of(slot: Slot?): DisplayElement {
        return DisplayElement(null, slot)
      }

      fun nextPage(gui: PagedScreen): DisplayElement =
        when (gui.canNextPage) {
          true -> this.of(
            texturedGuiElement("paged_screen/next")
              .setItemName(Component.translatable("rewards.paged_screen.next"))
              .hideDefaultTooltip()
              .setCallback { _, _, _ ->
                playClickSound(gui.player)
                gui.openNextPage()
              }
          )

          false -> this.of(
            texturedGuiElement("paged_screen/next_disabled")
              .hideTooltip()
          )
        }

      fun prevPage(gui: PagedScreen): DisplayElement =
        when (gui.canPrevPage) {
          true -> this.of(
            texturedGuiElement("paged_screen/prev")
              .setItemName(Component.translatable("rewards.paged_screen.prev"))
              .hideDefaultTooltip()
              .setCallback { _, _, _ ->
                playClickSound(gui.player)
                gui.openPrevPage()
              }
          )

          false -> this.of(
            texturedGuiElement("paged_screen/prev_disabled")
              .hideTooltip()
          )
        }

      fun filler(): DisplayElement {
        return FILLER
      }

      fun empty(): DisplayElement {
        return EMPTY
      }
    }
  }

  companion object {
    const val PAGE_SIZE: Int = 9 * 4
    fun playClickSound(player: ServerPlayer) {
      player.connection.send(
        ClientboundSoundEntityPacket(
          SoundEvents.UI_BUTTON_CLICK, SoundSource.UI, player, 1f, 1f, player.getRandom().nextLong()
        )
      )
    }
  }
}