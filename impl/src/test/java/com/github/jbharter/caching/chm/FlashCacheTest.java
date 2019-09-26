package com.github.jbharter.caching.chm;

import junit.framework.TestCase;

import java.util.Arrays;

public class FlashCacheTest extends TestCase {

    public void testBasicCall() {
        Cache<String,String> test = new FlashCache<>();

        test.setMapper(String::toUpperCase);
        test.put("rand");
        test.setMapper(String::toLowerCase);
        test.put("OTHER");

        assertEquals("Basic Call Test", "RAND", test.get("rand"));
        assertEquals("Basic Call Test", "other", test.get("OTHER"));

    }

    public void testUpperBounds() {
        Cache<String,Integer> test = new FlashCache<>(1L,10L);
        test.setMapper(Integer::parseInt);

        test.putAll(Arrays.asList("0","1","2","3","4","5","6","7","8","9","10","11","12"));

        assertEquals("Upper Bounds Test", 10, (long) test.size());
    }

    public void testClear() {
        Cache<String,String> test = new FlashCache<>(1L,10L);

        test.put("key","val");
        test.put("key0","val0");

        assertEquals("Test Clear", 2, (long) test.size());
        test.clear();
        assertEquals("Test Clear", 0, (long) test.size());
    }

}
