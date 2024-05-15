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

    var chaosData = chaosFile.read()

    fun test(server: MinecraftServer) {
        chaosData = chaosFile.read()

        println("Chaos data:")
        println(chaosData.required)
        println(chaosData.deps)

        val solver = DepthSolver(server, chaosData)
        solver.solveRequiredRecipes()

        println("Done with solves!")

        solver.syncConfig()

        chaosFile.write(chaosData)

        solver.showResults()
    }

}