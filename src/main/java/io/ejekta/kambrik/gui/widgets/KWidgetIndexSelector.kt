package io.ejekta.kambrik.gui.widgets

import kotlin.math.min
import kotlin.math.roundToInt

interface KWidgetIndexSelector {
    fun getIndices(total: Int, pick: Int): IntRange {
        require(pick > 0) { "You must pick at least one index!" }
        return 0 until min(total, pick)
    }
}