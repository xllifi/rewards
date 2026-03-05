package ru.xllifi.rewards.locker.items

import com.mojang.brigadier.context.CommandContext
import eu.pb4.placeholders.api.PlaceholderResult
import eu.pb4.placeholders.api.Placeholders
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
import ru.xllifi.rewards.locker.sql.CollectedLockerItem
import ru.xllifi.rewards.locker.sql.CollectedLockerItemTable
import ru.xllifi.rewards.utils.plus
import ru.xllifi.rewards.utils.id
import ru.xllifi.rewards.utils.ui.DEFAULT_COLOR
import ru.xllifi.rewards.utils.ui.texturedGuiElement
import ru.xllifi.rewards.serializers.text.Component as AdvComponent
import net.minecraft.network.chat.Component as McComponent

@Serializable
@SerialName("prefix")
object PrefixItemKind : LockerItemKind {
  override val name: String = "prefix"
  override fun getGuiElementBuilder(): GuiElementBuilder =
    texturedGuiElement("locker_item/prefix")
      .setItemName(McComponent.translatable("rewards.reward.locker_item.prefix.kind"))
}

@Serializable
@SerialName("prefix")
@RegisteredLockerItem(PrefixItemKind::class)
class PrefixLockerItem(
  val component: AdvComponent,
) : LockerItem() {
  override val kind: LockerItemKind = PrefixItemKind

  override fun getGuiElementBuilder(audiences: MinecraftServerAudiences): GuiElementBuilder =
    texturedGuiElement("locker_item/prefix", this.getNative(audiences).style.color?.value ?: DEFAULT_COLOR)
      .setItemName(this.getNative(audiences))

  fun getNative(player: ServerPlayer): McComponent =
    player.level().server.getServerAttachment().audiences.asNative(this.component)

  fun getNative(server: MinecraftServer): McComponent =
    server.getServerAttachment().audiences.asNative(this.component)

  fun getNative(ctx: CommandContext<CommandSourceStack>): McComponent =
    ctx.getServerAttachment().audiences.asNative(this.component)

  fun getNative(audiences: MinecraftServerAudiences): McComponent =
    audiences.asNative(this.component)
}

fun setupPrefixPlaceholder() {
  Placeholders.register(id(modId, "locker_prefix")) { ctx, _ ->
    if (!ctx.hasPlayer())
      return@register PlaceholderResult.invalid("No player!")
    val player = ctx.player!!

    val equippedPrefixes: List<PrefixLockerItem> = transaction {
      CollectedLockerItem.find {
        CollectedLockerItemTable.playerUuid.eq(player.uuid) +
          CollectedLockerItemTable.kind.eq(PrefixItemKind) +
          CollectedLockerItemTable.equipped.eq(true)
      }.toList().mapNotNull { it.item as? PrefixLockerItem }
    }

    val component: MutableComponent = McComponent.empty()
    equippedPrefixes
      .map { it.getNative(ctx.server) }
      .forEach { component.append(it).append(" ") }

    return@register PlaceholderResult.value(component)
  }
}