package io.ejekta.bountiful.messages

import io.ejekta.bountiful.content.gui.BoardScreenHandler
import io.ejekta.kambrik.message.KambrikMsg
import kotlinx.serialization.Serializable
import net.minecraft.network.packet.CustomPayload

@Serializable
class SelectBounty(private val index: Int, private val uuidString: String) : KambrikMsg() {
    override fun onServerReceived(ctx: MsgContext) {
        val handler = ctx.player.server.playerManager.playerList.firstOrNull {
            it.uuidAsString == uuidString
        }?.currentScreenHandler as? BoardScreenHandler ?: return
        handler.inventory.select(index)
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID

    companion object {
        val ID = CustomPayload.id<SelectBounty>("select_bounty")
    }
}