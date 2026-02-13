package ru.xllifi.rewards.commands.admin

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import de.phyrone.brig.wrapper.DSLCommandNode
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import ru.xllifi.rewards.commands.Command
import net.minecraft.network.chat.Component
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.progression.commands.getProgressionAndTierIdxArguments
import ru.xllifi.rewards.progression.commands.getProgressionArgument
import ru.xllifi.rewards.progression.commands.progressionArgument
import ru.xllifi.rewards.progression.sql.CollectedProgressionTiers
import ru.xllifi.rewards.progression.sql.CollectedProgressionTiersTable
import ru.xllifi.rewards.progression.sql.setTierCollection
import ru.xllifi.rewards.utils.plus

object AdminProgressionCommands : Command {
  override fun DSLCommandNode<CommandSourceStack>.register() {
    literal("progression") {
      with(AdminTiersCommands) { register() }
    }
  }
}

object AdminTiersCommands : Command {
  fun uncollectAllForEveryone(ctx: CommandContext<CommandSourceStack>): Int {
    val progression = ctx.getProgressionArgument("progression")

    transaction(Main.database) {
      val tiers = CollectedProgressionTiers.find {
        CollectedProgressionTiersTable.progressionId eq progression.id
      }
      for (tier in tiers) {
        tier.delete()
      }
    }

    ctx.source.sendSuccess({
      Component.translatable(
        "rewards.commands.admin.progression.uncollect_all_for_everyone.success",
        progression.id
      )
    }, true)
    return Command.SINGLE_SUCCESS
  }

  fun collectAll(ctx: CommandContext<CommandSourceStack>): Int {
    val progression = ctx.getProgressionArgument("progression")
    val player = EntityArgument.getPlayer(ctx, "player")

    transaction(Main.database) {
      for (index in 0..<progression.tiers.size) {
        CollectedProgressionTiers.new {
          playerUuid = player.uuid
          progressionId = progression.id
          tierIndex = index
        }
      }
    }

    ctx.source.sendSuccess({
      Component.translatable(
        "rewards.commands.admin.progression.collect_all.success",
        progression.id, player.plainTextName
      )
    }, true)
    return Command.SINGLE_SUCCESS
  }

  fun uncollectAll(ctx: CommandContext<CommandSourceStack>): Int {
    val progression = ctx.getProgressionArgument("progression")
    val player = EntityArgument.getPlayer(ctx, "player")

    transaction(Main.database) {
      val tiers = CollectedProgressionTiers.find {
        CollectedProgressionTiersTable.playerUuid.eq(player.uuid) +
        CollectedProgressionTiersTable.progressionId.eq(progression.id)
      }
      for (tier in tiers) {
        tier.delete()
      }
    }

    ctx.source.sendSuccess({
      Component.translatable(
        "rewards.commands.admin.progression.uncollect_all.success",
        progression.id, player.plainTextName
      )
    }, true)
    return Command.SINGLE_SUCCESS
  }

  fun collect(ctx: CommandContext<CommandSourceStack>): Int {
    val (progression, tierIdx) = ctx.getProgressionAndTierIdxArguments("progression", "tier_index")
    val player = EntityArgument.getPlayer(ctx, "player")

    progression.setTierCollection(player, tierIdx, true)

    ctx.source.sendSuccess({
      Component.translatable(
        "rewards.commands.admin.progression.tier.collect.success",
        tierIdx, player.plainTextName
      )
    }, true)
    return Command.SINGLE_SUCCESS
  }

  fun uncollect(ctx: CommandContext<CommandSourceStack>): Int {
    val (progression, tierIdx) = ctx.getProgressionAndTierIdxArguments("progression", "tier_index")
    val player = EntityArgument.getPlayer(ctx, "player")

    progression.setTierCollection(player, tierIdx, false)

    ctx.source.sendSuccess({
      Component.translatable(
        "rewards.commands.admin.progression.tier.uncollect.success",
        tierIdx, player.plainTextName
      )
    }, true)
    return Command.SINGLE_SUCCESS
  }

  override fun DSLCommandNode<CommandSourceStack>.register() {
    literal("tier") {
      literal("uncollect_all_for_everyone") {
        progressionArgument("progression") {
          executes { ctx -> uncollectAllForEveryone(ctx) }
        }
      }
      argument("player", EntityArgument.player()) {
        progressionArgument("progression") {
          literal("collect_all") { executes { ctx -> collectAll(ctx) } }
          literal("uncollect_all") { executes { ctx -> uncollectAll(ctx) } }
          argument("tier_index", IntegerArgumentType.integer()) {
            literal("collect") { executes { ctx -> collect(ctx) } }
            literal("uncollect") { executes { ctx -> uncollect(ctx) } }
          }
        }
      }
    }
  }
}