package graal.filter.test;

import java.lang.reflect.Method;

/**
 * @author Moritz Halbritter
 */
class C {

    // Array is necessary to trick the static analysis of GraalVM - without it the compiler
    // is clever enough to generate reflection metadata on its own.
    private static final String[] B_CLASS_NAME = new String[] { "graal.filter.test.B" };

    void invokeB() throws Exception {
        Class<?> clazz = Class.forName(B_CLASS_NAME[0]);
        Method someMethod = clazz.getDeclaredMethod("someMethod");
        Object instance = clazz.getDeclaredConstructor().newInstance();
        someMethod.invoke(instance);
    }
}
