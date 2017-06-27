package com.github.jbharter.caching;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class FlashCache<K,V> implements Map<K,V> {

    private ConcurrentHashMap<K,V> internalCache;

    private AtomicLong maxNumElements;
    private AtomicLong numElements;
    private ConcurrentLinkedQueue<K> keyQueue = new ConcurrentLinkedQueue<>();
    private AtomicBoolean near = new AtomicBoolean(false);
    private int optBufferSize;

    public FlashCache() {
        internalCache   = new ConcurrentHashMap<>();
        maxNumElements  = new AtomicLong(1000000);
        numElements     = new AtomicLong(0);
        optBufferSize   = 20;
    }
    public FlashCache(Long maxElements) {
        internalCache   = new ConcurrentHashMap<>();
        maxNumElements  = new AtomicLong(maxElements);
        numElements     = new AtomicLong(0);
        optBufferSize   = 20;
    }
    public FlashCache(Long maxElements, int buffSize) {
        internalCache   = new ConcurrentHashMap<>();
        maxNumElements  = new AtomicLong(maxElements);
        numElements     = new AtomicLong(0);
        optBufferSize   = buffSize;
    }

    public int size() {
        numElements.getAndSet(internalCache.size());
        return numElements.intValue();
    }
    public boolean isEmpty() { return internalCache.isEmpty(); }
    public boolean containsKey(Object key) { return internalCache.containsKey(key); }
    public boolean containsValue(Object value) { return internalCache.containsValue(value); }
    public V get(Object key) { return internalCache.get(key); }


    public Set<K> keySet() { return internalCache.keySet(); }
    public Collection<V> values() { return internalCache.values(); }
    public Set<Entry<K, V>> entrySet() { return internalCache.entrySet(); }
    public void clear() {
        internalCache.clear();
        keyQueue.clear();
        usize();
    }
    public V remove(Object key) { return (key != null) ? internalRemove((K)key) : null; }


    private void usize() { numElements.getAndSet(keyQueue.size()); }
    private void purge(int num){
        if (keyQueue.size() > num)
            for (int i = 0; i < num; ++i) internalCache.remove(keyQueue.poll());
        else keyQueue.clear();
    }
    private V internalRemove(K key) {
        keyQueue.remove(key);
        usize();
        return internalCache.remove(key);
    }

    public V put(K key, V value) {
        if (!near.get()) {
            near.getAndSet(maxNumElements.get() - optBufferSize > numElements.incrementAndGet());
            return internalCache.put(key,value);
        } else if (maxNumElements.get() > numElements.get()) {
            numElements.incrementAndGet();
            return internalCache.put(key,value);
        } else {
            purge(optBufferSize);
            return put(key,value);
        }
    }
    public void putAll(Map<? extends K, ? extends V> m) {
        if (m.size() < maxNumElements.get()) {
            internalCache.putAll(m);
        } else {
            Long added = m.entrySet().parallelStream().map(entry -> put(entry.getKey(),entry.getValue())).count();
            if (added == m.size()) near.getAndSet(m.size() - optBufferSize > numElements.get());
        }
    }
}
