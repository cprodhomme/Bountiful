package io.ejekta.bountiful.chaos

import io.ejekta.kambrik.ext.identifier
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeEntry
import net.minecraft.recipe.RecipeManager
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.server.MinecraftServer

class DepthSolver(server: MinecraftServer) {

    private val recipeManager: RecipeManager = server.recipeManager
    private val regManager = server.registryManager

    val terminators = mutableSetOf<String>()

    private val recipeMap = recipeManager.values().toList().associateBy { it.id }

    // Final cost map
    val costMap = mutableMapOf<Item, Double>()

    val rawDepMap = mutableMapOf<Item, MutableSet<Item>>()

    fun submitFinalCost(item: Item, cost: Double) {
        rawDepMap.remove(item)
        costMap[item] = cost
    }

    val ItemStack.recipes: List<RecipeEntry<*>>
        get() = stackLookup[this.item] ?: emptyList()

    val RecipeEntry<*>.result: ItemStack
        get() = this.value.getResult(regManager)

    val RecipeEntry<*>.inItemSets: List<Set<ItemStack>>
        get() = this.value.ingredients.map { it.matchingStacks.toSet() }


    val stackLookup = recipeManager.values().toList().groupBy { it.value.getResult(regManager).item }

    val recipeLookup = recipeManager.values().toList().associateBy { it.value.getResult(regManager).item }

    // Attempts to solve for stack cost.
    fun solveFor(stack: ItemStack, path: List<ItemStack>): Double? {
        val padding = (path.size + 1) * 2
        //println("Solving: $stack".padStart(padding))
        val recipes = stack.recipes

        // If no recipe exists, it is a terminator. In the future, pull from the terminator pool list. For now, return a dummy value
        if (recipes.isEmpty()) {
            terminators.add(stack.identifier.toString())
            return null
        }

        for (recipe in recipes) {
            val inputCounts = recipe.inItemSets.flatten().groupBy { it.item }.map { it.key to it.value.sumOf { stack -> stack.count } }.toMap()

            var ingredientRunningCost = 0.0

            val inputSets = recipe.inItemSets

            var numUnsolvedIngredients = 0

            for (optionSet in inputSets) {
                // Currently grabs the first solved and adds the cost; This assumes tag ingredients all have the same cost;
                // It might be useful to average them and not just grab the first one in the future!
                for (option in optionSet) {
                    if (option.item in path.map { it.item }) {
                        // Cyclic dependency!
                        continue
                    }
                    if (option.item in costMap) { // Already calculated cost, simple O(1) lookup for worth
                        // Add the cost of the item times the amount needed (in that slot)
                        ingredientRunningCost += costMap[option.item]!! * option.count
                        numUnsolvedIngredients -= 1
                        break
                    } else {
                        // No calculated cost. Can we iteratively find one?
                        val solvedCost = solveFor(option, path + stack)
                        if (solvedCost != null) {
                            ingredientRunningCost += solvedCost * option.count
                            numUnsolvedIngredients -= 1
                            break
                        }
                    }
                }
            }

            if (numUnsolvedIngredients > 0) {
                //println("Could not solve for ${stack.identifier}".padStart(padding))
            } else {
                //println("Resolved all ingredients for: ${stack.identifier}".padStart(padding))
                submitFinalCost(stack.item, ingredientRunningCost)
                //println("Cost was: $ingredientRunningCost".padStart(padding))
            }

        }

        return null
    }

    fun emitTerminators() {
        println("Terminators:")
        for (line in terminators.sorted()) {
            println(line)
        }
    }

}