package com.github.jbharter.caching;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FlashCache<K,V> extends BaseCache<K,V> {

    private ConcurrentLinkedQueue<K> keyQueue = new ConcurrentLinkedQueue<>();

    public FlashCache() {
        internalCache   = new ConcurrentHashMap<>();
    }
    public FlashCache(Function<K,V> function) {
        internalCache   = new ConcurrentHashMap<>();
        mapper = function;
    }
    public FlashCache(Long maxElements) {
        super(maxElements);
        internalCache   = new ConcurrentHashMap<>();
    }
    public FlashCache(Long step, Long maxElements) {
        super(step,maxElements);
        internalCache   = new ConcurrentHashMap<>();
    }

    public void purge() {
        if (keyQueue.size() > 0) internalCache.remove(keyQueue.poll());
        else { clear(); }
    }
    public void purge(Long num) { for (int i = 0; i < num; ++i) purge(); }

    public FlashCache<K,V> setMapper(Function<? super K, ? extends V> map)   { this.mapper = map; return this; }
    private ConcurrentLinkedQueue<K> getKeyQueue()                      { return keyQueue; }
    private Function<? super K,? extends V> getFunc()                   { return this.mapper; }

    // Map interface
    public V put(K key) {
        try {
            if (this.mapper == null) throw new NoSuchMethodException("Called put() on mapper, but no mapper initialized");
            V val = this.mapper.apply(key);
            put(key,val);
            return val;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }
    public Set<V> put(Collection<K> keyColl) { return keyColl.stream().map(this::put).collect(Collectors.toSet()); }
    public V put(K key, V value) {
        if (notfull()) {
            keyQueue.add(key);
            return internalCache.put(key,value);
        } else {
            purge(getPurgeStep());
            keyQueue.add(key);
            return internalCache.put(key,value);
        }
    }
    public V put(K key, Function<? super K,? extends V> mapper) { return put(key,mapper.apply(key)); }
//    public void putAll(Map<? extends K, ? extends V> m) {
//        if (m.size() + internalCache.size() <= getUpperBound()) {
//            keyQueue.addAll(m.keySet());
//            internalCache.putAll(m);
//        } else if (m.size() + internalCache.size() > getUpperBound()) {
//            Queue<K> keyset = new ConcurrentLinkedQueue<>(m.keySet());
//            Queue<V> valset = new ConcurrentLinkedQueue<>(m.values());
//            while (m.size() + internalCache.size() <= getUpperBound()) {
//                put(keyset.poll(),valset.poll());
//            }
//        }
//    }
//    public void putAll(BaseCache<? extends K, ? extends V> bc) { putAll(bc.getInternalCache()); }

    public V remove(K key) {
        keyQueue.remove(key);
        return internalCache.remove(key);
    }

    public V get(K key) {
        return (internalCache.containsKey(key)) ? internalCache.get(key) : put(key);
    }

    @Override
    public V getOrDefault(K key, V defaultValue) {
        return null;
    }

    public void clear() {
        keyQueue.clear();
        internalCache.clear();
    }

    @Override
    public void basicPurgeEvent() {
        System.out.println("FlashCache purge event");
    }

    @Override
    public void criticalPurgeEvent() {
        System.out.println("FlashCache critical purge event");
    }
}
