package io.ejekta.bountiful.chaos

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.minecraft.util.Identifier

@Serializable
class BountifulChaosInfo(
    var deps: MutableMap<@Contextual Identifier, Int> = mutableMapOf(),
    var unsolved: Int = 0,
    var redundant: MutableList<@Contextual Identifier> = mutableListOf()
)