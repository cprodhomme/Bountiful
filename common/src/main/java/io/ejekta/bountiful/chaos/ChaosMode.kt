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

    fun getRecipes() {

        val rm = mc.server?.recipeManager ?: return
        val re = mc.server?.registryManager ?: return
        val recipes = rm.values()
        val solver = ChaosSolver(rm, re)
        //solver.computeTree()

    }

    fun test(server: MinecraftServer) {

        val solver = DepthSolver(server)
        for (item in server.registryManager.get(Registries.ITEM.key)) {
            //println("Solving for: $item")
            solver.solveFor(ItemStack(item), emptyList())
        }

        val goldHoeItem = ItemStack(Items.GOLDEN_HOE)

        solver.solveFor(goldHoeItem, emptyList())
        println("Done with solves!")

        solver.emitTerminators()
    }

}