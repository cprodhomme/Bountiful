package io.ejekta.bountiful.chaos

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.minecraft.util.Identifier

@Serializable
class BountifulChaosData(
    var required: MutableMap<@Contextual Identifier, Double?> = mutableMapOf(),
    var deps: MutableMap<@Contextual Identifier, Int> = mutableMapOf(),
    var unsolved: Int = 0
)