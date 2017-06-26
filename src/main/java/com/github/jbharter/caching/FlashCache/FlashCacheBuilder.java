package com.github.jbharter.caching.FlashCache;

import java.util.concurrent.ConcurrentHashMap;


public class FlashCacheBuilder<K,V> {
    private int maxEntries = 1000000;
    private ConcurrentHashMap<K,V> map = new ConcurrentHashMap<>();
    private int optBuffSize = 20;

    public FlashCacheBuilder() {

    }
    public FlashCacheBuilder setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
        return this;
    }
    public FlashCacheBuilder setOptBufferSize(int numEntries) {
        this.optBuffSize = numEntries;
        return this;
    }
    ConcurrentHashMap<K,V> getMap() {
        return this.map;
    }
    int getMaxEntries() {
        return this.maxEntries;
    }
    int getOptBufferSize() {
        return optBuffSize;
    }
    public FlashCache<K,V> build() {
        return new FlashCacheByElements<>(this);
    }
}
