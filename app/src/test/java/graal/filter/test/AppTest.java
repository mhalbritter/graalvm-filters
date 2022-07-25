package graal.filter.test;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

class AppTest {

    // See comment in Class 'C' why this has to be a String[] array
    private static final String[] APP_CLASS_NAME = new String[] { "graal.filter.test.App" };

    @Test
    void test() throws Exception {
        Class<?> clazz = Class.forName(APP_CLASS_NAME[0]);
        Method method = clazz.getDeclaredMethod("main", String[].class);
        method.invoke(null, new Object[] { new String[0] });
    }
}
