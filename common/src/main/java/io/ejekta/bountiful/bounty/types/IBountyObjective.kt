package io.ejekta.bountiful.bounty.types

import io.ejekta.bountiful.components.BountyDataEntry
import net.minecraft.entity.player.PlayerEntity

interface IBountyObjective : IBountyType {
    fun getProgress(entry: BountyDataEntry, player: PlayerEntity, current: Int): Progress {
        return Progress(current, entry.amount)
    }

    fun tryFinishObjective(entry: BountyDataEntry, player: PlayerEntity, current: Int): Boolean {
        return current >= entry.amount
    }

    fun getNewCurrent(entry: BountyDataEntry, player: PlayerEntity, current: Int): Int {
        return current
    }

}