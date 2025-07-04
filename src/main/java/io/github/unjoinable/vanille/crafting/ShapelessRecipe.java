package io.github.unjoinable.vanille.crafting;

import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shapeless recipe where ingredients can be placed in any order/position.
 * Only requires the correct materials and quantities to match.
 */
public record ShapelessRecipe(
        ItemStack result,
        List<Material> ingredients) implements Recipe {

    public static final Key KEY = Key.key("minecraft:crafting_shapeless");

    /**
     * Checks if the crafting input matches this shapeless recipe.
     * Ingredients can be in any position as long as materials and quantities match.
     *
     * @param input List of 9 ItemStacks from the crafting grid
     * @return true if input contains exactly the required ingredients
     */
    @Override
    public boolean matches(List<ItemStack> input) {
        if (input.size() != 9) {
            throw new IllegalArgumentException("Crafting input must contain exactly 9 slots, got: " + input.size());
        }

        List<Material> inputMaterials = input.stream()
                .filter(item -> !Recipe.isEmpty(item))
                .map(ItemStack::material)
                .toList();

        if (inputMaterials.size() != ingredients.size()) {
            return false;
        }

        return hasSameIngredients(inputMaterials, ingredients);
    }

    /**
     * Efficiently checks if two lists contain the same elements with same frequencies.
     *
     * @param input List of materials from crafting grid
     * @param required List of required materials
     * @return true if lists have identical contents
     */
    private boolean hasSameIngredients(List<Material> input, List<Material> required) {
        Map<Material, Long> inputCounts = input.stream()
                .collect(Collectors.groupingBy(material -> material, Collectors.counting()));

        Map<Material, Long> requiredCounts = required.stream()
                .collect(Collectors.groupingBy(material -> material, Collectors.counting()));

        return inputCounts.equals(requiredCounts);
    }

    @Override
    public ItemStack getResult() {
        return result;
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }
}