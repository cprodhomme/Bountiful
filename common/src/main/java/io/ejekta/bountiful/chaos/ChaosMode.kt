package io.ejekta.bountiful.chaos

import io.ejekta.bountiful.config.BountifulIO.rootFolder
import io.ejekta.bountiful.config.JsonFormats
import io.ejekta.kambrikx.file.KambrikConfigFile
import io.ejekta.kambrikx.file.KambrikParseFailMode
import net.minecraft.server.MinecraftServer

object ChaosMode {


    private val chaosFile = KambrikConfigFile(
        rootFolder,
        "bountiful-chaos.json",
        JsonFormats.Config,
        KambrikParseFailMode.LEAVE,
        BountifulChaosData.serializer()
    ) { BountifulChaosData() }

    private val chaosFileInfo = KambrikConfigFile(
        rootFolder,
        "bountiful-chaos-info.json",
        JsonFormats.Config,
        KambrikParseFailMode.LEAVE,
        BountifulChaosInfo.serializer()
    ) { BountifulChaosInfo() }

    var chaosData = chaosFile.read()
    var chaosInfo = chaosFileInfo.read()

    fun test(server: MinecraftServer) {
        chaosData = chaosFile.read()
        chaosInfo = chaosFileInfo.read()

        val solver = DepthSolver(server, chaosData, chaosInfo)
        solver.solveRequiredRecipes()

        println("Done with solves!")

        solver.syncConfig()

        chaosFile.write(chaosData)
        chaosFileInfo.write(chaosInfo)

        solver.showResults()
    }

}