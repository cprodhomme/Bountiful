package io.ejekta.bountiful.bounty.types.builtin

import io.ejekta.bountiful.bounty.types.IBountyObjective
import io.ejekta.bountiful.components.BountyDataEntry
import io.ejekta.bountiful.data.PoolEntry
import io.ejekta.bountiful.util.iterateBountyStacks
import io.ejekta.kambrik.ext.identifier
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.Registries
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier


class BountyTypeEntity : IBountyObjective {

    override val id: Identifier = Identifier.of("entity")

    override fun isValid(entry: PoolEntry, server: MinecraftServer): Boolean {
        val id = getEntityType(Identifier.of(entry.content)).identifier
        return id == Identifier.of(entry.content)
    }

    override fun textOnBounty(entry: BountyDataEntry, isObj: Boolean, player: PlayerEntity, current: Int): MutableText {
        val progress = getProgress(entry, player, current)
        return when (isObj) {
            true -> Text.literal("Kill ").append(
                getEntityType(entry).name.copy()
            ).formatted(progress.color).append(
                progress.neededText.colored(Formatting.WHITE)
            )
            false -> Text.literal("ERR: Cannot have an entity (${entry.content}) as a reward.")
        }
    }

    override fun textOnBoardSidebar(entry: BountyDataEntry, player: PlayerEntity): List<Text> {
        return listOf(getEntityType(entry).name)
    }

    fun incrementEntityBounties(playerEntity: ServerPlayerEntity, killedEntity: LivingEntity) {
        // The player cannot kill themselves (arrow, potion, etc) to complete a bounty
        if (playerEntity == killedEntity) {
            return
        }
        playerEntity.iterateBountyStacks {
            val entityObjs = objs.filter { it.logicId == this@BountyTypeEntity.id }
            if (entityObjs.isNotEmpty()) {
                var changes = false
                for (obj in entityObjs) {
                    if (obj.content == killedEntity.type.identifier.toString()) {
                        advance(obj)
                        changes = true
                    }
                }
                if (changes) {
                    checkForCompletionAndAlert(playerEntity)
                }
            }
        }
    }


    companion object {
        fun getEntityType(entry: BountyDataEntry): EntityType<*> {
            return getEntityType(Identifier.of(entry.content))
        }

        fun getEntityType(id: Identifier): EntityType<*> {
            return Registries.ENTITY_TYPE.get(id)
        }
    }

}