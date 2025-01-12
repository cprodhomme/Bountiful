package io.ejekta.bountiful.content.villager

import com.google.common.collect.ImmutableMap
import io.ejekta.bountiful.content.BountifulContent
import net.minecraft.entity.ai.brain.Activity
import net.minecraft.entity.ai.brain.MemoryModuleState
import net.minecraft.entity.ai.brain.task.LookTargetUtil
import net.minecraft.entity.ai.brain.task.MultiTickTask
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.server.world.ServerWorld
import kotlin.jvm.optionals.getOrNull

class WalkToBoardTask(val speed: Float) :
    MultiTickTask<VillagerEntity?>(
        ImmutableMap.of(
            BountifulContent.MEM_MODULE_NEAREST_BOARD,
            MemoryModuleState.VALUE_PRESENT
        ), 1200
    ) {
    override fun shouldRun(serverWorld: ServerWorld, villagerEntity: VillagerEntity?): Boolean {
        return villagerEntity?.brain?.firstPossibleNonCoreActivity?.map { activity: Activity -> activity === Activity.IDLE || activity === Activity.WORK || activity === Activity.PLAY }
            ?.orElse(true) ?: true
    }

    override fun shouldKeepRunning(serverWorld: ServerWorld?, entity: VillagerEntity?, l: Long): Boolean {
        val goalSpot = entity?.brain?.getOptionalMemory(BountifulContent.MEM_MODULE_NEAREST_BOARD)?.getOrNull()

        goalSpot?.let { globalPos ->
            val dist = entity.blockPos.toCenterPos().distanceTo(globalPos.pos.toCenterPos())
            if (dist < 1.75) {
                println("Close enough")
                return false
            }
        }

        return true
    }

    override fun keepRunning(serverWorld: ServerWorld?, entity: VillagerEntity?, l: Long) {
        if (serverWorld != null && entity != null) {
            val memPos = entity.brain.getOptionalRegisteredMemory(BountifulContent.MEM_MODULE_NEAREST_BOARD).getOrNull()?.pos
            memPos?.let {
                LookTargetUtil.walkTowards(
                    entity, it,
                    speed, 1
                )
            }
        }
    }

    override fun finishRunning(serverWorld: ServerWorld?, entity: VillagerEntity?, l: Long) {
        val goalSpot = entity?.brain?.getOptionalMemory(BountifulContent.MEM_MODULE_NEAREST_BOARD)?.getOrNull()

        if (serverWorld != null && entity != null) {
            goalSpot?.let {
                val boardEntity = serverWorld.getBlockEntity(it.pos, BountifulContent.BOARD_ENTITY).getOrNull()
                boardEntity?.handleVillagerVisit(entity)
            }
            entity.brain.forget(BountifulContent.MEM_MODULE_NEAREST_BOARD)
        }
    }




    companion object {
        private const val RUN_TIME = 1200

    }
}
