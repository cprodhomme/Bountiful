package io.ejekta.bountiful.bridge

import io.ejekta.bountiful.Bountiful
import io.ejekta.bountiful.bounty.types.BountyTypeRegistry
import io.ejekta.bountiful.config.BountifulIO
import io.ejekta.bountiful.content.BountifulContent
import io.ejekta.bountiful.content.villager.DecreeTradeFactory
import io.ejekta.bountiful.messages.*
import io.ejekta.bountiful.util.iterateBountyStacks
import io.ejekta.kambrik.Kambrik
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.minecraft.advancement.criterion.EnterBlockCriterion
import net.minecraft.advancement.criterion.TickCriterion
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.passive.TameableEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemGroups
import net.minecraft.registry.RegistryKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.village.TradeOffers

interface BountifulSharedApi {

    fun isModLoaded(id: String): Boolean

    fun registerItemDynamicTextures() {
        ModelPredicateProviderRegistry.register(
            BountifulContent.BOUNTY_ITEM,
            Bountiful.id("rarity")
        ) { stack, clientWorld, livingEntity, seed ->
            (stack[BountifulContent.BOUNTY_INFO]?.rarity?.ordinal?.toFloat() ?: 0f) / 10f
        }

        ModelPredicateProviderRegistry.register(
            BountifulContent.DECREE_ITEM,
            Bountiful.id("status")
        ) { stack, clientWorld, livingEntity, seed ->
            val data = stack[BountifulContent.DECREE_DATA]
            if ((data?.ids ?: emptySet()).isNotEmpty()) 1f else 0f
        }
    }

    fun registerServerMessages() {
        Kambrik.Message.registerServerMessage(SelectBounty.serializer(), SelectBounty.ID)
        Kambrik.Message.registerServerMessage(ServerPlayerStatus.serializer(), ServerPlayerStatus.ID)
    }

    fun registerClientMessages() {
        Kambrik.Message.registerClientMessage(ClipboardCopy.serializer(), ClipboardCopy.ID)
        Kambrik.Message.registerClientMessage(OnBountyComplete.serializer(), OnBountyComplete.ID)
        Kambrik.Message.registerClientMessage(UpdateCriteriaObjective.serializer(), UpdateCriteriaObjective.ID)
        Kambrik.Message.registerClientMessage(ClientPlayerStatus.serializer(), ClientPlayerStatus.ID)
    }

    fun registerJigsawPieces(server: MinecraftServer) {
        listOf("plains", "savanna", "snowy", "taiga", "desert").forEach { villageType ->
            Bountiful.LOGGER.info("Registering Bounty Board Jigsaw Piece for Village Type: $villageType")
            Kambrik.Structure.addToStructurePool(
                server,
                Identifier.of("bountiful:village/common/bounty_gazebo"),
                Identifier.of("minecraft:village/$villageType/houses"),
                Identifier.of("bountiful:$villageType"),
                BountifulIO.configData.board.villageGenFrequency
            )
        }
    }

    fun handleEntityKills(world: ServerWorld, entity: Entity, killedEntity: LivingEntity) {
        if (entity is LivingEntity) {
            val playerList = mutableSetOf<ServerPlayerEntity>()
            playerList.addAll(world.getPlayers { it.distanceTo(entity) < 12f || it.distanceTo(killedEntity) < 12f })
            (entity as? ServerPlayerEntity)?.let { playerList.add(it) }

            if (entity is TameableEntity) {
                val owner = entity.owner as? ServerPlayerEntity
                owner?.let {
                    playerList.add(it)
                }
            }

            (killedEntity.attacker as? ServerPlayerEntity)?.let { playerList.add(it) }
            (killedEntity.attacking as? ServerPlayerEntity)?.let { playerList.add(it) }

            playerList.forEach {
                (entity as? TameableEntity)?.let { tameable ->
                    if (tameable.owner ==  it) {
                        BountifulContent.Triggers.FETCH_QUEST.trigger(it)
                    }
                }
                BountyTypeRegistry.ENTITY.incrementEntityBounties(it, killedEntity)
            }
        }
    }

    fun getItemGroups(): Map<RegistryKey<ItemGroup>, List<() -> Item>> {
        return mapOf(
            ItemGroups.FUNCTIONAL to listOf({ BountifulContent.BOARD_ITEM }, { BountifulContent.DECREE_ITEM }),
        )
    }

    fun modifyTradeList(list: MutableList<TradeOffers.Factory>) {
        list.add(DecreeTradeFactory())
    }

    // Update Criterion bounties
    fun registerCriterionStuff() {
        Kambrik.Criterion.subscribe { player, criterion, predicate ->
            if (criterion !is TickCriterion && criterion !is EnterBlockCriterion) {
                player.iterateBountyStacks {

                    val triggerObjs = objs.filter { it.critConditions != null }.takeIf { it.isNotEmpty() } ?: emptyList()

                    for (obj in triggerObjs) {

                        val result = Kambrik.Criterion.testAgainst(
                            criterion,
                            Kambrik.Criterion.createCriterionConditionsFromJson(
                                buildJsonObject {
                                    put("trigger", obj.content)
                                    put("conditions", obj.critConditions ?: buildJsonObject {  })
                                }
                            ) ?: continue,
                            predicate
                        )

                        if (result) {
                            advance(obj)
                            UpdateCriteriaObjective(
                                player.inventory.indexOf(stack), obj.id).sendToClient(player)
                        }
                    }

                    checkForCompletionAndAlert(player)
                }
            }
        }
    }

    fun registerCompostables()

    fun getClassLoader(): ClassLoader {
        return Bountybridge.Companion::class.java.classLoader
    }

}