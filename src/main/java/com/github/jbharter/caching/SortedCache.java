package com.github.jbharter.caching;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SortedCache<K,V> extends BaseCache<K,V> {

    private ConcurrentHashMap<Long,K>   internalComputeTimeMap   = new ConcurrentHashMap<>();
    private PriorityBlockingQueue<Long> internalComputeTimeQueue = new PriorityBlockingQueue<>();

    public SortedCache() {
        //super(new CacheManagement(-1L,-1L));
        internalCache   = new ConcurrentHashMap<>();
    }
    public SortedCache(SortedCache<K,V> c) {
        //super(new CacheManagement(-1L,-1L));
        internalCache = new ConcurrentHashMap<>(c.internalCache);
        internalComputeTimeMap = new ConcurrentHashMap<>(c.getInternalComputeTimeMap());
        internalComputeTimeQueue = new PriorityBlockingQueue<>(c.getInternalComputeTimeQueue());
        mapper = c.mapper;
    }
    private ConcurrentHashMap<Long,K> getInternalComputeTimeMap()     { return internalComputeTimeMap; }
    private PriorityBlockingQueue<Long> getInternalComputeTimeQueue() { return internalComputeTimeQueue; }

    public V poll()                                                                     { return internalCache.remove(internalComputeTimeMap.remove(internalComputeTimeQueue.poll())); }
    public V put(K key)                                                                 { return put(key,this.mapper); }
    public V put(K key, Function<? super K,? extends V> map)                            {
        if (getMemPressure() > doubleMemPressureMax) purge(DEFAULT_PURGE_STEP);
        Long t0 = System.nanoTime();
        V val = map.apply(key);
        Long time = (System.nanoTime() - t0);

        internalCache.put(key,val);
        internalComputeTimeMap.put(time,key);
        internalComputeTimeQueue.add(time);
        return internalCache.get(key);
    }
    public void put(Collection<? extends K> c)                                          { c.forEach(this::get); }
    public void put(Collection<? extends K> c, Function<? super K,? extends V> mapper)  { c.forEach(thing -> put(thing,mapper)); }
    public void setMapper(Function<? super K,? extends V> map)                          { this.mapper = map; }
    public V get(K key)                                                                 { return internalCache.getOrDefault(key, this.mapper != null ? put(key) : null); }

    @Override
    public V getOrDefault(K key, V defaultValue) {
        return null;
    }

    public void purge()                                                                        {
        if (size() > 0) poll();
        else clear();
    }
    public void purge(Long num)                                                                { for (int i = 0; i < num; ++i) purge(); }
    public V remove(Object key)                                                         {
        Set<Long> rset = internalComputeTimeMap.entrySet().parallelStream().filter(any -> any.getValue().equals(key)).map(Map.Entry::getKey).collect(Collectors.toSet());
        rset.forEach(inSet -> {
            internalComputeTimeMap.remove(inSet);
            internalComputeTimeQueue.remove(inSet);
        });
        return internalCache.remove(key);
    }
    public void forEach(BiConsumer<? super K, ? super V> action)                        { internalCache.forEach(action); }
    public void clear()                                                                 {
        internalCache.clear();
        internalComputeTimeMap.clear();
        internalComputeTimeQueue.clear();
    }
    public Set<K> keySet()                                                              { return internalCache.keySet(); }
    public Collection<V> values()                                                       { return internalCache.values(); }
    public Set<Map.Entry<K, V>> entrySet()                                              { return internalCache.entrySet(); }

    @Override
    public void basicPurgeEvent() {

    }

    @Override
    public void criticalPurgeEvent() {

    }
}
