package io.ejekta.bountiful.components

import io.ejekta.bountiful.bounty.BountyRarity
import io.ejekta.bountiful.bounty.types.BountyTypeRegistry
import io.ejekta.bountiful.bounty.types.IBountyType
import io.ejekta.bountiful.content.BountifulContent
import io.ejekta.bountiful.data.Decree
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

// Tracks the status of a given bounty
@Serializable @JvmRecord
data class BountyDataEntry(
    val id: String,
    val logicId: @Contextual Identifier,
    val content: String,
    val amount: Int,
    val worth: Int,
    val nbt: @Contextual NbtCompound? = null,
    val name: String? = null,
    val icon: @Contextual Identifier? = null,
    val isMystery: Boolean = false,
    val rarity: BountyRarity = BountyRarity.COMMON,
    val tracking: JsonObject = JsonObject(emptyMap()), // Used to track extra data, e.g. current progress if needed
    val critConditions: JsonObject? = null,
    val relatedDecreeIds: Set<String> = emptySet()
) {

    private fun getRelatedDecrees(): Set<Decree> {
        return BountifulContent.getDecrees(relatedDecreeIds)
    }

    fun getRelatedProfessions(): Set<String> {
        return getRelatedDecrees().map { it.linkedProfessions }.flatten().toSet()
    }

    val translation: MutableText
        get() = Text.translatable("bountiful.entry.${id}")

    val logic: IBountyType
        get() = BountyTypeRegistry[logicId]!!

    override fun toString(): String {
        return "BDE[type=$logic, content=$content, amount=$amount, isNbtNull=${nbt == null}, name=$name, mystery=$isMystery]"
    }

    fun textBoard(player: PlayerEntity): List<Text> {
        return logic.textBoard(this, player)
    }

    fun textSummary(player: PlayerEntity, isObj: Boolean): MutableText {
        return when (isMystery) {
            true -> Text.literal("???").formatted(Formatting.BOLD).append(
                Text.literal("x$amount").formatted(Formatting.WHITE)
            )
            false -> logic.textSummary(this, isObj, player)
        }
    }

}