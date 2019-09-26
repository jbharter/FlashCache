package com.github.jbharter.caching.chm;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Objects;

public class SortedCacheTest extends TestCase {

    public void testBasicCall() {
        Cache<String,String> test = new SortedCache<>();

        test.setMapper(String::trim);

        test.put(" a string to be trimmed ");
        assertEquals("Basic Call", "a string to be trimmed", test.get(" a string to be trimmed "));

        test.setMapper(each -> each.trim().toUpperCase());
        test.put(" another ");
        assertEquals("Basic Call", "ANOTHER", test.get(" another "));
    }

    public void testComputeQueue() {
        Cache<String,String> test = new SortedCache<>();
        test.setMapper(String::trim);
        test.put(Arrays.asList("0","1","2","3","4","5","6","7","8","9","10","11","12"));

        test.setMapper(str -> {
            try {
                Thread.sleep(1000);
                return "fancy func";
            } catch (InterruptedException e) {
                e.printStackTrace();
                return "fail";
            }
        });

        test.put("fancy key");
        System.out.println(test.poll());
        System.out.println(test.poll());
        System.out.println(test.poll());
        System.out.println(test.poll());
        System.out.println(test.poll());


        assertTrue("Test Compute Queue", !Objects.equals(test.poll(), "fancy func"));
    }

    public void testClear() {
        Cache<String,String> test = new SortedCache<>();

        test.setMapper(String::trim);
        test.put(Arrays.asList("0","1","2","3","4","5","6","7","8","9","10","11","12"));

        assertEquals("Test Clear", 13, (long) test.size());
        test.clear();
        assertEquals("Test Clear", 0, (long) test.size());
    }
}
