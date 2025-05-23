package org.leycm.test;

import org.leycm.ify.Ify;

import static org.leycm.ify.Ify.*;

public class Main {
    public static void main(String[] args) {

        System.out.println("--- Basic Tests ---");

        System.out.println(in("apple", "banana", "apple", "orange")); // true
        System.out.println(more(2, "yes", "yes", "no", "yes", "yes")); // true
        System.out.println(exact(2, "gold", "gold", "gold", "silver")); // true
        System.out.println(less(1, "x", "y", "z")); // true
        System.out.println(atLeast(3, 42, 42, 42, 42)); // true
        System.out.println(atMost(2, "a", "a", "a")); // false

        System.out.println("--- With Custom Comparators ---");

        System.out.println(
                exact(1, (a, b) -> a.toLowerCase().equals(b.toLowerCase()), "hello", "HELLO", "world")
        ); // true

        System.out.println(
                Ify.<Object>in((a, b) -> a instanceof Integer, null, 123, "hi", 5.5)
        ); // true

        System.out.println(
                more(1, (a, b) -> b != null && b.getClass().getSimpleName().equals("UserData"), null, new UserData(), new Object())
        ); // true

        System.out.println(
                atLeast(2, (a, b) -> ((String) b).startsWith("wow"), "", "wow!", "wow again", "nope")
        ); // true
    }
}