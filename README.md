# JCache
A quick class to provide a lightweight cache wrapper on top of the ConcurrentHashMap class

[![](https://jitpack.io/v/jbharter/JCache.svg)](https://jitpack.io/#jbharter/JCache)
[![](https://travis-ci.org/jbharter/JCache.svg?branch=master)](https://travis-ci.org/jbharter/JCache#)

This is my attempt at a lightweight caching type. There is probably a better built in implementation, but since I like to make things more difficult for myself, I created one on my own! This was created as an extension on the ConcurrentHashMap class, in an effort to provide an upper bound for the number of objects allowed.

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
