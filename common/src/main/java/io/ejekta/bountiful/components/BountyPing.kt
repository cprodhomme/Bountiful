package io.ejekta.bountiful.components

import kotlinx.serialization.Serializable

@Serializable
@JvmRecord
data class BountyPing(val complete: Boolean)

