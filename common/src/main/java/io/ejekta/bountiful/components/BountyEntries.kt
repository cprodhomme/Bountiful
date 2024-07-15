package io.ejekta.bountiful.components

import kotlinx.serialization.Serializable

@Serializable @JvmRecord
data class BountyEntries(val entries: List<BountyDataEntry>)