package io.ejekta.bountiful.components

import io.ejekta.bountiful.bounty.BountyRarity
import io.ejekta.bountiful.util.fourUBytesToLong
import io.ejekta.bountiful.util.longToFourUBytes
import kotlinx.serialization.Serializable

@Serializable
@JvmRecord
data class BountyPing(val complete: Boolean)

