package com.github.jbharter.caching.FlashCache;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public abstract class FlashCache<K,V> implements Map<K,V> {

    private ConcurrentHashMap<K,V> internalCache;

    public class FlashCacheBuilder {
        private int maxEntries = 1000000;
        private ConcurrentHashMap<K,V> map = new ConcurrentHashMap<>();
        private int optBuffSize = 20;

        public FlashCacheBuilder setMaxEntries(int maxEntries) {
            this.maxEntries = maxEntries;
            return this;
        }
        public FlashCacheBuilder setOptBufferSize(int numEntries) {
            this.optBuffSize = numEntries;
            return this;
        }
        ConcurrentHashMap<K,V> getMap() { return this.map; }
        int getMaxEntries() { return this.maxEntries; }
        int getOptBufferSize() { return optBuffSize; }

        public FlashCache build() {
            return new FlashCacheByElements(this);
        }
    }

    public class FlashCacheByElements extends FlashCache<K,V> implements Map<K,V> {
        private AtomicLong maxNumElements;
        private AtomicLong numElements;
        private ConcurrentLinkedQueue<K> keyQueue = new ConcurrentLinkedQueue<>();
        private AtomicBoolean near = new AtomicBoolean(false);
        private int optBufferSize;

        public FlashCacheByElements(FlashCacheBuilder builder) {
            internalCache   = builder.getMap();
            maxNumElements  = new AtomicLong(builder.getMaxEntries());
            numElements     = new AtomicLong(0);
            optBufferSize   = builder.getOptBufferSize();
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
}
