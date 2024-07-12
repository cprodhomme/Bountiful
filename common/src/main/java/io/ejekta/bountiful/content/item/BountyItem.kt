package io.ejekta.bountiful.content.item

import io.ejekta.bountiful.bounty.BountyData
import io.ejekta.bountiful.bounty.BountyInfo
import io.ejekta.bountiful.bounty.BountyRarity
import io.ejekta.bountiful.config.BountifulIO
import net.minecraft.client.MinecraftClient
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
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
        val info = BountyInfo[stack]
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

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext
    ) {
        if (stack != null && world != null) {
            val data = BountyInfo[stack].genTooltip(BountyData[stack], world is ServerWorld, context)
            tooltip?.addAll(data)
        }
        super.appendTooltip(stack, world, tooltip, context)
    }

}