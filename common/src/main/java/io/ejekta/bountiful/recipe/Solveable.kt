package com.example.recipe

import com.example.recipe.RecursiveRecipeParser.Companion.getStackOrPut
import com.example.recipe.RecursiveRecipeParser.Companion.stackKey
import io.ejekta.kambrik.ext.identifier
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeType

class Solveable(val ingredients: List<Ingredient>, val makes: Int, val type: RecipeType<*>) {
    fun solve(parser: RecursiveRecipeParser, seen: MutableSet<ItemStack>, deep: Int): Int? {
        val routes = ingredients.map { ingr ->
            val staks = ingr.matchingStacks.toList().filter { parser.visited.stackKey(it) !in seen }

            if (staks.isEmpty()) {
                return null
            }

            staks.mapNotNull { stack ->
                if (stack !in parser.planned) {
                    val calc = parser.plan(stack, seen.toMutableSet(), deep + 1)
                    calc?.let { parser.planned[stack] = it }
                }
                parser.planned[stack]
            }.minOrNull()
        }.filterNotNull()

        //println("\t".repeat(deep) + "routes (${routes.size})")
        return if (routes.isEmpty()) {
            null
        } else {
            routes.sum()
        }
    }

    override fun toString(): String {
        return "Solveable(ingredients=${ingredients.map { 
            ingredient -> ingredient.matchingStacks.map { it.identifier }.joinToString("/") 
        }}, makes=$makes, type=$type)"
    }

}