package io.ejekta.bountiful.bounty.types.builtin

import io.ejekta.bountiful.bounty.types.IBountyObjective
import io.ejekta.bountiful.bounty.types.Progress
import io.ejekta.bountiful.components.BountyDataEntry
import io.ejekta.bountiful.data.PoolEntry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier


class BountyTypeCriteria : IBountyObjective {

    override val id: Identifier = Identifier.of("criteria")

    override fun isValid(entry: PoolEntry, server: MinecraftServer): Boolean {
        return true // TODO can we validate Criteria?
    }

    override fun textOnBounty(entry: BountyDataEntry, isObj: Boolean, player: PlayerEntity, current: Int): MutableText {
        val progress = getProgress(entry, player, current)
        val textSum = if (entry.name != null) Text.literal(entry.name) else entry.translation
        return textSum.colored(progress.color).append(progress.neededText.colored(Formatting.WHITE))
    }

    override fun textOnBoardSidebar(entry: BountyDataEntry, player: PlayerEntity): List<Text> {
        return listOf(
            if (entry.name != null) Text.literal(entry.name) else entry.translation
        )
    }

    override fun getProgress(entry: BountyDataEntry, player: PlayerEntity, current: Int): Progress {
        return Progress(current, entry.amount)
    }

    override fun consumeObjectives(entry: BountyDataEntry, player: PlayerEntity, current: Int): Boolean {
        return current >= entry.amount
    }

}