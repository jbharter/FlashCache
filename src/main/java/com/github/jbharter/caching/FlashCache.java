package com.github.jbharter.caching;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;

public class FlashCache<K,V> extends BaseCache<K,V> {

    private ConcurrentHashMap<K,Double> computationOptimization = new ConcurrentHashMap<>();
    private AtomicLong maxNumElements;
    private ConcurrentLinkedQueue<K> keyQueue = new ConcurrentLinkedQueue<>();
    private AtomicBoolean full = new AtomicBoolean(false);
    private int purgeStep;
    private Function<K,V> func;
    // TODO --> manage size based on how much free memory we have

    public FlashCache() {
        internalCache   = new ConcurrentHashMap<>();
        maxNumElements  = new AtomicLong(1000000);
        purgeStep = 20;
        instanceSet.put(this, thing -> this);
    }
    public FlashCache(Function<K,V> function) {
        internalCache   = new ConcurrentHashMap<>();
        maxNumElements  = new AtomicLong(1000000);
        purgeStep = 20;
        instanceSet.put(this, thing -> this);
        func = function;
    }
    public FlashCache(Long maxElements) {
        internalCache   = new ConcurrentHashMap<>();
        maxNumElements  = new AtomicLong(maxElements);
        purgeStep = 20;
        instanceSet.put(this, thing -> this);
    }
    public FlashCache(Long maxElements, int step) {
        internalCache   = new ConcurrentHashMap<>();
        maxNumElements  = new AtomicLong(maxElements);
        purgeStep = step;
        instanceSet.put(this, thing -> this);
    }



    // Instance Management
    V internalRemove(K key) {
        keyQueue.remove(key);
        return internalCache.remove(key);
    }
    V internalPut(K key, V value) {
        keyQueue.add(key);
        return internalCache.put(key,value);
    }

//    V internalPut(K key, Function<? super K, ? extends V> mapper) {
//        return null;
//    }

    void internalPutAll(Map<? extends K, ? extends V> m) {

    }
    void purge() {
        if (keyQueue.size() > 0) internalCache.remove(keyQueue.poll());
        else {
            internalCache.clear();
            keyQueue.clear();
        }
    }
    void purge(int num){
        for (int i = 0; i < num; ++i) purge();
    }


    private AtomicLong getMaxNumElements() { return maxNumElements; }
    private ConcurrentLinkedQueue<K> getKeyQueue() { return keyQueue; }
    private AtomicBoolean getFull() { return full; }
    private int getPurgeStep() { return purgeStep; }
    private Function<K,V> getFunc() { return func; }

    void purgeHard(int num) {
        purge(num);
        FlashCache<K,V> t = this;
        this.internalCache = new ConcurrentHashMap<>(t.size());
        this.internalCache.putAll(t.getInternalCache());
        this.maxNumElements = t.getMaxNumElements();
        this.keyQueue = t.getKeyQueue();
        this.full = t.getFull();
        this.purgeStep = t.getPurgeStep();
        this.func = t.getFunc();
    }

    @Override
    Long getMeanMemberSize() {
        return null;
    }

    public void finalize() {
        instanceSet.remove(this);
    }

    V internalPut(K key, Function<? super K,? extends V> function) {
        Long t = System.nanoTime();
        V t2 = function.apply(key);
        computationOptimization.put(key, ((double) System.currentTimeMillis()) - t);
        return t2;
    }
    private Double getAverageComputeTime() {
        OptionalDouble t = computationOptimization.values().stream().mapToDouble(each -> each).average();
        return (t.isPresent()) ? t.getAsDouble() : -1;
    }

    // Map interface

    public V put(K key, V value) {
        if (!full.getAndSet(maxNumElements.get() < size() + purgeStep)) {
            return internalPut(key,value);
        } else {
            purge(purgeStep);
            return internalPut(key,value);
        }
    }
    public void putAll(Map<? extends K, ? extends V> m) {
        if (m.size() < maxNumElements.get()) {
            internalCache.putAll(m);
        } else {
            Long added = m.entrySet().parallelStream().map(entry -> put(entry.getKey(),entry.getValue())).count();
            if (added == m.size()) full.getAndSet(m.size() - purgeStep > size());
        }
    }

    @Override
    public void clear() {

    }

    public V put(K key) { return internalPut(key,this.func); }
    public V put(K key, Function<K,V> mapper) { return internalPut(key,mapper); }
    public void setMapper(Function<K,V> map) { this.func = map; }

    }
