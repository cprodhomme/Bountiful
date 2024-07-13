package io.ejekta.bountiful.components

import io.ejekta.bountiful.bounty.BountyData
import io.ejekta.bountiful.bounty.BountyDataEntry
import io.ejekta.bountiful.bounty.types.IBountyObjective
import io.ejekta.bountiful.bounty.types.IBountyReward
import io.ejekta.bountiful.content.BountifulContent
import io.ejekta.bountiful.messages.OnBountyComplete
import kotlinx.serialization.Serializable
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents

@Serializable @JvmRecord
data class BountyEntries(val entries: List<BountyDataEntry>) {

    // Objectives
    fun hasFinishedAll(player: PlayerEntity, completions: Map<String, Int>): Boolean {
        return entries.all {
            (it.logic as IBountyObjective).getProgress(it, player, completions[it.id] ?: 0).isComplete()
        }
    }

    fun tryFinish(player: PlayerEntity, completions: Map<String, Int>): Boolean {
        return entries.all {
            (it.logic as IBountyObjective).tryFinishObjective(it, player, completions[it.id] ?: 0)
        }
    }


    fun isDone(player: PlayerEntity, stack: ItemStack): Boolean {
        return entries.all {
            (it.logic as IBountyObjective).getProgress(it, player).isComplete()
        } && (stack[BountifulContent.BOUNTY_INFO]!!.timeLeftTicks(player.world) > 0)
    }

    // Rewards
    fun rewardPlayer(player: PlayerEntity) {
        // Play XP pickup sound
        player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)

        // Give XP to player
        player.addExperience(entries.sumOf { (it.rarity.ordinal) * 2 + 1 })

        for (reward in entries) {
            (reward.logic as IBountyReward).giveReward(reward, player)
        }
    }


    fun checkForCompletionAndAlert(player: PlayerEntity, stack: ItemStack): BountyData {
        if (isDone(player, stack)) {

            val ping = stack[BountifulContent.BOUNTY_PING] ?: BountyPing(false)

            if (!ping.complete) {
                stack[BountifulContent.BOUNTY_PING] = BountyPing(true)
                val playAction = OnBountyComplete(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)

                if (player is ServerPlayerEntity) {
                    playAction.sendToClient(player)
                } else {
                    playAction.runLocally(player)
                }
            } else {
                // do nothing was already complete
            }

        } else {
            stack[BountifulContent.BOUNTY_PING] = BountyPing(false)

        }

        return this
    }

}