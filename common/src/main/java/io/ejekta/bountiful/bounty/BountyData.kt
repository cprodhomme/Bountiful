package io.ejekta.bountiful.bounty

import io.ejekta.bountiful.Bountiful
import io.ejekta.bountiful.bounty.types.IBountyObjective
import io.ejekta.bountiful.bounty.types.IBountyReward
import io.ejekta.bountiful.config.JsonFormats
import io.ejekta.bountiful.content.BountifulContent
import io.ejekta.bountiful.messages.OnBountyComplete
import kotlinx.serialization.Serializable
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text

@Suppress("RemoveRedundantQualifierName")
@Serializable
class BountyData {

    val objectives = mutableListOf<BountyDataEntry>()
    val rewards = mutableListOf<BountyDataEntry>()
    private var pingComplete: Boolean = false


    val objectiveTypes: List<IBountyObjective>
        get() = objectives.map { it.logic as IBountyObjective }



    override fun toString(): String {
        return JsonFormats.DataPack.encodeToString(BountyData.serializer(), this)
    }

}