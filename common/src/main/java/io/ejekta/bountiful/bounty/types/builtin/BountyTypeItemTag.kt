package io.ejekta.bountiful.bounty.types.builtin

import io.ejekta.bountiful.bounty.types.IBountyExchangeable
import io.ejekta.bountiful.bounty.types.Progress
import io.ejekta.bountiful.components.BountyDataEntry
import io.ejekta.bountiful.data.PoolEntry
import io.ejekta.bountiful.util.getTagItemKey
import io.ejekta.bountiful.util.getTagItems
import io.ejekta.kambrik.ext.collect
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.TagKey
import net.minecraft.server.MinecraftServer
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.world.World


class BountyTypeItemTag : IBountyExchangeable {

    override val id: Identifier = Identifier.of("item_tag")

    private fun entryAppliesToStack(entry: BountyDataEntry, stack: ItemStack): Boolean {
        return stack.isIn(TagKey.of(Registries.ITEM.key, Identifier.of(entry.content)))
    }

    override fun isValid(entry: PoolEntry, server: MinecraftServer): Boolean {
        return getTagItems(server.registryManager, getTagItemKey(Identifier.of(entry.content))).isNotEmpty()
    }

    private fun getCurrentStacks(entry: BountyDataEntry, player: PlayerEntity): Map<ItemStack, Int>? {
        return player.inventory.main.collect(entry.amount) {
            entryAppliesToStack(entry, this)
        }
    }

    override fun textSummary(entry: BountyDataEntry, isObj: Boolean, player: PlayerEntity): MutableText {
        val progress = getProgress(entry, player)
        val title = if (entry.name != null) Text.literal(entry.name) else entry.translation
        return when (isObj) {
            true -> title.copy().formatted(progress.color).append(progress.neededText.colored(Formatting.WHITE))
            false -> progress.givingText.append(title.colored(entry.rarity.color))
        }
    }

    override fun textBoard(entry: BountyDataEntry, player: PlayerEntity): List<Text> {
        return listOf(
            if (entry.name != null) {
                Text.literal(entry.name!!)
            } else {
                entry.translation
            },
            Text.literal(entry.content).formatted(Formatting.DARK_GRAY)
        )
    }

    override fun getProgress(entry: BountyDataEntry, player: PlayerEntity, current: Int): Progress {
        return Progress(getCurrentStacks(entry, player)?.values?.sum() ?: 0, entry.amount)
    }

    override fun getNewCurrent(entry: BountyDataEntry, player: PlayerEntity, current: Int): Int {
        return getCurrentStacks(entry, player)?.values?.sum() ?: 0
    }

    override fun tryFinishObjective(entry: BountyDataEntry, player: PlayerEntity, current: Int): Boolean {
        return getCurrentStacks(entry, player)?.let {
            it.forEach { (stack, toShrink) ->
                stack.decrement(toShrink)
            }
            true
        } ?: false
    }

    override fun giveReward(entry: BountyDataEntry, player: PlayerEntity) {
        // NO-OP
    }

    companion object {
        private fun getTag(entry: BountyDataEntry) = TagKey.of(Registries.ITEM.key, Identifier.of(entry.content))


        fun getItems(world: World, entry: BountyDataEntry): List<Item> {
            return getTagItems(world.registryManager, getTag(entry))
        }
    }

}