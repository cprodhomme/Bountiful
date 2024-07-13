package io.ejekta.bountiful.content.item

import io.ejekta.bountiful.bounty.DecreeData
import io.ejekta.bountiful.content.BountifulContent
import io.ejekta.bountiful.decree.DecreeSpawnCondition
import io.ejekta.bountiful.decree.DecreeSpawnRank
import io.ejekta.kambrik.bridge.Kambridge
import net.minecraft.client.MinecraftClient
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class DecreeItem : Item(
    Settings().maxCount(1).fireproof()
) {

    override fun getTranslationKey() = "bountiful.decree"

    override fun getName(stack: ItemStack?): Text {
        return Text.translatable(translationKey).formatted(Formatting.DARK_PURPLE)
    }

    override fun appendTooltip(
        stack: ItemStack?,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType?
    ) {
        if (Kambridge.isOnServer()) {
            return
        }
        if (stack != null) {
            val data = stack[BountifulContent.DECREE_DATA]?.tooltipInfo(MinecraftClient.getInstance().world!!)
            tooltip.addAll(data ?: emptySet())
        }
        super.appendTooltip(stack, context, tooltip, type)
    }

    companion object {
        fun create(
            spawnCondition: DecreeSpawnCondition,
            ranked: Int = 1,
            spawnRank: DecreeSpawnRank = DecreeSpawnRank.CONSTANT
        ): ItemStack {
            val spawnableDecrees = BountifulContent.Decrees.filter(spawnCondition.spawnFunc).map { it.id }
            return create(spawnableDecrees, ranked, spawnRank)
        }

        fun createWithAllDecrees(): ItemStack {
            val decIds = BountifulContent.Decrees.map { it.id }
            return create(decIds, decIds.size)
        }

        fun create(
            decIds: List<String>,
            ranked: Int = 1,
            spawnRank: DecreeSpawnRank = DecreeSpawnRank.CONSTANT
        ): ItemStack {
            val dd = DecreeData(rank = ranked)
            spawnRank.populateFunc(dd, decIds)
            return ItemStack(BountifulContent.DECREE_ITEM).apply {
                this[BountifulContent.DECREE_DATA] = dd
            }
        }
    }

}