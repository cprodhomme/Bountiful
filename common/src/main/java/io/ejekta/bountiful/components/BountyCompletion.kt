package io.ejekta.bountiful.components

import kotlinx.serialization.Serializable

@OptIn(ExperimentalUnsignedTypes::class)
@Serializable
@JvmRecord
data class BountyCompletion(val bits: UByteArray) {
    @OptIn(ExperimentalUnsignedTypes::class)
    fun modified(func: UByteArray.() -> Unit): BountyCompletion {
        return BountyCompletion(bits.apply(func))
    }
}

