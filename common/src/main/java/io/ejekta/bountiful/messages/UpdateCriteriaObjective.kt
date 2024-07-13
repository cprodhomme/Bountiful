package io.ejekta.bountiful.messages

import io.ejekta.bountiful.content.item.BountyItem
import io.ejekta.bountiful.util.ctx
import io.ejekta.kambrik.message.KambrikMsg
import kotlinx.serialization.Serializable
import net.minecraft.network.packet.CustomPayload

@Serializable
class UpdateCriteriaObjective(val slot: Int, val objIndex: Int) : KambrikMsg() {
    override fun onClientReceived() {
        println("Client received update bounty tooltip update with slot number: $slot")
        val player = ctx.player

        if (player == null) {
            println("Player was null, can't update the tooltip!!")
        } else {
            val stack = player.inventory.getStack(slot)

            if (stack.item is BountyItem) {

                // TODO update bounty objectives on criteria complete
//                BountyData.edit(stack) {
//                    objectives[objIndex].current += 1
//                }

            }
        }
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID

    companion object {
        val ID = CustomPayload.id<UpdateCriteriaObjective>("update_bounty_criteria")
    }
}