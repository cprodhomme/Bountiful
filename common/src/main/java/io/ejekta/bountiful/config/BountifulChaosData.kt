package io.ejekta.bountiful.config

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.minecraft.util.Identifier

@Serializable
class BountifulChaosData(
    val required: MutableMap<@Contextual Identifier, String?> = mutableMapOf(),
    val optional: MutableMap<@Contextual Identifier, String?> = mutableMapOf()
)