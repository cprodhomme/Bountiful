package io.ejekta.bountiful.bounty.types

import io.ejekta.bountiful.components.BountyDataEntry
import io.ejekta.bountiful.data.PoolEntry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

interface IBountyType {

    val id: Identifier

    fun textOnBounty(entry: BountyDataEntry, isObj: Boolean, player: PlayerEntity, current: Int): MutableText

    fun textOnBoardSidebar(entry: BountyDataEntry, player: PlayerEntity): List<Text>

    fun isValid(entry: PoolEntry, server: MinecraftServer): Boolean

    fun getDescription(entry: BountyDataEntry): MutableText {
        return entry.name?.let {
            Text.literal(it)
        } ?: Text.translatable(entry.id)
    }

    // ### Helpers ###

    val Pair<Int, Int>.isDone: Boolean
        get() = first == second

    val Pair<Int, Int>.color: Formatting
        get() = if (isDone) Formatting.GREEN else Formatting.RED

    fun Text.colored(progress: Pair<Int, Int>): MutableText {
        return copy().formatted(progress.color)
    }

    fun Text.colored(formatting: Formatting): MutableText {
        return copy().formatted(formatting)
    }

    val Pair<Int, Int>.needed
        get() = Text.literal(" ($first/$second)")

    val Pair<Int, Int>.giving
        get() = Text.literal("${second}x ")

}