package io.ejekta.bountiful.messages

import io.ejekta.bountiful.Bountiful
import io.ejekta.kambrik.message.KambrikMsg
import kotlinx.serialization.Serializable
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.CustomPayload

@Serializable
class ClipboardCopy(val text: String) : KambrikMsg() {
    override fun onClientReceived() {
        Bountiful.LOGGER.info("Copying text to clipboard: $text")
        MinecraftClient.getInstance().keyboard.clipboard = text
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID

    companion object {
        val ID = CustomPayload.id<ClipboardCopy>("clipboard_copy")
    }
}