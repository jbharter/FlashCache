package com.github.jbharter.caching;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SortedCache<K,V> extends BaseCache<K,V> {

    private ConcurrentHashMap<K,V>        internalCache            = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Double,K>   internalComputeTimeMap   = new ConcurrentHashMap<>();
    private PriorityBlockingQueue<Double> internalComputeTimeQueue = new PriorityBlockingQueue<>();
    private Function<K,V> mapper;

    public SortedCache() {
    }

    private V internalPoll() {
        return internalCache.remove(internalComputeTimeMap.remove(internalComputeTimeQueue.poll()));
    }
    private V internalPut(K key, Function<K,V> function) {
        Long t0 = System.nanoTime();
        V val = function.apply(key);
        Double time = ((double) System.currentTimeMillis() - t0);

        internalCache.put(key,val);
        internalComputeTimeMap.put(time,key);
        internalComputeTimeQueue.add(time);
        return val;
    }
    private V internalPutOverrideVal(K key, V val, Function<K,V> function) {
        Long t0 = System.nanoTime();
        function.apply(key);
        Double time = ((double) System.currentTimeMillis() - t0);

        internalComputeTimeMap.put(time,key);
        internalComputeTimeQueue.add(time);
        return internalCache.put(key,val);
    }
    public void setMapper(Function<K,V> map) { this.mapper = map; }

    public int size() {
        return 0;
    }
    public boolean isEmpty() {return false;}
    public boolean containsKey(Object key) {return false;}
    public boolean containsValue(Object value) {return false;}
    public V get(Object key) {return null;}
    public V put(K key) { return internalPut(key,this.mapper); }
    public V put(K key, Function<K,V> map) { return internalPut(key,map); }

    V internalRemove(K key) {
        return null;
    }
    V internalPut(K key, V val) {
        return null;
    }
    void internalPutAll(Map<? extends K, ? extends V> m) {

    }
    void purge() {
        internalPoll();
    }
    void purge(int num) {
        for (int i = 0; i < num && size() > 0; ++i) purge();
    }
    public void finalize() {
        instanceSet.remove(this);
    }

    public V put(K key, V value) {return internalPutOverrideVal(key,value,(k -> value));} // Not preferred, but overridden
    public V remove(Object key) {return null;}
    public void putAll(Map<? extends K, ? extends V> m) {}
    public void clear() {}
    public Set<K> keySet() {return null;}
    public Collection<V> values() {return null;}
    public Set<Entry<K, V>> entrySet() {return null;}

    public V getOrDefault(Object key, V defaultValue) {return null;}
    public void forEach(BiConsumer<? super K, ? super V> action) {}
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {}
    public V putIfAbsent(K key, V value) { return null;}
    public boolean remove(Object key, Object value) {return false;}
    public boolean replace(K key, V oldValue, V newValue) {return false;}
    public V replace(K key, V value) {return null;}
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {return null;}
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {return null;}
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {return null;}
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {return null;}
}
