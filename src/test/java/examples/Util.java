package examples;

import org.junit.Assert;

import java.util.function.Supplier;

public class Util {

    public static void waitUntilEquals(Supplier<Integer> query, int expected, long timeoutMillis) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            try {
                if (query.get() == expected) {
                    return;
                }
                Thread.sleep(500);
            } catch (Exception e) {
                continue;
            }
        }

        Assert.assertTrue("Condition not fulfilled after timeout", false);
    }

    public static void waitUntilStable(Supplier<Integer> query, long interval) {
        int initialValue = query.get();
        while (true) {
            try {
                Thread.sleep(interval);
                int newValue = query.get();
                if (newValue == initialValue) {
                    return;
                }
                initialValue = newValue;
            } catch (Exception e) {
                continue;
            }
        }
    }

}
