package com.github.jbharter.caching;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.jbharter.caching.CacheManager.getMemPressure;

public class SortedCache<K,V> extends BaseCache<K,V> {

    private ConcurrentHashMap<Long,K>   internalComputeTimeMap   = new ConcurrentHashMap<>();
    private PriorityBlockingQueue<Long> internalComputeTimeQueue = new PriorityBlockingQueue<>();

    public SortedCache() {
        super();
    }
    public SortedCache(SortedCache<K,V> c) {
        super(c.upperBound.get(), c.purgeStep.get(), c::getCache);

        internalComputeTimeMap = new ConcurrentHashMap<>(c.getInternalComputeTimeMap());
        internalComputeTimeQueue = new PriorityBlockingQueue<>(c.getInternalComputeTimeQueue());

        setCacheMappingFunction(c.getCacheMappingFunction());
    }
    private ConcurrentHashMap<Long,K> getInternalComputeTimeMap()     {
        return internalComputeTimeMap;
    }
    private PriorityBlockingQueue<Long> getInternalComputeTimeQueue() {
        return internalComputeTimeQueue;
    }

    public V poll() {
        return remove(internalComputeTimeMap.remove(internalComputeTimeQueue.poll()));
    }

    public V put(K key)                                                                 {
        return put(key,getCacheMappingFunction());
    }

    public V put(K key, Function<? super K,? extends V> map)                            {
        if (getMemPressure() > DEFAULT_PRESSURE_MAX) purge(DEFAULT_PURGE_STEP);
        Long t0 = System.nanoTime();
        V val = map.apply(key);
        Long time = (System.nanoTime() - t0);

        put(key,val);
        internalComputeTimeMap.put(time,key);
        internalComputeTimeQueue.add(time);
        return get(key);
    }

    public void putAll(Collection<? extends K> c)                                          {
        c.forEach(this::put);
    }
    @Deprecated
    public void put(Collection<? extends K> c, Function<? super K,? extends V> mapper)  {
        c.forEach(thing -> put(thing,mapper));
    }

    @Override
    protected void purge(int purgeDepth) {

    }

    public void purge(Long num) {
        for (int i = 0; i < num; ++i) {
            purge();
        }
    }

    public V remove(Object key)                                                         {
        Set<Long> rset = internalComputeTimeMap.entrySet().parallelStream().filter(any -> any.getValue().equals(key)).map(Map.Entry::getKey).collect(Collectors.toSet());
        rset.forEach(inSet -> {
            internalComputeTimeMap.remove(inSet);
            internalComputeTimeQueue.remove(inSet);
        });
        return getCache().remove(key);
    }
    public void forEach(BiConsumer<? super K, ? super V> action)                        {
        getCache().forEach(action);
    }
    public void clear()                                                                 {
        getCache().clear();
        internalComputeTimeMap.clear();
        internalComputeTimeQueue.clear();
    }

    @Override
    public void basicPurgeEvent() {
        System.out.println("SortedCache purge event");
        purge();
    }

    @Override
    public void criticalPurgeEvent() {
        System.out.println("SortedCache critical purge event");
        clear();
    }
}
