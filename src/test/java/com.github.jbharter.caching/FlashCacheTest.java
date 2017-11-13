package com.github.jbharter.caching;

import junit.framework.TestCase;

import java.util.Arrays;

public class FlashCacheTest extends TestCase {

    public void testBasicCall() {
        FlashCache<String,String> test = new FlashCache<>();

        test.setMapper(String::toUpperCase);
        test.put("rand");
        test.setMapper(String::toLowerCase);
        test.put("OTHER");

        assertTrue("Basic Call Test", test.get("rand").equals("RAND"));
        assertTrue("Basic Call Test", test.get("OTHER").equals("other"));

    }

    public void testUpperBounds() {
        FlashCache<String,Integer> test = new FlashCache<>(1L,10L);
        test.setMapper(Integer::parseInt);

        test.put(Arrays.asList("0","1","2","3","4","5","6","7","8","9","10","11","12"));

        assertTrue("Upper Bounds Test", test.size() == 10);
    }

    public void testClear() {
        FlashCache<String,String> test = new FlashCache<>(1L,10L);

        test.put("key","val");
        test.put("key0","val0");

        assertTrue("Test Clear", test.size() == 2);
        test.clear();
        assertTrue("Test Clear", test.size() == 0);
    }

}
