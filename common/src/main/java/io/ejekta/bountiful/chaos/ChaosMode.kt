package io.ejekta.bountiful.chaos

import com.example.recipe.RecursiveRecipeParser
import io.ejekta.kambrik.ext.identifier
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
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

    var rep: RecursiveRecipeParser? = null

    fun test(server: MinecraftServer) {
        val goldHoeItem = ItemStack(Items.GOLDEN_HOE)
        if (rep == null) {
            rep = RecursiveRecipeParser(server).apply { query(goldHoeItem) }
        }

        with(rep!!) {

            val solves = getSolveables(goldHoeItem)

            println("Solves:")
            println(solves)


        }


    }

}