package io.ejekta.bountiful.components

import io.ejekta.bountiful.Bountiful
import io.ejekta.bountiful.content.BountifulContent
import kotlinx.serialization.Serializable
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.world.World

@Serializable @JvmRecord
data class DecreeData(val ids: Set<String> = setOf(), val rank: Int = 1) {
    fun tooltipInfo(world: World): List<Text> {
        return mutableListOf<Text>() + when (ids.isNotEmpty()) {
            true -> {
                ids.map {
                    val dec = BountifulContent.Decrees.firstOrNull { d -> d.id == it }
                    val toText = if (dec?.name != null) {
                        Text.literal(dec.name)
                    } else {
                        Text.translatable("${Bountiful.ID}.decree.$it.name")
                    }
                    toText.formatted(Formatting.GOLD)
                }
            }
            false -> {
                listOf(Text.translatable("bountiful.decree.notset"))
            }
        }
    }



    companion object {
        val EMPTY = DecreeData()

        fun editOn(stack: ItemStack, func: DecreeStack.() -> Unit) {
            DecreeStack(stack).func()
        }
    }
}