package com.github.jbharter.caching.FlashCache;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public abstract class FlashCache<K,V> implements Map<K,V> {

    private AtomicLong entryCount = new AtomicLong(0);
    protected ConcurrentHashMap<K,V> internalCache;
    private FlashCache<K,V> self;

    public FlashCache() { // Default is 1 million element cache
        FlashCacheBuilder<K, V> b = new FlashCacheBuilder<>();
        this.self = new FlashCacheByElements<>(b);
        internalCache = b.getMap();
    }
    public FlashCache(FlashCacheBuilder<K, V> b) {
        this.self = new FlashCacheByElements<>(b);
        internalCache = b.getMap();
    }
    public FlashCache<K, V> getCache() {
        return this.self;
    }
    static<K,V> FlashCache<K,V> build(FlashCacheBuilder<K,V> builder) {
        return new FlashCacheByElements<>(builder);
    }
}
