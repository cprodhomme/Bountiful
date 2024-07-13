package io.ejekta.bountiful.components

import io.ejekta.bountiful.bounty.BountyData
import io.ejekta.bountiful.bounty.BountyRarity
import io.ejekta.bountiful.config.BountifulIO
import io.ejekta.bountiful.util.GameTime
import kotlinx.serialization.Serializable
import net.minecraft.client.MinecraftClient
import net.minecraft.item.Item
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.world.World
import kotlin.math.max

@Serializable @JvmRecord
data class BountyInfo(
    val rarity: BountyRarity,
    val timeStarted: Long,
    val timeToComplete: Long,
    val timePickedUp: Long
) {

    fun timeLeftTicks(world: World): Long {
        return when (BountifulIO.configData.bounty.shouldHaveTimersAndExpire) {
            true -> max(timeStarted - world.time + (timeToComplete * GameTime.TICK_RATE), 0L)
            false -> 1L
        }
    }

    fun timeLeftSecs(world: World): Long {
        return timeLeftTicks(world) / GameTime.TICK_RATE
    }

    fun timeTakenTicks(world: World): Long {
        return world.time - timePickedUp
    }

    fun timeTakenSecs(world: World): Long {
        return timeTakenTicks(world) / GameTime.TICK_RATE
    }

    // ### Formatting ### //

    fun formattedTimeLeft(world: World): Text {
        return GameTime.formatTimeExpirable(timeLeftSecs(world))
    }

    fun genTooltip(fromData: BountyData, isServer: Boolean, context: Item.TooltipContext, type: TooltipType): List<MutableText> {
        if (isServer) {
            return emptyList()
        }
        val player = MinecraftClient.getInstance().player!!
        return buildList {
            add(Text.translatable("bountiful.tooltip.required").formatted(Formatting.GOLD).append(":"))
            addAll(fromData.objectives.map {
                it.textSummary(player, true)
            })
            add(Text.translatable("bountiful.tooltip.rewards").formatted(Formatting.GOLD).append(":"))
            addAll(fromData.rewards.map {
                it.textSummary(player, false)
            })

            // TODO reimplement advanced tooltip contexts
            if (type == TooltipType.ADVANCED && BountifulIO.configData.client.advancedDebugTooltips) {
                add(Text.literal(""))
                add(Text.literal("Bountiful Debug Info:").formatted(Formatting.GOLD))
                add(Text.literal("Taken: ${timeTakenSecs(player.world)}, Left: ${timeLeftSecs(player.world)}"))
            }
        }
    }

    companion object {
        val DEFAULT = BountyInfo(BountyRarity.COMMON, -1L, -1L, -1L)
    }


}