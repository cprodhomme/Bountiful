package io.ejekta.bountiful

import io.ejekta.bountiful.bridge.Bountybridge
import io.ejekta.bountiful.chaos.ChaosMode
import io.ejekta.bountiful.config.BountifulIO
import io.ejekta.bountiful.config.BountifulReloadListener
import io.ejekta.bountiful.content.BountifulCommands
import io.ejekta.bountiful.content.BountifulContent
import io.ejekta.bountiful.content.villager.DecreeTradeFactory
import io.ejekta.kambrik.Kambrik
import io.ejekta.kambrik.internal.registration.KambrikRegistrar
import kotlinx.serialization.json.JsonObject
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.fabricmc.fabric.api.`object`.builder.v1.trade.TradeOfferHelper
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.ResourcePackActivationType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.item.ItemGroups
import net.minecraft.resource.ResourceType
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class BountifulModFabric : ModInitializer {

    init {

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(BountifulReloadListener)
        val ourContainer = FabricLoader.getInstance().getModContainer(Bountiful.ID).get()

        listOf(
            "campanion",
            "charm",
            "croptopia",
            "gofish",
            "techreborn",
            "villager-hats",
            "xtraarrows",
            "numismatic-overhaul"
        ).forEach {
            if (FabricLoader.getInstance().isModLoaded(it)) {
                val modContainer = FabricLoader.getInstance().getModContainer(it).get()
                ResourceManagerHelper.registerBuiltinResourcePack(
                    Identifier(Bountiful.ID, "compat-$it"),
                    ourContainer,
                    Text.literal("${ourContainer.metadata.name} - ${modContainer.metadata.name} Compat"),
                    ResourcePackActivationType.DEFAULT_ENABLED
                )
            }
        }

    }

    override fun onInitialize() {
        Bountiful.LOGGER.info("Common init")
        BountifulIO.loadConfig()
        KambrikRegistrar.doRegistrationsFor(Bountiful.ID)

        Bountybridge.registerServerMessages()
        Bountybridge.registerClientMessages()

        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback(BountifulCommands::register))

        Bountybridge.registerCompostables()

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register { e ->
            e.add(BountifulContent.DECREE_ITEM)
            e.add(BountifulContent.BOARD_ITEM)
        }

        ServerLifecycleEvents.SERVER_STARTING.register(ServerLifecycleEvents.ServerStarting { server ->
            Bountybridge.registerJigsawPieces(server)
        })

        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted { server ->
            if (BountifulIO.configData.chaos.enabled) {
                ChaosMode.inject(server)
            }
        })

        // Increment entity bounties for all players within 12 blocks of the player and all players within 12 blocks of the mob
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(ServerEntityCombatEvents.AfterKilledOtherEntity { world, entity, killedEntity ->
            Bountybridge.handleEntityKills(world, entity, killedEntity)
        })

        TradeOfferHelper.registerWanderingTraderOffers(1) {
            Bountybridge.modifyTradeList(it)
        }

        TradeOfferHelper.registerRebalancedWanderingTraderOffers {
            it.pool(
                Bountiful.id("merchant_trade_offers"), 1, DecreeTradeFactory()
            )
        }

        for ((group, items) in Bountybridge.getItemGroups()) {
            ItemGroupEvents.modifyEntriesEvent(group).register(ItemGroupEvents.ModifyEntries {
                for (item in items) {
                    it.add(item)
                }
            })
        }

        Bountybridge.registerCriterionStuff()
    }
}