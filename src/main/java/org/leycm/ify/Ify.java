package org.leycm.ify;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Utility class for conditional logic and matching checks on varargs input.
 * <p>
 * This class provides static methods to test whether a given value appears in a varargs list,
 * and how often, using either standard {@link Object#equals(Object)} or a custom {@link BiPredicate}.
 * </p>
 * <p>
 * Methods that accept a {@code BiPredicate<T, T>} can define custom logic for comparison.
 * The first argument passed to the predicate is the reference value (if applicable), the second
 * is each element from the {@code values} array.
 * If your predicate does not rely on the first value (i.e., the target), you can pass {@code null}
 * or a dummy value as the reference.
 * </p>
 */
public final class Ify {

    private Ify() {
    }

    /**
     * Checks if {@code value} is present at least once in {@code values}, using {@link Object#equals(Object)}.
     */
    @SafeVarargs
    public static <T> boolean in(T value, T... values) {
        return count(Objects::equals, value, values) > 0;
    }

    /**
     * Checks if any element in {@code values} matches the logic defined in {@code comparator} when compared to {@code value}.
     * <p>
     * The comparator can implement any custom matching logic. If you don't need the {@code value} itself in the logic,
     * you may ignore or hardcode it in the comparator.
     * </p>
     */
    @SafeVarargs
    public static <T> boolean in(BiPredicate<T, T> comparator, T value, T... values) {
        return count(comparator, value, values) > 0;
    }

    /**
     * Checks if {@code value} appears exactly {@code expected} times in {@code values}, using {@link Object#equals(Object)}.
     */
    @SafeVarargs
    public static <T> boolean exact(int expected, T value, T... values) {
        return count(Objects::equals, value, values) == expected;
    }

    /**
     * Checks if the number of elements in {@code values} that match the custom logic in {@code comparator}
     * is exactly {@code expected}.
     */
    @SafeVarargs
    public static <T> boolean exact(int expected, BiPredicate<T, T> comparator, T value, T... values) {
        return count(comparator, value, values) == expected;
    }

    /**
     * Checks if {@code value} appears more than {@code threshold} times in {@code values}, using {@link Object#equals(Object)}.
     */
    @SafeVarargs
    public static <T> boolean more(int threshold, T value, T... values) {
        return count(Objects::equals, value, values) > threshold;
    }

    /**
     * Checks if more than {@code threshold} elements in {@code values} match the given {@code comparator}.
     */
    @SafeVarargs
    public static <T> boolean more(int threshold, BiPredicate<T, T> comparator, T value, T... values) {
        return count(comparator, value, values) > threshold;
    }

    /**
     * Checks if {@code value} appears less than {@code threshold} times in {@code values}, using {@link Object#equals(Object)}.
     */
    @SafeVarargs
    public static <T> boolean less(int threshold, T value, T... values) {
        return count(Objects::equals, value, values) < threshold;
    }

    /**
     * Checks if fewer than {@code threshold} elements in {@code values} match the given {@code comparator}.
     */
    @SafeVarargs
    public static <T> boolean less(int threshold, BiPredicate<T, T> comparator, T value, T... values) {
        return count(comparator, value, values) < threshold;
    }

    /**
     * Checks if {@code value} appears at least {@code count} times in {@code values}, using {@link Object#equals(Object)}.
     */
    @SafeVarargs
    public static <T> boolean atLeast(int count, T value, T... values) {
        return count(Objects::equals, value, values) >= count;
    }

    /**
     * Checks if at least {@code count} elements in {@code values} match the {@code comparator}.
     */
    @SafeVarargs
    public static <T> boolean atLeast(int count, BiPredicate<T, T> comparator, T value, T... values) {
        return count(comparator, value, values) >= count;
    }

    /**
     * Checks if {@code value} appears at most {@code count} times in {@code values}, using {@link Object#equals(Object)}.
     */
    @SafeVarargs
    public static <T> boolean atMost(int count, T value, T... values) {
        return count(Objects::equals, value, values) <= count;
    }

    /**
     * Checks if at most {@code count} elements in {@code values} match the {@code comparator}.
     */
    @SafeVarargs
    public static <T> boolean atMost(int count, BiPredicate<T, T> comparator, T value, T... values) {
        return count(comparator, value, values) <= count;
    }

    /**
     * Counts how many times {@code value} matches elements in {@code values} using {@code comparator}.
     * <p>
     * The comparator is given the form {@code comparator.test(value, elementFromValues)}.
     * If your comparator does not use the {@code value}, you may treat it as a placeholder or pass {@code null}.
     * </p>
     */
    @SafeVarargs
    private static <T> int count(BiPredicate<T, T> comparator, T value, T @NotNull ... values) {
        return (int) Arrays.stream(values)
                .filter(v -> comparator.test(value, v))
                .count();
    }
}
