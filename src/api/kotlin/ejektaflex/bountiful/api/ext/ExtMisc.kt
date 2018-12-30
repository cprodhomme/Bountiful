package ejektaflex.bountiful.api.ext

import ejektaflex.bountiful.api.logic.IWeighted
import net.minecraft.client.Minecraft
import net.minecraft.command.ICommandSender
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.TextComponentString
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

val String.rl: ResourceLocation
    get() = ResourceLocation(substringBefore(":"), substringAfter(":"))

val ResourceLocation.inputStream: InputStream?
    get() = Minecraft.getMinecraft().resourceManager.getResource(this).inputStream

val ResourceLocation.toStringContents: String?
    get() = inputStream?.bufferedReader()?.readText()

fun ICommandSender.sendMessage(str: String) = sendMessage(TextComponentString(str))

fun Int.clampTo(range: IntRange): Int {
    return max(range.first, min(this, range.last))
}

fun Double.clampTo(low: Double, high: Double): Double {
    return max(low, min(this, high))
}

fun Long.clampTo(range: LongRange): Long {
    return max(range.first, min(this, range.last))
}

val <T : IWeighted> List<T>.weightedRandom: T
    get() {
        val sum = this.sumBy { it.weight }
        var point = (0..sum).random()
        for (item in this) {
            if (point <= item.weight) {
                return item
            }
            point -= item.weight
        }
        return last()
    }