package ru.xllifi.rewards.cosmetic

import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.network.chat.Component
import net.pearx.kasechange.toSnakeCase
import ru.xllifi.rewards.utils.ui.texturedGuiElement

enum class CosmeticKind {
  Prefix,
  Suffix,
  ;

  fun snakeCaseName(): String = this.name.toSnakeCase()

  fun getGuiElementBuilder(): GuiElementBuilder =
    when (this) {
      Prefix -> texturedGuiElement("cosmetic_kind/prefix")
      Suffix -> texturedGuiElement("cosmetic_kind/suffix")
    }
}