package com.github.jbharter.caching;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Objects;

public class SortedCacheTest extends TestCase {

    public void testBasicCall() {
        SortedCache<String,String> test = new SortedCache<>();
        test.setMapper(String::trim);

        test.put(" a string to be trimmed ");
        assertTrue("Basic Call", test.get(" a string to be trimmed ").equals("a string to be trimmed"));

        test.setMapper(each -> each.trim().toUpperCase());
        test.put(" another ");
        assertTrue("Basic Call", test.get(" another ").equals("ANOTHER"));
    }

    public void testComputeQueue() {
        SortedCache<String,String> test = new SortedCache<>();
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
        SortedCache<String,String> test = new SortedCache<>();

        test.setMapper(String::trim);
        test.put(Arrays.asList("0","1","2","3","4","5","6","7","8","9","10","11","12"));

        assertTrue("Test Clear", test.size() == 13);
        test.clear();
        assertTrue("Test Clear", test.size() == 0);
    }
}
