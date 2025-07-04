package io.github.unjoinable.vanille.crafting;

import io.github.unjoinable.vanille.utils.Matrix;
import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Shaped recipe that requires items in specific positions.
 * Supports variable pattern sizes, positioning anywhere in 3x3 grid, and optional mirroring.
 */
public record ShapedRecipe(
        ItemStack result,
        Map<Character, Material> ingredients,
        Matrix<Character> pattern,
        boolean mirrored) implements Recipe {

    public static final Key KEY = Key.key("minecraft:crafting_shaped");
    private static final int GRID_SIZE = 3;

    /**
     * Checks if the given crafting input matches this recipe.
     *
     * <p>The algorithm tries to match the pattern at every possible position in the 3x3 grid,
     * checking both normal and mirrored orientations if mirroring is enabled.
     *
     * @param input List of 9 ItemStacks representing the 3x3 crafting grid (row-major order)
     * @return true if the input matches this recipe pattern
     * @throws IllegalArgumentException if input doesn't contain exactly 9 items
     * @throws IllegalStateException if pattern is null or contains undefined characters
     */
    @Override
    public boolean matches(List<ItemStack> input) {
        validateInput(input);
        Matrix<ItemStack> grid = convertToMatrix(input);

        for (int offsetY = 0; offsetY <= GRID_SIZE - pattern.height; offsetY++) {
            for (int offsetX = 0; offsetX <= GRID_SIZE - pattern.width; offsetX++) {
                if (matchesAtPosition(grid, offsetX, offsetY, false) ||
                        (mirrored && matchesAtPosition(grid, offsetX, offsetY, true))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Validates that the crafting input is properly formatted.
     *
     * @param input The crafting input to validate
     * @throws IllegalArgumentException if input is invalid
     * @throws IllegalStateException if pattern is null
     */
    private void validateInput(List<ItemStack> input) {
        if (input.size() != 9) {
            throw new IllegalArgumentException("Crafting input must contain exactly 9 slots, got: " + input.size());
        }
        if (pattern == null) {
            throw new IllegalStateException("Recipe pattern cannot be null");
        }
    }

    /**
     * Converts a flat list of items into a 3x3 matrix representation.
     *
     * @param input List of 9 items in row-major order
     * @return 3x3 matrix of ItemStacks
     */
    private Matrix<ItemStack> convertToMatrix(List<ItemStack> input) {
        Matrix<ItemStack> matrix = new Matrix<>(GRID_SIZE, GRID_SIZE);
        for (int i = 0; i < 9; i++) {
            matrix.set(i % GRID_SIZE, i / GRID_SIZE, input.get(i));
        }
        return matrix;
    }

    /**
     * Checks if the pattern matches at a specific position in the grid.
     *
     * @param grid The 3x3 crafting grid
     * @param offsetX X offset where pattern starts
     * @param offsetY Y offset where pattern starts
     * @param mirror Whether to check mirrored pattern
     * @return true if pattern matches at this position
     */
    private boolean matchesAtPosition(Matrix<ItemStack> grid, int offsetX, int offsetY, boolean mirror) {
        if (!patternMatches(grid, offsetX, offsetY, mirror)) {
            return false;
        }

        return nonPatternSlotsEmpty(grid, offsetX, offsetY);
    }

    /**
     * Verifies that the pattern matches at the specified position.
     *
     * @param grid The crafting grid
     * @param offsetX X offset of pattern
     * @param offsetY Y offset of pattern
     * @param mirror Whether to use mirrored pattern
     * @return true if pattern matches
     */
    private boolean patternMatches(Matrix<ItemStack> grid, int offsetX, int offsetY, boolean mirror) {
        for (int y = 0; y < pattern.height; y++) {
            for (int x = 0; x < pattern.width; x++) {
                int patternX = mirror ? pattern.width - 1 - x : x;
                char expectedChar = pattern.get(patternX, y);
                ItemStack actualItem = grid.get(offsetX + x, offsetY + y);

                if (!slotMatches(expectedChar, actualItem)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Ensures all grid slots outside the pattern area are empty.
     *
     * @param grid The crafting grid
     * @param offsetX Pattern X offset
     * @param offsetY Pattern Y offset
     * @return true if all non-pattern slots are empty
     */
    private boolean nonPatternSlotsEmpty(Matrix<ItemStack> grid, int offsetX, int offsetY) {
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                if (isWithinPattern(x, y, offsetX, offsetY)) {
                    continue;
                }
                if (!Recipe.isEmpty(grid.get(x, y))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if a grid position falls within the pattern area.
     *
     * @param x Grid X coordinate
     * @param y Grid Y coordinate
     * @param offsetX Pattern X offset
     * @param offsetY Pattern Y offset
     * @return true if position is within pattern bounds
     */
    private boolean isWithinPattern(int x, int y, int offsetX, int offsetY) {
        return x >= offsetX && x < offsetX + pattern.width &&
                y >= offsetY && y < offsetY + pattern.height;
    }

    /**
     * Checks if a single slot matches the expected pattern character.
     *
     * @param patternChar Expected character from pattern (' ' for empty, others for ingredients)
     * @param gridItem Actual item in the grid slot
     * @return true if slot matches expectation
     * @throws IllegalStateException if pattern character has no corresponding ingredient
     */
    private boolean slotMatches(char patternChar, ItemStack gridItem) {
        if (patternChar == ' ') {
            return Recipe.isEmpty(gridItem);
        }

        Material expectedMaterial = ingredients.get(patternChar);
        if (expectedMaterial == null) {
            throw new IllegalStateException("No ingredient mapping found for pattern character: '" + patternChar + "'");
        }

        return !Recipe.isEmpty(gridItem) && gridItem.material() == expectedMaterial;
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