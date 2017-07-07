package com.github.jbharter.caching;


import java.util.concurrent.atomic.AtomicLong;

public class SortedCacheTest {
    public static void main(String[] args) {
        SortedCache<String,String> cache = new SortedCache<>();
        cache.setMapper(String::toUpperCase);

        cache.put("key");
        cache.put("another key");
        AtomicLong increment = new AtomicLong(0);

        while (BaseCache.memPressure() < 0.9) {
            cache.put("Key " + Long.toString(increment.incrementAndGet()));
        }

        cache.forEach((key, value) -> System.out.println(key + " => " + value));

    }
}
