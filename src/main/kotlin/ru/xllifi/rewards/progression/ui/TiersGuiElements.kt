package ru.xllifi.rewards.progression.ui

import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.ChatFormatting
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.sounds.SoundSource
import ru.xllifi.rewards.logger
import ru.xllifi.rewards.progression.Progression
import ru.xllifi.rewards.progression.sql.setTierCollection
import ru.xllifi.rewards.rewards.grant
import ru.xllifi.rewards.utils.ui.texturedGuiElement

val ProgressionScreen.blankGuiElement: GuiElement
  get() = texturedGuiElement("progression/blank").hideTooltip().build()

val ProgressionScreen.noTierGuiElement: GuiElement
  get() = texturedGuiElement("progression/blank").hideTooltip().build()

fun ProgressionScreen.pendingTierGuiElement(tier: Progression.Tier): GuiElement =
  texturedGuiElement("progression/pending")
    .setItemName(Component.translatable("rewards.progression.tier.pending").withStyle(ChatFormatting.GRAY))
    .setLore(
      listOf(
        Component.translatable("rewards.progression.tier.pending.lore").withStyle(ChatFormatting.GRAY),
        *tier.unlockCondition.lore(player).toTypedArray(),
      )
    )
    .build()

fun ProgressionScreen.failedTierGuiElement(tier: Progression.Tier): GuiElement =
  texturedGuiElement("progression/failed")
    .setItemName(Component.translatable("rewards.progression.tier.failed").withStyle(ChatFormatting.GRAY))
    .setLore(
      listOf(
        Component.translatable("rewards.progression.tier.failed.lore").withStyle(ChatFormatting.GRAY),
        *tier.unlockCondition.lore(player).toTypedArray(),
      )
    )
    .build()

fun ProgressionScreen.collectedTierGuiElement(tier: Progression.Tier): GuiElement =
  texturedGuiElement("progression/collected")
    .setItemName(Component.translatable("rewards.progression.tier.collected").withStyle(ChatFormatting.GRAY))
    .setLore(
      listOf(
        Component.translatable("rewards.progression.tier.collected.lore").withStyle(ChatFormatting.GRAY),
        *tier.rewards.map { it.lore() }.toTypedArray(),
      )
    )
    .build()

fun ProgressionScreen.completedTierGuiElement(tier: Progression.Tier): GuiElement =
  GuiElementBuilder(tier.displayItem)
    .setName(audiences.asNative(tier.title))
    .setLore(
      listOf(
        *tier.description.map { audiences.asNative(it) }.toTypedArray(),
        Component.empty(),
        Component.translatable("rewards.generic.rewards").withStyle(ChatFormatting.GRAY),
        *tier.rewards.map { it.lore() }.toTypedArray(),
        Component.empty(),
        Component.translatable("rewards.generic.click_to_collect").withStyle(ChatFormatting.YELLOW),
      )
    )
    .setCallback { _, _, _ ->
      progression.setTierCollection(player, progression.tiers.indexOf(tier), true)
      if (tier.collectionSound != null) {
        try {
          val sound = player.registryAccess()
            .get(
              ResourceKey.create(
                Registries.SOUND_EVENT,
                tier.collectionSound
              )
            ).get().value()
          player.playNotifySound(sound, SoundSource.UI, 1f, 1f)
        } catch (e: Exception) {
          logger.error("Failed to play sound ${tier.collectionSound} to player ${player.plainTextName}: ${e.stackTraceToString()}")
        }
      }
      tier.rewards.grant(player)
      updateDisplay()
    }
    .build()