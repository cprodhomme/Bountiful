package io.ejekta.bountiful.content.item

import io.ejekta.bountiful.bounty.BountyData
import io.ejekta.bountiful.bounty.BountyInfo
import io.ejekta.bountiful.bounty.BountyRarity
import io.ejekta.bountiful.config.BountifulIO
import io.ejekta.bountiful.content.BountifulContent
import io.ejekta.kambrik.bridge.Kambridge
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.world.World
import java.util.*

class BountyItem : Item(
    Settings().maxCount(1).fireproof()
) {

    override fun getName(stack: ItemStack): Text {
        // TODO this may be sided
        val info = stack[BountifulContent.BOUNTY_INFO] ?: return Text.literal("ERR? No Info")
        var text = Text.translatable(info.rarity.name.lowercase()
            // Capitalizing
            .replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            } + " Bounty ").formatted(info.rarity.color)
        if (info.rarity == BountyRarity.LEGENDARY) {
            text = text.formatted(Formatting.BOLD)
        }
        if (BountifulIO.configData.bounty.shouldHaveTimersAndExpire) {
            text = text.append(
                Text.literal("(")
                    .append(info.formattedTimeLeft(MinecraftClient.getInstance().world!!))
                    .append(Text.literal(")"))
                    .formatted(Formatting.WHITE)
            )
        }
        return text
    }

    fun tryCashIn(player: PlayerEntity, stack: ItemStack): Boolean {

        if (stack[BountifulContent.BOUNTY_INFO]!!.timeLeftTicks(player.world) <= 0) {
            player.sendMessage(Text.translatable("bountiful.bounty.expired"))
            return false
        }

        val objs = stack[BountifulContent.BOUNTY_OBJS]
        val comp = stack[BountifulContent.BOUNTY_COMPLETION]?.amounts ?: emptyMap()
        return if (objs?.hasFinishedAll(player, comp) == true) {
            objs.tryFinish(player, stack[BountifulContent.BOUNTY_COMPLETION]?.amounts ?: emptyMap())
            stack[BountifulContent.BOUNTY_REWS]!!.rewardPlayer(player)
            stack.decrement(stack.maxCount)
            true
        } else {
            player.sendMessage(Text.translatable("bountiful.tooltip.requirements"), false)
            false
        }
    }

    // TODO genTooltip should probably be moved to this class since it contains both entry lists
    override fun appendTooltip(
        stack: ItemStack?,
        context: TooltipContext,
        tooltip: MutableList<Text>?,
        type: TooltipType
    ) {
        if (Kambridge.isOnServer()) {
            return
        }
        if (stack != null) {
            val data = stack[BountifulContent.BOUNTY_INFO]?.genTooltip(BountyData[stack], Kambridge.isOnServer(), context, type)
            tooltip?.addAll(data ?: emptySet())
        }
        super.appendTooltip(stack, context, tooltip, type)
    }

}