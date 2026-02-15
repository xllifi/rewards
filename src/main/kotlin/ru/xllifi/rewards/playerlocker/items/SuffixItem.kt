package ru.xllifi.rewards.playerlocker.items

import com.mojang.brigadier.context.CommandContext
import eu.pb4.placeholders.api.PlaceholderResult
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.elements.GuiElementBuilder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.xllifi.rewards.config.getServerAttachment
import ru.xllifi.rewards.modId
import ru.xllifi.rewards.playerlocker.sql.CollectedLockerItem
import ru.xllifi.rewards.playerlocker.sql.CollectedLockerItemTable
import ru.xllifi.rewards.utils.plus
import ru.xllifi.rewards.utils.resLoc
import ru.xllifi.rewards.utils.ui.DEFAULT_COLOR
import ru.xllifi.rewards.utils.ui.texturedGuiElement
import ru.xllifi.rewards.serializers.text.Component as AdvComponent
import net.minecraft.network.chat.Component as McComponent

@Serializable
@SerialName("suffix")
object SuffixItemKind : LockerItemKind {
  override val name: String = "suffix"
  override fun getGuiElementBuilder(): GuiElementBuilder =
    texturedGuiElement("locker_item/suffix")
      .setItemName(McComponent.translatable("rewards.reward.locker_item.suffix.kind"))
}

@Serializable
@SerialName("suffix")
@RegisteredLockerItem(SuffixItemKind::class)
class SuffixLockerItem(
  val component: AdvComponent,
) : LockerItem() {
  override val kind: LockerItemKind = SuffixItemKind

  override fun getGuiElement(audiences: MinecraftServerAudiences): GuiElement =
    texturedGuiElement("locker_item/suffix", this.getNative(audiences).style.color?.value ?: DEFAULT_COLOR)
      .setItemName(this.getNative(audiences))
      .build()

  fun getNative(player: ServerPlayer): McComponent =
    player.level().server.getServerAttachment().audiences.asNative(this.component)

  fun getNative(server: MinecraftServer): McComponent =
    server.getServerAttachment().audiences.asNative(this.component)

  fun getNative(ctx: CommandContext<CommandSourceStack>): McComponent =
    ctx.getServerAttachment().audiences.asNative(this.component)

  fun getNative(audiences: MinecraftServerAudiences): McComponent =
    audiences.asNative(this.component)
}

fun setupSuffixPlaceholder() {
  Placeholders.register(resLoc(modId, "locker_suffix")) { ctx, _ ->
    if (!ctx.hasPlayer())
      return@register PlaceholderResult.invalid("No player!")
    val player = ctx.player!!

    val equippedSuffixes: List<SuffixLockerItem> = transaction {
      CollectedLockerItem.find {
        CollectedLockerItemTable.playerUuid.eq(player.uuid) +
          CollectedLockerItemTable.kind.eq(SuffixItemKind) +
          CollectedLockerItemTable.equipped.eq(true)
      }.toList().mapNotNull { it.item as? SuffixLockerItem }
    }

    val component: MutableComponent = McComponent.empty()
    equippedSuffixes
      .map { it.getNative(ctx.server) }
      .forEach { component.append(it) }

    return@register PlaceholderResult.value(component)
  }
}