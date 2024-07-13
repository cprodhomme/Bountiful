package io.ejekta.bountiful.bounty.types.builtin

import io.ejekta.bountiful.bounty.types.IBountyReward
import io.ejekta.bountiful.components.BountyDataEntry
import io.ejekta.bountiful.data.PoolEntry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import kotlin.random.Random


class BountyTypeCommand : IBountyReward {

    override val id: Identifier = Identifier.of("command")

    override fun isValid(entry: PoolEntry, server: MinecraftServer): Boolean {
        val parsed = server.commandManager.dispatcher.parse(entry.content, server.commandSource)
        return parsed.exceptions.isEmpty()
    }

    override fun textSummary(entry: BountyDataEntry, isObj: Boolean, player: PlayerEntity): MutableText {
        return getDescription(entry)
    }

    override fun textBoard(entry: BountyDataEntry, player: PlayerEntity): List<Text> {
        return listOf(getDescription(entry))
    }

    override fun giveReward(entry: BountyDataEntry, player: PlayerEntity) {
        val server = player.server ?: return
        val replacedCmd = entry.content
            .replace("%BOUNTY_AMOUNT%", entry.amount.toString())
            .replace("%PLAYER_NAME%", player.nameForScoreboard)
            .replace("%PLAYER_NAME_RANDOM", server.playerNames.random())
            .replace("%PLAYER_POSITION%", "${player.pos.x} ${player.pos.y} ${player.pos.z}")
            // Should not NPE since capture group would fail first
            .replace(Regex("%RANDOM_INT\\((?<low>-*\\d+),\\s*(?<high>-*\\d+)\\)%")) {
                result -> Random.nextInt(
                    result.groups["low"]!!.value.toInt(),
                    result.groups["high"]!!.value.toInt() + 1 // exclusive until, needs increase by 1
                ).toString()
            }
        server.commandManager.executeWithPrefix(server.commandSource, replacedCmd)
    }

}