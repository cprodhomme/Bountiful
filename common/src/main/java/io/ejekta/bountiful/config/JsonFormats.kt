package io.ejekta.bountiful.config

import codec
import io.ejekta.kambrik.Kambrik
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

@OptIn(ExperimentalSerializationApi::class)
object JsonFormats {

    val MojangSerializer = SerializersModule {
        include(Kambrik.Serial.DefaultSerializers)
        codec(Identifier.CODEC)
        codec(NbtCompound.CODEC)
        codec(Vec3d.CODEC)
        codec(BlockPos.CODEC)
    }

    val DataPack = Json {
        serializersModule = MojangSerializer
        prettyPrint = true
        allowTrailingComma = true
    }
    val BlockEntity = Json {
        serializersModule = MojangSerializer
    }
    val Hand = Json {
        serializersModule = MojangSerializer
        prettyPrint = true
    }
    val Config = Json {
        serializersModule = MojangSerializer
        encodeDefaults = true
        prettyPrint = true
        allowTrailingComma = true
    }
}