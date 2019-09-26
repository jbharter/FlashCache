package com.github.jbharter.caching.chm;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public class FlashCache<K,V> extends BaseCache<K,V> {

    private ConcurrentLinkedQueue<K> keyQueue = new ConcurrentLinkedQueue<>();

    public FlashCache() {
        super();
    }

    public FlashCache(Function<K,V> function) {
        setCacheMappingFunction(function);
    }

    public FlashCache(Long maxElements) {
        super(maxElements);
    }

    public FlashCache(Long step, Long maxElements) {
        super(step,maxElements, ConcurrentHashMap::new);
    }

    public void purge() {
        if (keyQueue.size() > 0) {
            remove(keyQueue.poll());
        } else {
            clear();
        }
    }

    @Override
    protected void purge(int purgeDepth) {

    }

    public void purge(Long num) { for (int i = 0; i < num; ++i) purge(); }


    // Map interface
    public V put(K key) {
        try {
            if (getCacheMappingFunction() == null) throw new NoSuchMethodException("Called put() on mapper, but no mapper initialized");
            V val = getCacheMappingFunction().apply(key);
            put(key,val);
            return val;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void putAll(Collection<? extends K> keyCollection) {
        keyCollection.forEach(this::put);
    }

    @Override
    public V poll() {
        return remove(keyQueue.poll());
    }

    @Override
    public V put(K key, V value) {
        if (upperBound.get() > size()) {
            keyQueue.add(key);
            return getCache().put(key,value);
        } else { // Too full, do purge.
            purge(purgeStep.get());
            return put(key, value);
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (m.size() + size() <= upperBound.get()) {
            keyQueue.addAll(m.keySet());
            getCache().putAll(m);
        } else {
            purge(m.size());
            putAll(m);
        }
    }

    @Override
    public V remove(Object key) {
        // FIXME - Need to be able to remove this
        keyQueue.remove(keyCast(key));
        return getCache().remove(key);
    }

    @Override
    public V get(Object key) {
        if (containsKey(key)) {
            return getCache().get(key);
        } else {
            return getCache().computeIfAbsent(keyCast(key),getCacheMappingFunction());
        }
    }

    public void clear() {
        keyQueue.clear();
        super.clear();
    }

    @Override
    public void basicPurgeEvent() {
        System.out.println("FlashCache purge event");
        purge();
    }

    @Override
    public void criticalPurgeEvent() {
        System.out.println("FlashCache critical purge event");
        clear();
    }
}
