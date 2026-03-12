package ru.xllifi.rewards.commands.admin

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.context.CommandContext
import de.phyrone.brig.wrapper.DSLCommandNode
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import ru.xllifi.rewards.commands.Command
import ru.xllifi.rewards.cosmetic.commands.cosmeticArgument
import ru.xllifi.rewards.cosmetic.commands.cosmeticKindArgument
import net.minecraft.network.chat.Component
import ru.xllifi.rewards.cosmetic.commands.getCosmeticArgument

object AdminCosmeticsCommands : Command {
  fun setCollected(ctx: CommandContext<CommandSourceStack>): Int {
    val player = EntityArgument.getPlayer(ctx, "player")
    val (cosmeticKind, cosmetic) = ctx.getCosmeticArgument("cosmetic_kind", "cosmetic")
    val isEquipped = BoolArgumentType.getBool(ctx, "is_equipped")
    cosmetic.updateOrCreateFor(player, isEquipped)
    ctx.source.sendSystemMessage(
      Component.translatable(
        "command.rewards.cosmetic.set_collected.success",
        player.displayName,
        Component.literal("${cosmeticKind.snakeCaseName()}:${cosmetic.id}").withStyle(ChatFormatting.YELLOW),
        Component.translatable("command.rewards.cosmetic.set_collected.success.to_$isEquipped")
      )
    )
    return Command.SINGLE_SUCCESS
  }

  override fun DSLCommandNode<CommandSourceStack>.register() {
    literal("set_collected") {
      argument("player", EntityArgument.player()) {
        cosmeticKindArgument("cosmetic_kind") {
          cosmeticArgument("cosmetic_kind", "cosmetic") {
            argument("is_equipped", BoolArgumentType.bool()) {
              executes { ctx -> setCollected(ctx) }
            }
          }
        }
      }
    }
  }
}