package io.ejekta.bountiful.messages

import io.ejekta.bountiful.content.BountifulContent
import io.ejekta.kambrik.message.KambrikMsg
import kotlinx.serialization.Serializable
import net.minecraft.network.packet.CustomPayload

@Serializable
class ServerPlayerStatus(private val statusType: Type) : KambrikMsg() {

    override fun onServerReceived(ctx: MsgContext) {
        statusType.msgFunc(ctx)
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID

    companion object {
        val ID = CustomPayload.id<ServerPlayerStatus>("server_player_status")
    }

    enum class Type(val msgFunc: MsgContext.() -> Unit) {
        DECREE_PLACED({
            println("Decree placed by: $player")

            // Do logic if a player placed all decrees on the board
            //player.currentBoardInteracting?.checkUserPlacedAllDecrees(player)

            BountifulContent.Triggers.DECREE_PLACED.trigger(player)
        }),
        BOUNTY_TAKEN({
            println("Incrementing bounty taken stat!")
            player.incrementStat(BountifulContent.CustomStats.BOUNTIES_TAKEN)
        })
        ;

        fun sendToServer() {
            println("Sending $this to server..")
            ServerPlayerStatus(this).sendToServer()
        }
    }
}