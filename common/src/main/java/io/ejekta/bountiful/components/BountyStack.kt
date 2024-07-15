package io.ejekta.bountiful.components

import io.ejekta.bountiful.bounty.types.IBountyObjective
import io.ejekta.bountiful.bounty.types.IBountyReward
import io.ejekta.bountiful.config.BountifulIO
import io.ejekta.bountiful.content.BountifulContent
import io.ejekta.bountiful.messages.OnBountyComplete
import io.ejekta.kambrik.component.ComponentDelegate
import net.minecraft.client.MinecraftClient
import net.minecraft.component.ComponentType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class BountyStack(val stack: ItemStack) {

    private fun <C : Any?> component(type: ComponentType<C>) = ComponentDelegate(stack, type)

    var completion: Map<BountyDataEntry, Int>
        get() {
            val completionMap = stack[BountifulContent.BOUNTY_COMPLETION]?.amounts ?: emptyMap()
            return objs.associateWith { (completionMap[it.id] ?: 0) }
        }
        set(value) { stack[BountifulContent.BOUNTY_COMPLETION] = BountyCompletion(value.mapKeys { it.key.id }) }

    var info: BountyInfo
        get() = stack[BountifulContent.BOUNTY_INFO] ?: BountyInfo.EMPTY
        set(value) { stack[BountifulContent.BOUNTY_INFO] = value }

    var objs: List<BountyDataEntry>
        get() = stack[BountifulContent.BOUNTY_OBJS]?.entries ?: emptyList()
        set(value) { stack[BountifulContent.BOUNTY_OBJS] = BountyEntries(value) }

    var rews: List<BountyDataEntry>
        get() = stack[BountifulContent.BOUNTY_REWS]?.entries ?: emptyList()
        set(value) { stack[BountifulContent.BOUNTY_REWS] = BountyEntries(value) }

    var ping: Boolean
        get() = stack[BountifulContent.BOUNTY_PING]?.complete ?: false
        set(value) { stack[BountifulContent.BOUNTY_PING] = BountyPing(value) }

    fun progressOf(entry: BountyDataEntry): Int {
        return completion[entry] ?: 0
    }

    fun advance(entry: BountyDataEntry) {
        completion = completion.toMutableMap().apply {
            this[entry] = (this[entry] ?: 0) + 1
        }
    }

    // Objectives

    private fun hasFinishedObjectives(player: PlayerEntity): Boolean {
        return objs.all {
            (it.logic as IBountyObjective).getProgress(it, player, progressOf(it)).isComplete()
        }
    }

    private fun consumeObjectives(player: PlayerEntity): Boolean {
        return objs.all {
            (it.logic as IBountyObjective).consumeObjectives(it, player, progressOf(it))
        }
    }

    private fun isDone(player: PlayerEntity): Boolean {
        return objs.all {
            (it.logic as IBountyObjective).getProgress(it, player, progressOf(it)).isComplete()
        } && ((info.timeLeftTicks(player.world)) > 0)
    }

    // Rewards
    private fun rewardPlayer(player: PlayerEntity) {
        // Play XP pickup sound
        player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)

        // Give XP to player
        player.addExperience(rews.sumOf { (it.rarity.ordinal) * 2 + 1 })

        for (reward in rews) {
            (reward.logic as IBountyReward).giveReward(reward, player)
        }
    }

    // Awarding

    fun tryCashIn(player: PlayerEntity): Boolean {
        if (info.timeLeftTicks(player.world) <= 0) {
            player.sendMessage(Text.translatable("bountiful.bounty.expired"))
            return false
        }
        return if (hasFinishedObjectives(player)) {
            consumeObjectives(player)
            rewardPlayer(player)
            stack.decrement(stack.maxCount)
            true
        } else {
            player.sendMessage(Text.translatable("bountiful.tooltip.requirements"), false)
            false
        }
    }

    fun checkForCompletionAndAlert(player: PlayerEntity) {
        if (isDone(player)) {
            if (!ping) {
                ping = true
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
            ping = false
        }
    }

    fun genTooltip(isServer: Boolean, type: TooltipType): List<MutableText> {
        if (isServer) {
            return emptyList()
        }
        val player = MinecraftClient.getInstance().player!!
        return buildList {
            add(Text.translatable("bountiful.tooltip.required").formatted(Formatting.GOLD).append(":"))
            addAll(objs.map {
                it.textOnBounty(player, true, progressOf(it))
            })
            add(Text.translatable("bountiful.tooltip.rewards").formatted(Formatting.GOLD).append(":"))
            addAll(rews.map {
                it.textOnBounty(player, false, progressOf(it))
            })

            if (type == TooltipType.ADVANCED && BountifulIO.configData.client.advancedDebugTooltips) {
                add(Text.literal(""))
                add(Text.literal("Bountiful Debug Info:").formatted(Formatting.GOLD))
                add(Text.literal("Taken: ${info.timeTakenSecs(player.world)}, Left: ${info.timeLeftSecs(player.world)}"))
            }
        }
    }

}