package kechin.dmitrii.yaTask;

public class ObjectUtil {

    private ObjectUtil() {}

    public static void assertNotNull(Object object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
    }
}
