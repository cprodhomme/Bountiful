package io.ejekta.bountiful.chaos

import com.example.recipe.RecursiveRecipeParser
import io.ejekta.kambrik.ext.identifier
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.server.MinecraftServer

object ChaosMode {

    // For now, we'll just test clientside. This can easily be moved to server later
    val mc = MinecraftClient.getInstance()

    fun test(server: MinecraftServer) {

        val solver = DepthSolver(server)

        solver.solveRequiredRecipes()

        val goldHoeItem = ItemStack(Items.GOLDEN_HOE)

        solver.solveFor(goldHoeItem, emptyList())
        println("Done with solves!")

        solver.emitTerminators()
    }

}