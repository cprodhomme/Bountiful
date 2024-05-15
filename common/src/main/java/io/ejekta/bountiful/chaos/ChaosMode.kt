package io.ejekta.bountiful.chaos

import com.example.recipe.RecursiveRecipeParser
import io.ejekta.bountiful.config.BountifulChaosData
import io.ejekta.bountiful.config.BountifulIO
import io.ejekta.bountiful.config.BountifulIO.rootFolder
import io.ejekta.bountiful.config.JsonFormats
import io.ejekta.kambrik.ext.identifier
import io.ejekta.kambrikx.file.KambrikConfigFile
import io.ejekta.kambrikx.file.KambrikParseFailMode
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
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

    fun initChaosData() {
        chaosData = chaosFile.read()
        chaosFile.write(chaosData)
    }

    fun test(server: MinecraftServer) {

        println("Chaos data:")
        println(chaosData.required)
        println(chaosData.optional)

        val solver = DepthSolver(server)

        solver.solveRequiredRecipes()

        val goldHoeItem = ItemStack(Items.GOLDEN_HOE)

        solver.solveFor(goldHoeItem, emptyList())
        println("Done with solves!")

        solver.emitTerminators(chaosData)

        solver.emitOptionals(chaosData)

        chaosFile.write(chaosData)
    }



}