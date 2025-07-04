package io.github.unjoinable.vanille.utils;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.Arrays;

/**
 * A generic 2D matrix with type-safe operations.
 * Uses Object[] internally to avoid generic array creation issues.
 */
public class Matrix<T> {
    public final int width;
    public final int height;
    private final Object[] data;

    /**
     * Creates a new matrix with the specified dimensions.
     * All elements are initially null.
     *
     * @param width  the number of columns (x-axis size)
     * @param height the number of rows (y-axis size)
     */
    public Matrix(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Matrix dimensions must be positive: width=" + width + ", height=" + height);
        }
        this.width = width;
        this.height = height;
        this.data = new Object[width * height];
    }

    /**
     * Creates a new matrix filled with the provided value.
     */
    public Matrix(int width, int height, T fillValue) {
        this(width, height);
        fill(fillValue);
    }

    /**
     * Creates a new matrix using a supplier function to initialize each element.
     */
    public Matrix(int width, int height, Supplier<T> initializer) {
        this(width, height);
        forEach((x, y) -> set(x, y, initializer.get()));
    }

    /**
     * Creates a new matrix using a function that takes coordinates to initialize each element.
     */
    public Matrix(int width, int height, BiFunction<Integer, Integer, T> initializer) {
        this(width, height);
        forEach((x, y) -> set(x, y, initializer.apply(x, y)));
    }

    /**
     * Copy constructor - creates a deep copy of another matrix.
     */
    public Matrix(Matrix<T> other) {
        this.width = other.width;
        this.height = other.height;
        this.data = Arrays.copyOf(other.data, other.data.length);
    }

    public T get(int x, int y) {
        checkBounds(x, y);
        //noinspection unchecked
        return (T) data[getIndex(x, y)];
    }

    public void set(int x, int y, T value) {
        checkBounds(x, y);
        data[getIndex(x, y)] = value;
    }

    /**
     * Gets the value at the specified position, or returns the default value if out of bounds.
     */
    public T getOrDefault(int x, int y, T defaultValue) {
        return isValidPosition(x, y) ? get(x, y) : defaultValue;
    }

    /**
     * Fills the entire matrix with the specified value.
     */
    public void fill(T value) {
        Arrays.fill(data, value);
    }

    /**
     * Fills a rectangular region of the matrix with the specified value.
     */
    public void fillRegion(int startX, int startY, int endX, int endY, T value) {
        for (int y = Math.max(0, startY); y < Math.min(height, endY); y++) {
            for (int x = Math.max(0, startX); x < Math.min(width, endX); x++) {
                set(x, y, value);
            }
        }
    }

    /**
     * Executes the given action for each coordinate in the matrix.
     */
    public void forEach(BiConsumer<Integer, Integer> action) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                action.accept(x, y);
            }
        }
    }

    /**
     * Executes the given action for each element and its coordinates in the matrix.
     */
    public void forEachWithValue(TriConsumer<Integer, Integer, T> action) {
        forEach((x, y) -> action.accept(x, y, get(x, y)));
    }

    /**
     * Creates a new matrix by applying the mapper function to each coordinate.
     */
    public <R> Matrix<R> map(BiFunction<Integer, Integer, R> mapper) {
        Matrix<R> result = new Matrix<>(width, height);
        forEach((x, y) -> result.set(x, y, mapper.apply(x, y)));
        return result;
    }

    /**
     * Creates a new matrix by applying the mapper function to each element and its coordinates.
     */
    public <R> Matrix<R> mapWithValue(TriFunction<Integer, Integer, T, R> mapper) {
        Matrix<R> result = new Matrix<>(width, height);
        forEach((x, y) -> result.set(x, y, mapper.apply(x, y, get(x, y))));
        return result;
    }

    /**
     * Returns true if any element matches the given predicate.
     */
    public boolean anyMatch(BiPredicate<Integer, Integer> predicate) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (predicate.test(x, y)) return true;
            }
        }
        return false;
    }

    /**
     * Returns true if all elements match the given predicate.
     */
    public boolean allMatch(BiPredicate<Integer, Integer> predicate) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!predicate.test(x, y)) return false;
            }
        }
        return true;
    }

    /**
     * Returns true if any element matches the given predicate (with value access).
     */
    public boolean anyMatchWithValue(TriPredicate<Integer, Integer, T> predicate) {
        return anyMatch((x, y) -> predicate.test(x, y, get(x, y)));
    }

    /**
     * Returns true if all elements match the given predicate (with value access).
     */
    public boolean allMatchWithValue(TriPredicate<Integer, Integer, T> predicate) {
        return allMatch((x, y) -> predicate.test(x, y, get(x, y)));
    }

    /**
     * Returns true if the given coordinates are within the matrix bounds.
     */
    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /**
     * Returns a new matrix that is a submatrix of this one.
     */
    public Matrix<T> subMatrix(int startX, int startY, int subWidth, int subHeight) {
        if (startX < 0 || startY < 0 || startX + subWidth > width || startY + subHeight > height) {
            throw new IndexOutOfBoundsException("Submatrix bounds exceed matrix dimensions");
        }

        Matrix<T> result = new Matrix<>(subWidth, subHeight);
        for (int y = 0; y < subHeight; y++) {
            for (int x = 0; x < subWidth; x++) {
                result.set(x, y, get(startX + x, startY + y));
            }
        }
        return result;
    }

    /**
     * Returns a new matrix that is the transpose of this one.
     */
    public Matrix<T> transpose() {
        Matrix<T> result = new Matrix<>(height, width);
        forEach((x, y) -> result.set(y, x, get(x, y)));
        return result;
    }

    /**
     * Converts matrix coordinates to linear array index.
     */
    private int getIndex(int x, int y) {
        return y * width + x;
    }

    private void checkBounds(int x, int y) {
        if (!isValidPosition(x, y)) {
            throw new IndexOutOfBoundsException(
                    String.format("Position (%d, %d) out of bounds for %dx%d matrix", x, y, width, height)
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Matrix<?> other)) return false;
        if (width != other.width || height != other.height) return false;

        return Arrays.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, Arrays.hashCode(data));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Matrix[").append(width).append("x").append(height).append("]:\n");
        for (int y = 0; y < height; y++) {
            sb.append("[");
            for (int x = 0; x < width; x++) {
                if (x > 0) sb.append(", ");
                sb.append(get(x, y));
            }
            sb.append("]\n");
        }
        return sb.toString();
    }

    // Functional interfaces for operations with values
    @FunctionalInterface
    public interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }

    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }

    @FunctionalInterface
    public interface TriPredicate<T, U, V> {
        boolean test(T t, U u, V v);
    }
}