package io.ejekta.bountiful.chaos

import io.ejekta.kambrik.ext.identifier
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeEntry
import net.minecraft.recipe.RecipeManager
import net.minecraft.registry.Registries
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import kotlin.jvm.optionals.getOrNull

class DepthSolver(val server: MinecraftServer, val data: BountifulChaosData) {

    private val recipeManager: RecipeManager = server.recipeManager
    private val regManager = server.registryManager

    private val terminators = mutableSetOf<Identifier>()
    private val deps = mutableMapOf<Identifier, MutableSet<Identifier>>()

    // Final cost map
    private val costMap = mutableMapOf<Item, Double>()

    // Populate cost map from config
    init {
        for (itemId in data.required.filter { it.value != null }.keys) {
            val item = server.registryManager.get(Registries.ITEM.key).getOrEmpty(itemId).getOrNull()
            item?.let { costMap[it] = data.required[itemId]!! }
        }
    }

    private val ItemStack.recipes: List<RecipeEntry<*>>
        get() = stackLookup[this.item] ?: emptyList()

    private val RecipeEntry<*>.inItemSets: List<Set<ItemStack>>
        get() = this.value.ingredients.map { it.matchingStacks.toSet() }

    private val stackLookup = recipeManager.values().toList().groupBy { it.value.getResult(regManager).item }

    // Attempts to solve for stack cost.
    fun solveFor(stack: ItemStack, path: List<ItemStack>): Double? {
        val padding = (path.size + 1) * 2
        //println("Solving: $stack".padStart(padding))
        val recipes = stack.recipes

        // If no recipe exists, it is a terminator. In the future, pull from the terminator pool list. For now, return a dummy value
        if (recipes.isEmpty()) {
            terminators.add(stack.identifier)
            // Add dependency on said item
            for (pathItem in path) {
                deps.getOrPut(stack.identifier) { mutableSetOf() }.add(pathItem.identifier)
            }
            return null
        }

        val recipeCosts = mutableListOf<Double>()

        for (recipe in recipes) {
            val inputCounts = recipe.inItemSets.flatten().groupBy { it.item }.map { it.key to it.value.sumOf { stack -> stack.count } }.toMap()

            var ingredientRunningCost = 0.0

            val inputSets = recipe.inItemSets

            var numUnsolvedIngredients = 0

            for (optionSet in inputSets) {
                // Currently grabs the first solved and adds the cost; This assumes tag ingredients all have the same cost;
                // It might be useful to average them and not just grab the first one in the future!
                for (option in optionSet) {
                    if (option.item in path.map { it.item }) { // Cyclic dependency!
                        continue
                    }
                    if (path.size > 24) { // Avoid too much recursion
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

            // If a recipe makes 3 of something, the actual cost is only 1/3 as much
            ingredientRunningCost /= recipe.value.getResult(regManager).count

            if (numUnsolvedIngredients > 0) {
                //println("Could not solve for ${stack.identifier}".padStart(padding))
            } else {
                //println("Resolved all ingredients for: ${stack.identifier}".padStart(padding))
                recipeCosts.add(ingredientRunningCost)
                //println("Cost was: $ingredientRunningCost".padStart(padding))
            }

        }

        val finalCost = recipeCosts.minOrNull()

        // Of all calculated recipe costs, find the minimum
        finalCost?.let {
            costMap[stack.item] = it
        }

        return finalCost
    }

    fun solveRequiredRecipes() {
        var unsolved = 0
        for (item in regManager.get(Registries.ITEM.key)) {
            // If there exists a recipe for it, solve it
            if (item in stackLookup.keys) {
                val didSolve = solveFor(ItemStack(item), emptyList())
                if (didSolve == null) {
                    unsolved += 1
                }
            } else {
                unsolved += 1
            }
        }
        data.unsolved = unsolved
    }

    fun syncConfig() {
        println("Terminators:")
        // Insert terminators into required prop
        for (line in terminators.sorted()) {
            data.required[line] = costMap[regManager.get(Registries.ITEM.key).getOrEmpty(line).getOrNull()]
            println(line)
        }
        // Reset JSON file ordering (this is a bit hacky)
        data.required = data.required.toList().toMap().toMutableMap()
        // Update dependency numbering
        data.deps = deps.map { it.key to it.value.size }.sortedBy { -it.second }.toMap().toMutableMap()
    }

    fun showResults() {
        for (item in regManager.get(Registries.ITEM.key).sortedBy { it.identifier }) {
            println("Item: ${item.identifier.toString().padEnd(20)} - ${costMap[item]}")
        }
    }

}