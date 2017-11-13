# JCache
A quick class to provide a lightweight cache wrapper on top of the ConcurrentHashMap class

[![](https://jitpack.io/v/jbharter/JCache.svg)](https://jitpack.io/#jbharter/JCache)
[![](https://travis-ci.org/jbharter/JCache.svg?branch=master)](https://travis-ci.org/jbharter/JCache#)
[![License: BSD-III](https://img.shields.io/badge/license-BSD--III-blue.svg)](https://opensource.org/licenses/BSD-3-Clause)

This is my attempt at a lightweight caching type. There is probably a better built in implementation, but since I like 
to make things more difficult for myself, I created one on my own! This was created as an extension on the 
ConcurrentHashMap class, in an effort to provide an upper bound for the number of objects allowed. In addition to this, 
caches can reference transform functions that allow transparent data manipulation upon retrieval. Sorted cache natively 
purges based on least computationally expensive elements, in the case that a transform is defined.


## Installing
Use this in maven projects by adding this to your repositories,

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

and this to your dependencies

```xml
<dependency>
    <groupId>com.github.jbharter</groupId>
    <artifactId>JCache</artifactId>
    <version>1.0.0-STABLE</version>
</dependency>
```

## Examples
```java
    // Simple K -> K Cache 
    FlashCache<String,String> test = new FlashCache<>(/* Step Size */1L,/* max number of elements*/10L);
        test.put(Arrays.asList("0","1","2","3","4","5","6","7","8","9","10","11","12"));
        test.size() // == 10 --> Cache has an upper bound on it's size
```

```java
    // Simple K -> K cache with transform
    FlashCache<String,String> test = new FlashCache<>();
        
        test.setMapper(String::toUpperCase);
        test.put("rand");
        test.setMapper(String::toLowerCase);
        test.put("OTHER");
        
        test.get("rand") // == "RAND"
        test.get("OTHER") // == "other"
```

```java
    // Simple K -> V Cache
    FlashCache<String,Integer> test = new FlashCache<>(/* Step Size */1L,/* max number of elements*/10L);
        test.setMapper(Integer::parseInt);    

        test.put(Arrays.asList("0","1","2","3","4","5","6","7","8","9","10","11","12"));
        test.get("3") == 3 // String key type maps to Integer Value type. The upper bound is also still imposed.
```

For a slightly more exotic cache, you can specify any transform you like, and when you approach the memory bounds, the 
cache will create space by purging the least computationally expensive elements.
```java
    // Compute Complexity Sorted K -> K  Cache with transform
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
        
        test.poll() // != "fancy func" --> poll pops the least computationally expensive element in the cache

```
