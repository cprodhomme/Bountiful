package io.ejekta.bountiful.messages

import io.ejekta.bountiful.content.BountifulContent
import io.ejekta.kambrik.message.KambrikMsg
import kotlinx.serialization.Serializable
import net.minecraft.network.packet.CustomPayload

@Serializable
class DecreePlaced : KambrikMsg() {
    override fun getId(): CustomPayload.Id<out CustomPayload> = ID

    override fun onServerReceived(ctx: MsgContext) {
        println("Decree placed by: ${ctx.player}")
        BountifulContent.Triggers.DECREE_PLACED.trigger(ctx.player)
    }

    companion object {
        val ID = CustomPayload.id<DecreePlaced>("decree_placed")
    }
}