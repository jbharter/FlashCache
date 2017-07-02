package com.github.jbharter.caching;

public class FlashCacheTest {
    public static void main(String[] args) {
        FlashCache<String,String> test = new FlashCache<>(100000L);
        for (int i = 0; i < 10000000; i++) {
            test.put(Integer.toString(i), "Random Text " + Integer.toString(i));
            if (i % 10000 == 0) {
                System.out.println(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
            }
        }
    }
}
