package io.github.unjoinable.vanille.crafting;

import net.kyori.adventure.key.Keyed;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;

/**
 * Recipe interface for Minecraft crafting system.
 *
 * <p>Recipes define how items can be combined to create other items.
 * The crafting system supports different types of recipes:
 *
 * <ul>
 *   <li><strong>Shaped Recipes</strong> - Items must be placed in specific positions (e.g., sword, pickaxe)</li>
 *   <li><strong>Shapeless Recipes</strong> - Items can be placed anywhere (e.g., dye mixing)</li>
 * </ul>
 *
 * <p>The input is provided as a List of ItemStacks representing the crafting grid
 * in row-major order (left-to-right, top-to-bottom). For a 3x3 crafting table:
 * <pre>
 * [0] [1] [2]
 * [3] [4] [5]
 * [6] [7] [8]
 * </pre>
 *
 * <p>Empty slots are represented by ItemStack.AIR or null items.
 *
 * @see ShapedRecipe
 * @see ShapelessRecipe
 */
public interface Recipe extends Keyed {

    /**
     * Checks if the given input matches this recipe.
     *
     * @param input the crafting grid contents as a list of ItemStacks
     * @return true if the input matches this recipe's pattern
     */
    boolean matches(List<ItemStack> input);

    /**
     * Gets the result item that this recipe produces.
     *
     * @return the resulting ItemStack when this recipe is crafted
     */
    ItemStack getResult();

    /**
     * Checks if an ItemStack is considered empty for recipe matching.
     *
     * @param item the ItemStack to check
     * @return true if the item is null, AIR, or has amount 0
     */
    static boolean isEmpty(ItemStack item) {
        return item == null || item.material() == Material.AIR || item.amount() == 0;
    }
}