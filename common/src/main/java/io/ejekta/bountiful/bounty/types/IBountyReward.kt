package io.ejekta.bountiful.bounty.types

import io.ejekta.bountiful.components.BountyDataEntry
import net.minecraft.entity.player.PlayerEntity

interface IBountyReward : IBountyType {
    fun giveReward(entry: BountyDataEntry, player: PlayerEntity)
}