package com.github.jbharter.caching;

public class FlashCacheTest {
    public static void main(String[] args) {
        FlashCache<String,String> test = new FlashCache<>(input -> input + " and one");
        for (int i = 0; i < 10000000; i++) {
            test.put("A text");
            if (i % 10000 == 0) {
                test = new FlashCache<>(input -> input + " and one");
                System.out.println(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
            }
        }
    }
}
