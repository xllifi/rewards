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
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.cosmetic.commands.getCosmeticArgument
import ru.xllifi.rewards.cosmetic.sql.CollectedCosmetic
import ru.xllifi.rewards.cosmetic.sql.CollectedCosmeticsTable

object AdminCosmeticsCommands : Command {
  fun setCollected(ctx: CommandContext<CommandSourceStack>): Int {
    val player = EntityArgument.getPlayer(ctx, "player")
    val (cosmeticKind, cosmetic) = ctx.getCosmeticArgument("cosmetic_kind", "cosmetic")
    val isCollected = BoolArgumentType.getBool(ctx, "is_collected")
    if (isCollected) {
      cosmetic.updateOrCreateFor(player, null)
    } else {
      transaction(Main.database) {
        CollectedCosmetic.findById(
          CompositeID {
            it[CollectedCosmeticsTable.playerUuid] = player.uuid
            it[CollectedCosmeticsTable.cosmeticKind] = cosmeticKind
            it[CollectedCosmeticsTable.cosmeticId] = cosmetic.id
          }
        )?.delete()
      }
    }
    ctx.source.sendSystemMessage(
      Component.translatable(
        "command.rewards.cosmetic.set_collected.success",
        player.displayName,
        Component.literal("${cosmeticKind.snakeCaseName()}:${cosmetic.id}").withStyle(ChatFormatting.YELLOW),
        Component.translatable("command.rewards.cosmetic.set_collected.success.to_$isCollected")
      )
    )
    return Command.SINGLE_SUCCESS
  }

  override fun DSLCommandNode<CommandSourceStack>.register() {
    literal("set_collected") {
      argument("player", EntityArgument.player()) {
        cosmeticKindArgument("cosmetic_kind") {
          cosmeticArgument("cosmetic_kind", "cosmetic") {
            argument("is_collected", BoolArgumentType.bool()) {
              executes { ctx -> setCollected(ctx) }
            }
          }
        }
      }
    }
  }
}