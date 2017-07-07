package com.github.jbharter.caching;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SortedCache<K,V> extends BaseCache<K,V> {

    private ConcurrentHashMap<K,V>        internalCache            = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Double,K>   internalComputeTimeMap   = new ConcurrentHashMap<>();
    private PriorityBlockingQueue<Double> internalComputeTimeQueue = new PriorityBlockingQueue<>();
    private Function<? super K,? extends V> mapper;

    public SortedCache() {
        instanceSet.put(this, thing -> {
            if(thing instanceof Integer) purge((int)thing);
            else purge();
            return null;
        });
    }
    public SortedCache(SortedCache<K,V> c) {
        internalCache = new ConcurrentHashMap<>(c.getInternalCache());
        internalComputeTimeMap = new ConcurrentHashMap<>(c.getInternalComputeTimeMap());
        internalComputeTimeQueue = new PriorityBlockingQueue<>(c.getInternalComputeTimeQueue());
        mapper = c.mapper;
    }
    private ConcurrentHashMap<Double,K> getInternalComputeTimeMap() { return internalComputeTimeMap; }
    private PriorityBlockingQueue<Double> getInternalComputeTimeQueue() { return internalComputeTimeQueue; }

    private V internalPoll() {
        return internalCache.remove(internalComputeTimeMap.remove(internalComputeTimeQueue.poll()));
    }
    V internalPut(K key, Function<? super K,? extends V> function) {
        makeWay();
        Long t0 = System.nanoTime();
        V val = function.apply(key);
        Double time = ((double) System.currentTimeMillis() - t0);

        internalCache.put(key,val);
        internalComputeTimeMap.put(time,key);
        internalComputeTimeQueue.add(time);
        return val;
    }
    V internalPut(K key, V val) { return internalCache.put(key, val); }
    void internalPutAll(Map<? extends K, ? extends V> m) { internalCache.putAll(m); }
    private V internalPutOverrideVal(K key, V val, Function<K,V> function) {
        Long t0 = System.nanoTime();
        function.apply(key);
        Double time = ((double) System.currentTimeMillis() - t0);

        internalComputeTimeMap.put(time,key);
        internalComputeTimeQueue.add(time);
        return internalCache.put(key,val);
    }
    public void setMapper(Function<? super K,? extends V> map) { this.mapper = map; }

    public V put(K key)                                                 { return internalPut(key,this.mapper); }
    public void put(Collection<? extends K> c)                          { c.parallelStream().forEach(this::put); }
    public V put(K key, Function<K,V> map)                              { return internalPut(key,map); }
    public void put(Collection<? extends K> c, Function<K,V> mapper)    { c.parallelStream().forEach(thing -> put(thing,mapper));}

    V internalRemove(Object key) {
        Set<Double> rset = internalComputeTimeMap.entrySet().parallelStream().filter(any -> any.getValue().equals(key)).map(Map.Entry::getKey).collect(Collectors.toSet());
        rset.forEach(inSet -> {
            internalComputeTimeMap.remove(inSet);
            internalComputeTimeQueue.remove(inSet);
        });
        return internalCache.remove(key);
    }

    void purge()                { internalPoll(); }
    void purge(int num)         { for (int i = 0; i < num && size() > 0; ++i) purge(); }
    void purgeHard(int num) {
        purge(num);
        SortedCache<K,V> t = this;
        this.internalCache = new ConcurrentHashMap<>(t.size());
        this.internalCache = t.getInternalCache();
        this.internalComputeTimeMap = t.getInternalComputeTimeMap();
        this.internalComputeTimeQueue = t.getInternalComputeTimeQueue();
        this.mapper = t.mapper;
    }
    Long getMeanMemberSize() {
        return null;
    }

    public void finalize()      { instanceSet.remove(this); }

    public V put(K key, V value) { return internalPutOverrideVal(key,value,(k -> value));} // Not preferred, but overridden
    public V remove(Object key) {return internalRemove(key);}
    public void putAll(Map<? extends K, ? extends V> m) { internalPutAll(m); }
    public void clear() {
        internalCache.clear();
        internalComputeTimeMap.clear();
        internalComputeTimeQueue.clear();
    }
    public Set<K> keySet() { return internalCache.keySet(); }
    public Collection<V> values() { return internalCache.values(); }
    public Set<Entry<K, V>> entrySet() { return internalCache.entrySet(); }

    public void forEach(BiConsumer<? super K, ? super V> action) { internalCache.forEach(action); }
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) { return internalCache.compute(key, remappingFunction); }
}
